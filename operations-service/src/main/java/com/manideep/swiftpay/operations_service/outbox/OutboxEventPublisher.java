package com.manideep.swiftpay.operations_service.outbox;

import com.manideep.swiftpay.common_lib.enums.EventAggregateType;
import com.manideep.swiftpay.operations_service.entity.OutboxEvent;
import com.manideep.swiftpay.operations_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;

    public void publish(EventAggregateType aggregateType, UUID aggregateId, String eventType, Map<String,Object> payload){
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventAggregateType(aggregateType)
                .aggregateId(aggregateId)
                .payload(payload)
                .eventType(eventType)
                .build();

        outboxEventRepository.save(outboxEvent);
    }
}
