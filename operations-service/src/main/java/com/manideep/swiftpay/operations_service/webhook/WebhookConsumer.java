package com.manideep.swiftpay.operations_service.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manideep.swiftpay.common_lib.dto.WebhookTargetResponseDTO;
import com.manideep.swiftpay.common_lib.enums.WebhookEventStatus;
import com.manideep.swiftpay.common_lib.util.SignerUtil;
import com.manideep.swiftpay.operations_service.client.MerchantServiceClient;
import com.manideep.swiftpay.operations_service.entity.WebhookEvent;
import com.manideep.swiftpay.operations_service.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookConsumer {

    private final WebhookEventRepository webhookEventRepository;
    private final MerchantServiceClient merchantServiceClient;
    private final ObjectMapper objectMapper;
    private final SignerUtil signerUtil;
    private final WebhookRetryQueue retryQueue;
    private final WebhookDlqRecorder dlqRecorder;

    @KafkaListener(topics = {
            "${app.kafka.topics.order:order.events}",
            "${app.kafka.topics.payment:payment.events}",
            "${app.kafka.topics.refund:refund.events}",
            "${app.kafka.topics.settlement:settlement.events}"
    })
    public void onWebhookEvent(ConsumerRecord<String, Map<String,Object>> record, Acknowledgment ack) throws JsonProcessingException {
        try{
            Map<String,Object> envelop = record.value();
            Map<String,Object> data = (Map<String, Object>) envelop.get("data");

            String eventType = (String) data.get("eventType");
            Object rawMerchantId =  data.get("merchantId");

            if(rawMerchantId == null){
                log.warn("No merchant was found, skipping event : {}", eventType);
                ack.acknowledge();
                return;
            }

            UUID merchantId = UUID.fromString(rawMerchantId.toString());
            List<WebhookTargetResponseDTO> targets = merchantServiceClient.getActiveConfigsForEvent(merchantId, eventType);

            if(targets.isEmpty()){
                log.warn("No target url is present for merchant id : {}", merchantId);
                ack.acknowledge();
                return;
            }

            Map<String,Object> signatureData = Map.of("event", eventType, "payload", data);
            String signatureJson = objectMapper.writeValueAsString(signatureData);

            for(WebhookTargetResponseDTO target : targets){
                String signature = signerUtil.sign(signatureJson, target.webhookSecret());

                WebhookEvent webhookEvent = WebhookEvent.builder()
                        .merchantId(merchantId)
                        .eventType(eventType)
                        .payload(data)
                        .targetUrl(target.targetUrl())
                        .signature(signature)
                        .status(WebhookEventStatus.PENDING)
                        .nextRetryAt(LocalDateTime.now())
                        .build();

                webhookEvent = webhookEventRepository.save(webhookEvent);

                retryQueue.enqueue(webhookEvent.getId(), webhookEvent.getNextRetryAt());

            }
            ack.acknowledge();
        }
        catch (DataAccessException | CannotCreateTransactionException dbDown){
            log.error("Webhook consumer failed due to db down, offset: {}", record.offset());
           // TODO: finished
        } catch (Exception logicError) {
            dlqRecorder.consumerFailed(record, logicError.getMessage());
            ack.acknowledge();
        }

    }
}
