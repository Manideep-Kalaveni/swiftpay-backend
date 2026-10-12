package com.manideep.swiftpay.operations_service.settlement;

import com.manideep.swiftpay.common_lib.enums.SettlementStatus;
import com.manideep.swiftpay.common_lib.util.RandomizerUtil;
import com.manideep.swiftpay.operations_service.entity.Settlement;
import com.manideep.swiftpay.operations_service.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankSettlementCallbackSimulator {

    private final SettlementRepository settlementRepository;
    private final SettlementTransactionExecutor settlementTransactionExecutor;


    @Scheduled(fixedDelayString = "5000")
    @SchedulerLock(name = "operations-service-bank-callback", lockAtLeastFor = "1s", lockAtMostFor = "1m")
    public void processCallbacks(){
        List<Settlement> settlements = settlementRepository.findByStatus(SettlementStatus.TRANSFER_PENDING);

        if(settlements.isEmpty()) return;

        for(Settlement settlement : settlements){
            simulateCallBack(settlement.getId());
        }
    }

    private void simulateCallBack(UUID settlementId) {
        log.info("Initiating settlement callback for settlement id :{}", settlementId);
        String utrNumber = "UTR_" + RandomizerUtil.randomBase64(12);
        settlementTransactionExecutor.resolveTransfer(settlementId, utrNumber, null, null);
    }
}