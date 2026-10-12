package com.manideep.swiftpay.merchant_service.mapper;

import com.manideep.swiftpay.merchant_service.dto.response.MerchantResponseDTO;
import com.manideep.swiftpay.merchant_service.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MerchantMapper {
    @Mapping(source = "status", target = "merchantStatus")
    MerchantResponseDTO toMerchantResponseDTO(Merchant merchant);
}
