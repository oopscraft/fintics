package org.oopscraft.fintics.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.fintics.dao.SimulateEntity;
import org.oopscraft.fintics.dao.SimulateRepository;
import org.oopscraft.fintics.model.Simulate;
import org.oopscraft.fintics.model.SimulateSearch;
import org.oopscraft.fintics.simulate.SimulateRunnable;
import org.oopscraft.fintics.simulate.SimulateRunnableFactory;
import org.oopscraft.fintics.trade.LogAppender;
import org.oopscraft.fintics.trade.LogAppenderFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulateService implements ApplicationListener<ContextStoppedEvent> {

    private final SimulateRepository simulateRepository;

    private final SimulateRunnableFactory simulateRunnableFactory;

    private final LogAppenderFactory logAppenderFactory;

    private final BlockingQueue<Runnable> simulateQueue = new ArrayBlockingQueue<>(10);

    private final ThreadPoolExecutor simulateExecutor = new ThreadPoolExecutor(
            5,
            5,
           60,
            TimeUnit.SECONDS,
            simulateQueue
    );

    private final Map<String,SimulateRunnable> simulateRunnableMap = new ConcurrentHashMap<>();

    private final Map<String,Future<?>> simulateFutureMap = new ConcurrentHashMap<>();

    public Page<Simulate> getSimulates(SimulateSearch simulateSearch, Pageable pageable) {
        Page<SimulateEntity> simulateEntityPage = simulateRepository.findAll(simulateSearch, pageable);
        List<Simulate> simulates = simulateEntityPage.getContent().stream()
                .map(Simulate::from)
                .toList();
        long count = simulateEntityPage.getTotalElements();
        return new PageImpl<>(simulates, pageable, count);
    }

    public Optional<Simulate> getSimulate(String simulateId) {
        return simulateRepository.findById(simulateId)
                .map(Simulate::from);
    }

    @Transactional
    public synchronized Simulate runSimulate(Simulate simulate) {
        // new simulate
        if(simulate.getSimulateId() == null) {
            simulate.setSimulateId(IdGenerator.uuid());
        }

        // add log appender
        Context context = ((Logger)log).getLoggerContext();
        String destination = String.format("/simulates/%s/log", simulate.getSimulateId());
        LogAppender logAppender = logAppenderFactory.getObject(context, destination);

        // run
        SimulateRunnable simulateRunnable = simulateRunnableFactory.getObject(simulate);
        simulateRunnable.setLogAppender(logAppender);
        simulateRunnable.onComplete(() -> {
            this.simulateRunnableMap.remove(simulate.getSimulateId());
            this.simulateFutureMap.remove(simulate.getSimulateId());
        });
        simulateRunnable.saveSimulate();

        // submit
        Future<?> simulateFuture = simulateExecutor.submit(simulateRunnable);
        simulateRunnableMap.put(simulate.getSimulateId(), simulateRunnable);
        simulateFutureMap.put(simulate.getSimulateId(), simulateFuture);

        // return
        return simulate;
    }

    @Transactional
    public synchronized void stopSimulate(String simulateId) {
        // data status update
        simulateRepository.findById(simulateId).ifPresent(simulateEntity -> {
            simulateEntity.setStatus(Simulate.Status.STOPPING);
            simulateRepository.saveAndFlush(simulateEntity);
        });
        // interrupt thread
        if(simulateRunnableMap.containsKey(simulateId)) {
            simulateRunnableMap.get(simulateId).setInterrupted(true);
            simulateFutureMap.get(simulateId).cancel(true);
            simulateRunnableMap.remove(simulateId);
            simulateFutureMap.remove(simulateId);
        }
    }

    @Transactional
    public Simulate modifySimulate(Simulate simulate) {
         SimulateEntity simulateEntity = simulateRepository.findById(simulate.getSimulateId()).orElseThrow();
         simulateEntity.setStatus(simulate.getStatus());
         simulateEntity.setFavorite(simulate.isFavorite());
         SimulateEntity savedSimulateEntity = simulateRepository.saveAndFlush(simulateEntity);
         return Simulate.from(savedSimulateEntity);
    }

    @Transactional
    public void deleteSimulate(String simulateId) {
        simulateRepository.deleteById(simulateId);
        simulateRepository.flush();
    }

    @Override
    public void onApplicationEvent(@NotNull ContextStoppedEvent event) {
        log.info("ContextStoppedEvent:{}", event);
        this.simulateExecutor.shutdownNow();
    }

}
