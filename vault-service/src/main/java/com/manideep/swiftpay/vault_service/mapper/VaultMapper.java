package com.manideep.swiftpay.vault_service.mapper;

import com.manideep.swiftpay.vault_service.dto.response.TokenizeResponseDTO;
import com.manideep.swiftpay.vault_service.entity.VaultCard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VaultMapper {
    TokenizeResponseDTO toTokenizeResponseDTO(VaultCard vaultCard);
}
