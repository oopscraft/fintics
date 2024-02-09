package org.oopscraft.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.FinticsProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredOhlcvDeleter {

    private final FinticsProperties finticsProperties;

    @PersistenceContext
    private final EntityManager entityManager;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 600_000, fixedDelay = 3600_000)
    public void deleteExpiredOhlcvs() {
        log.info("Start delete expired ohlcvs.");
        try {
            LocalDateTime expiredDateTime = LocalDateTime.now().minusMonths(finticsProperties.getOhlcvRetentionMonths());
            deleteExpiredAssetOhlcvs(expiredDateTime);
            deleteExpiredIndiceOhlcvs(expiredDateTime);
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            // TODO send error alarm
            throw new RuntimeException(e);
        }
        log.info("End delete expired ohlcvs");
    }

    void deleteExpiredAssetOhlcvs(LocalDateTime expiredDateTime) {
        TransactionDefinition definition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(definition);
        try {
            entityManager.createQuery(
                            "delete" +
                                    " from AssetOhlcvEntity" +
                                    " where dateTime < :expiredDateTime")
                    .setParameter("expiredDateTime", expiredDateTime)
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
        } finally {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
        }
    }

    void deleteExpiredIndiceOhlcvs(LocalDateTime expiredDateTime) {
        TransactionDefinition definition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(definition);
        try {
            entityManager.createQuery(
                            "delete" +
                                    " from IndiceOhlcvEntity" +
                                    " where dateTime < :expiredDateTime")
                    .setParameter("expiredDateTime", expiredDateTime)
                    .executeUpdate();
            entityManager.flush();
            entityManager.clear();
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
        } finally {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
        }
    }

}
