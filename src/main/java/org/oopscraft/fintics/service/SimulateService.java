package org.oopscraft.fintics.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.simulate.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulateService implements ApplicationListener<ContextClosedEvent> {

    private final SimulateRepository simulateRepository;

    private final SimulateRunnableFactory simulateRunnableFactory;

    private final SimpMessagingTemplate messagingTemplate;

    private final BlockingQueue<Runnable> simulateQueue = new ArrayBlockingQueue<>(10);

    private final ThreadPoolExecutor simulateExecutor = new ThreadPoolExecutor(
            1,
            3,
           60,
            TimeUnit.SECONDS,
            simulateQueue
    );

    private final Map<String,SimulateRunnable> simulateRunnableMap = new ConcurrentHashMap<>();

    private final Map<String,Future<?>> simulateFutureMap = new ConcurrentHashMap<>();

    public Page<Simulate> getSimulates(String tradeId, Simulate.Status status, Pageable pageable) {
        // where
        Specification<SimulateEntity> specification = Specification.where(null);
        specification = specification
                .and(Optional.ofNullable(tradeId)
                        .map(SimulateSpecifications::equalTradeId)
                        .orElse(null))
                .and(Optional.ofNullable(status)
                        .map(SimulateSpecifications::equalStatus)
                        .orElse(null));

        // sort
        Sort sort = Sort.by(SimulateEntity_.STARTED_AT).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // find
        Page<SimulateEntity> simulateEntityPage = simulateRepository.findAll(specification, pageable);
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

    public synchronized Simulate runSimulate(Simulate simulate) {
        simulate.setSimulateId(IdGenerator.uuid());

        // add log appender
        Context context = ((Logger)log).getLoggerContext();
        SimulateLogAppender simulateLogAppender = new SimulateLogAppender(simulate, context, messagingTemplate);

        // run
        SimulateRunnable simulateRunnable = simulateRunnableFactory.getObject(simulate);
        simulateRunnable.setSimulateLogAppender(simulateLogAppender);
        simulateRunnable.onComplete(() -> {
            this.simulateRunnableMap.remove(simulate.getSimulateId());
            this.simulateFutureMap.remove(simulate.getSimulateId());
        });

        Future<?> simulateFuture = simulateExecutor.submit(simulateRunnable);
        simulateRunnableMap.put(simulate.getSimulateId(), simulateRunnable);
        simulateFutureMap.put(simulate.getSimulateId(), simulateFuture);

        // return
        return simulate;
    }

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
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        this.simulateExecutor.shutdownNow();
    }

}
