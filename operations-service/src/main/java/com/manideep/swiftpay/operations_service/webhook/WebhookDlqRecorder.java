package com.manideep.swiftpay.operations_service.webhook;

import com.manideep.swiftpay.common_lib.enums.WebhookEventStatus;
import com.manideep.swiftpay.operations_service.entity.DLQEvent;
import com.manideep.swiftpay.operations_service.entity.WebhookEvent;
import com.manideep.swiftpay.operations_service.repository.DLQEventRepository;
import com.manideep.swiftpay.operations_service.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookDlqRecorder {

    private final DLQEventRepository dlqEventRepository;
    private final WebhookEventRepository webhookEventRepository;

    @Transactional
    public void recordAfterAttemptExhaustion(WebhookEvent event, String finalError){
        event.setStatus(WebhookEventStatus.DEAD);
        webhookEventRepository.save(event);

        DLQEvent dlqEvent = DLQEvent.builder()
                .webhookEvent(event)
                .merchantId(event.getMerchantId())
                .finalError(finalError)
                .payload(event.getPayload())
                .build();

        dlqEventRepository.save(dlqEvent);
    }

    public void consumerFailed(ConsumerRecord<String, Map<String, Object>> record, String message) {
        Map<String,Object> envelop = record.value();

        UUID merchantId = null;

        try{
            Map<String,Object> data = (Map<String, Object>) envelop.get("data");
            Object rawMerchantId = data != null ? data.get("merchantId") : null;

            if(rawMerchantId != null){
                merchantId = UUID.fromString(rawMerchantId.toString());
            }

        } catch (Exception ignore) {

        }

        DLQEvent dlqEvent = DLQEvent.builder()
                .merchantId(merchantId)
                .finalError(message)
                .payload(envelop != null ? envelop : Map.of())
                .build();

        dlqEventRepository.save(dlqEvent);
    }
}
