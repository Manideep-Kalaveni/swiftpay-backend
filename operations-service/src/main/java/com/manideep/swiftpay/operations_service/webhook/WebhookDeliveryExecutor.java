package com.manideep.swiftpay.operations_service.webhook;

import com.manideep.swiftpay.common_lib.enums.WebhookEventStatus;
import com.manideep.swiftpay.operations_service.entity.WebhookEvent;
import com.manideep.swiftpay.operations_service.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryExecutor {

    private final WebhookEventRepository webhookEventRepository;
    private final WebhookRetryQueue retryQueue;
    private final RestClient restClient;
    private final WebhookDlqRecorder dlqRecorder;

    private final int MAX_ATTEMPTS = 7;
    private static final List<Duration> BACKOFF = List.of(
            Duration.ofMinutes(1),Duration.ofMinutes(5),Duration.ofMinutes(30),
            Duration.ofHours(2),Duration.ofHours(8),Duration.ofHours(24)
    );

    @Value("${app.webhook.delivery.signature-header:X-Swiftpay-Signature}")
    private String signatureHeader;

    @Transactional
    public void deliver(UUID webhookId){
        Optional<WebhookEvent> webhookEvent = webhookEventRepository.findById(webhookId);

        if(webhookEvent.isEmpty()){
            log.warn("webhook event not found with id:{}",webhookId);
            return;
        }

        WebhookEvent event = webhookEvent.get();

        if(event.getStatus().equals(WebhookEventStatus.DELIVERED) || event.getStatus().equals(WebhookEventStatus.DEAD)){
            log.warn("Event id:{} with status:{}",webhookId,event.getStatus());
            return;
        }

        event.setAttempts(event.getAttempts() + 1);
        event.setLastAttemptAt(LocalDateTime.now());

        try{

           var response =  restClient.post()
                    .uri(event.getTargetUrl())
                    .header(signatureHeader, event.getSignature())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("event",event.getEventType(),"payload",event.getPayload()))
                    .retrieve()
                    .toBodilessEntity();

           int statusCode = response.getStatusCode().value();
           event.setLastResponseCode(statusCode);
           if(response.getStatusCode().is2xxSuccessful()){
                event.setStatus(WebhookEventStatus.DELIVERED);
                event.setDeliveredAt(LocalDateTime.now());
                webhookEventRepository.save(event);
                return;
           }

           handleAttemptFailed(event,"HTTP"+statusCode);

        }
        catch(RestClientException e){
            handleAttemptFailed(event,e.getMessage());
        }
    }

    private void handleAttemptFailed(WebhookEvent event, String error) {
        event.setLastResponseBody(error);

        if(event.getAttempts() >= MAX_ATTEMPTS){
            event.setStatus(WebhookEventStatus.DEAD);

            // TODO Save in DLQ
            dlqRecorder.recordAfterAttemptExhaustion(event,error);
            return;
        }

        Duration backoff = BACKOFF.get(event.getAttempts() - 1);
        LocalDateTime nextRetryAt = LocalDateTime.now().plus(backoff);

        event.setStatus(WebhookEventStatus.FAILED);
        event.setNextRetryAt(nextRetryAt);
        webhookEventRepository.save(event);

        retryQueue.enqueue(event.getId(),nextRetryAt);
    }


}
