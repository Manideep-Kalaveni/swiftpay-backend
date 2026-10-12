package com.manideep.swiftpay.payment_service.outbox;

import com.manideep.swiftpay.common_lib.enums.OutboxStatus;
import com.manideep.swiftpay.payment_service.entity.OutboxEvent;
import com.manideep.swiftpay.payment_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OutboxResultHandler {

    private final OutboxEventRepository outboxEventRepository;
    public static final Integer MAX_ATTEMPTS = 3;


    public void handleEventPublished(OutboxEvent event) {
        event.setStatus(OutboxStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
        outboxEventRepository.save(event);
    }

    public void handleEventFailed(OutboxEvent event, String errorMessage){
            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(errorMessage.length() < 500 ? errorMessage : errorMessage.substring(0,500));

            if(event.getAttempts() >= MAX_ATTEMPTS){
                event.setStatus(OutboxStatus.FAILED);
            }
            outboxEventRepository.save(event);
    }
}
