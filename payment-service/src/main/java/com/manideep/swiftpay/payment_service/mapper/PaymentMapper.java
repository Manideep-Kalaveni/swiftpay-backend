package com.manideep.swiftpay.payment_service.mapper;

import com.manideep.swiftpay.payment_service.dto.response.PaymentResponseDTO;
import com.manideep.swiftpay.payment_service.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "order.id", target = "orderId")
    PaymentResponseDTO toPaymentResponseDTO(Payment payment);

    List<PaymentResponseDTO> toPaymentResponseListDTO(List<Payment> paymentList);
}
