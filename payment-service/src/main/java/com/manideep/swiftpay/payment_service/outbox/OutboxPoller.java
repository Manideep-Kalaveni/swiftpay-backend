package com.manideep.swiftpay.payment_service.outbox;

import com.manideep.swiftpay.common_lib.config.KafkaProperties;
import com.manideep.swiftpay.common_lib.enums.OutboxStatus;
import com.manideep.swiftpay.payment_service.entity.OutboxEvent;
import com.manideep.swiftpay.payment_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProperties kafkaProperties;
    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final OutboxResultHandler outboxResultHandler;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(name = "payment-service-outbox-poller", lockAtLeastFor = "1s", lockAtMostFor = "1m")
    public void someMethod(){
        
            List<OutboxEvent> events = outboxEventRepository.findByStatusOrderByCreatedAt(OutboxStatus.PENDING);

            for(OutboxEvent event : events){
                try {

                    String topic = kafkaProperties.topicFor(event.getStatus());
                    String key = extractMerchantId(event.getPayload());

                    Map<String, Object> envelop = Map.of(
                            "eventType", event.getEventType(),
                            "aggregateType", event.getEventAggregateType().name(),
                            "aggregateId", event.getAggregateId(),
                            "data", event.getPayload()
                    );

                    kafkaTemplate.send(topic, key, envelop).get(5, TimeUnit.SECONDS);
                    outboxResultHandler.handleEventPublished(event);

                }
                catch (Exception e){
                    log.error("Outbox event failed event id : {}, attempts : {}", event.getId(), event.getAttempts());
                    outboxResultHandler.handleEventFailed(event,e.getMessage());
                }
            }
    }


    private String extractMerchantId(Map<String,Object> payload){
        Object merchantId = payload.get("merchantId");
        return merchantId != null ? merchantId.toString() : "unknown";
    }
}
