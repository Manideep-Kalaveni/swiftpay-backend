package com.manideep.swiftpay.payment_service.mapper;

import com.manideep.swiftpay.payment_service.dto.response.OrderResponseDTO;
import com.manideep.swiftpay.payment_service.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponseDTO toOrderResponseDTO(Order order);
}
