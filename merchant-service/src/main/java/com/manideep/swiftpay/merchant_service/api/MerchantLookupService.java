package com.manideep.swiftpay.merchant_service.api;


import com.manideep.swiftpay.common_lib.dto.SettlementBankDetailsDTO;
import com.manideep.swiftpay.common_lib.dto.WebhookTargetResponseDTO;

import java.util.List;
import java.util.UUID;

public interface MerchantLookupService {

    List<WebhookTargetResponseDTO> getActiveConfigsForEvent(UUID merchantId, String eventType);

    List<UUID> getActiveMerchants();

    SettlementBankDetailsDTO getSettlementBankDetails(UUID merchantId);
}
