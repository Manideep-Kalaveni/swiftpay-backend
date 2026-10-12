package com.manideep.swiftpay.merchant_service.service;


import com.manideep.swiftpay.merchant_service.dto.request.LoginRequestDTO;
import com.manideep.swiftpay.merchant_service.dto.request.MerchantRequestDTO;
import com.manideep.swiftpay.merchant_service.dto.response.LoginResponseDTO;
import com.manideep.swiftpay.merchant_service.dto.response.MerchantResponseDTO;

public interface AuthService {
    MerchantResponseDTO registerMerchant(MerchantRequestDTO merchantRequestDTO);

    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
}
