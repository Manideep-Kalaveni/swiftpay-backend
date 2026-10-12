package com.manideep.swiftpay.operations_service.webhook;

import com.manideep.swiftpay.common_lib.enums.WebhookEventStatus;
import com.manideep.swiftpay.operations_service.entity.WebhookEvent;
import com.manideep.swiftpay.operations_service.repository.WebhookEventRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliverySchedular {

    private final WebhookEventRepository webhookEventRepository;
    private final WebhookRetryQueue retryQueue;
    private final WebhookDeliveryExecutor deliveryExecutor;

    @Value("${app.webhook.delivery.poll-batch-size:100}")
    private int batchSize;

    private ExecutorService virtualThreadExecutor;

    @PostConstruct
    void init(){
        virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @PreDestroy
    void shutdown(){
        virtualThreadExecutor.shutdown();
    }

    @Scheduled(fixedDelay = 1000)
    @SchedulerLock(name = "operations-service-webhook-delivery", lockAtLeastFor = "1s", lockAtMostFor = "1m")
    public void pollAndDeliver(){
        Set<UUID> due = retryQueue.pollDue(batchSize);

        if(due.isEmpty()) return;

        for(UUID id : due){
            // deliver..
            // Utilized virtual threads to run in another threads no need to block the current running thread
            virtualThreadExecutor.execute(() -> deliveryExecutor.deliver(id));

        }
    }

    @Scheduled(fixedDelay = 10000)
    public void reconcileFromDatabase(){
        LocalDateTime now = LocalDateTime.now();

        List<WebhookEvent> due = webhookEventRepository.findByStatusAndNextRetryAtBefore(WebhookEventStatus.PENDING, now);

        for(WebhookEvent event : due){
            retryQueue.enqueueIfAbsent(event.getId(),event.getNextRetryAt());
        }
    }
}
