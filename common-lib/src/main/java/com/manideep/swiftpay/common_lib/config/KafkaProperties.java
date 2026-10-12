package com.manideep.swiftpay.common_lib.config;


import com.manideep.swiftpay.common_lib.enums.OutboxStatus;
import com.manideep.swiftpay.common_lib.exception.ResourceNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "app.kafka")
@Getter
@Setter
public class KafkaProperties{

    private Map<String,String> topics;

    public String topicFor(OutboxStatus status){
        String topic = topics.get(status.name().toLowerCase());

        if(topic == null){
            throw new ResourceNotFoundException("TOPIC", status.name());
        }

        return topic;
    }
}
