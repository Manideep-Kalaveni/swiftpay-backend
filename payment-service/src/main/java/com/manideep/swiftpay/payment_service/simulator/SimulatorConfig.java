package com.manideep.swiftpay.payment_service.simulator;

import com.manideep.swiftpay.common_lib.enums.ChaosMode;
import com.manideep.swiftpay.common_lib.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "payment.simulator")
@Getter
@Setter
public class SimulatorConfig {

    private Integer pollIntervalMs = 2000;
    private ChaosMode chaosMode = ChaosMode.NORMAL;
    private Map<String,MethodSimulatorConfig> method = new HashMap<>();

    public MethodSimulatorConfig configFor(PaymentMethod paymentMethod){
        return method.getOrDefault(paymentMethod.name(), new MethodSimulatorConfig());
    }

    @Getter
    @Setter
    public static class MethodSimulatorConfig{
        private Integer minDelaySeconds = 1;
        private Integer maxDelaySeconds = 5;
        private Integer successRate = 80;
    }

}