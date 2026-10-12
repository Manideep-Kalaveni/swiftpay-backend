package com.manideep.swiftpay.payment_service.simulator;

import com.manideep.swiftpay.common_lib.enums.ChaosMode;
import com.manideep.swiftpay.common_lib.enums.PaymentStatus;
import com.manideep.swiftpay.common_lib.util.RandomizerUtil;
import com.manideep.swiftpay.payment_service.entity.Payment;
import com.manideep.swiftpay.payment_service.repository.PaymentRepository;
import com.manideep.swiftpay.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BankCallbackSimulator {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SimulatorConfig simulatorConfig;

    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    @SchedulerLock(name = "payment-service-bank-callback-simulator", lockAtMostFor = "1m", lockAtLeastFor = "1s")
    public void processCallBack(){

        LocalDateTime globalWindow = LocalDateTime.now().minusSeconds(1);

        List<Payment> candidates = paymentRepository.findByPaymentStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZED, globalWindow);

        if(candidates.isEmpty()){
            return;
        }

        for(Payment payment : candidates){
            simulateCallback(payment);
        }
    }

    private void simulateCallback(Payment payment) {
        SimulatorConfig.MethodSimulatorConfig methodConfig = simulatorConfig.configFor(payment.getPaymentMethod());

        LocalDateTime dueAt = dueAt(payment, methodConfig);

        // For more information this will be a good
        if(LocalDateTime.now().isBefore(dueAt)){
            return;
        }

        ChaosMode chaosMode = simulatorConfig.getChaosMode();

        switch(chaosMode){
            case SUCCESS -> resolve(payment, true);
            case FAILURE -> resolve(payment,false);
            case TIMEOUT -> log.debug("Bankcall Simulator Timedout");
            case NORMAL, SLOW -> resolve(payment, shouldApprove(payment,methodConfig));
        }

    }

    private void resolve(Payment payment, boolean approve){
        if(approve){
            String bankRef = "SIM_BANK_REF"+ RandomizerUtil.randomBase64(8);
            paymentService.resolveAuthorization(payment.getId(), true, bankRef, null, null);
        }
        else{
            paymentService.resolveAuthorization(payment.getId(), false, null, "SIM_BANK_ERROR_CODE", "Simulator Bank Declined");
        }
    }

    private boolean shouldApprove(Payment payment, SimulatorConfig.MethodSimulatorConfig methodConfig){
        int bucket = Math.abs(payment.getId().hashCode()) % 100;
        return bucket < methodConfig.getSuccessRate();
    }

    private LocalDateTime dueAt(Payment payment, SimulatorConfig.MethodSimulatorConfig config){
        int range = config.getMaxDelaySeconds() - config.getMinDelaySeconds();
        int delaySeconds = config.getMinDelaySeconds() + Math.abs(payment.getId().hashCode()) % (range + 1);

        if(simulatorConfig.getChaosMode().equals(ChaosMode.SLOW)){
            delaySeconds += 2;
        }
        return payment.getCreatedAt().plusSeconds(delaySeconds);
    }

}