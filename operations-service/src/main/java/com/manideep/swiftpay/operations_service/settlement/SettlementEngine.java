package com.manideep.swiftpay.operations_service.settlement;

import com.manideep.swiftpay.operations_service.client.MerchantServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementEngine {

    private final MerchantServiceClient merchantServiceClient;
    private final SettlementTransactionExecutor settlementTransactionExecutor;

    @Scheduled(cron = "0 0 23 * * *")
    @SchedulerLock(name = "operations-service-settlement-engine", lockAtMostFor = "1m", lockAtLeastFor = "1s")
    public void runSchedular(){
        log.info("Settlement running at night 11 pm");
        run();
    }

    private void run(){
        List<UUID> merchants = merchantServiceClient.listActiveMerchantIds();
        log.info("Processing settlements of {} merchants", merchants.size());

        try(ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()){

            List<Future<?>> futures = new ArrayList<>();
            for(UUID merchantId : merchants){
                futures.add(executorService.submit(() -> {
                    // Submit it to another thread instead of blocking the schedular thread
                    settlementTransactionExecutor.processForMerchant(merchantId, LocalDate.now());
                }));
            }

            for(Future<?> future : futures){
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Settlement batch future failed:{}",e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
