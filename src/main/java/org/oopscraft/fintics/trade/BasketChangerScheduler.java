package org.oopscraft.fintics.trade;

import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.model.Basket;
import org.oopscraft.fintics.model.BasketSearch;
import org.oopscraft.fintics.service.BasketService;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
public class BasketChangerScheduler {

    private Map<String, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    private final TaskScheduler taskScheduler;

    public void startScheduledTask(Basket basket) {
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                () -> executeTask(basket.getBasketName()),
                new CronTrigger("*/3 * * * * *")
        );
        scheduledTasks.put(basket.getBasketId(), scheduledFuture);
    }

    public void stopScheduledTask(Basket basket) {
        ScheduledFuture<?> scheduledFuture = scheduledTasks.get(basket.getBasketId());
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledTasks.remove(basket.getBasketId());
        }
    }

    private void executeTask(String taskName) {
        // 실제 작업 로직을 여기에 구현
        System.out.println("Executing task: " + taskName);
    }

}
