package com.manideep.swiftpay.merchant_service.controller;

import com.manideep.swiftpay.merchant_service.dto.request.LoginRequestDTO;
import com.manideep.swiftpay.merchant_service.dto.request.MerchantRequestDTO;
import com.manideep.swiftpay.merchant_service.dto.response.LoginResponseDTO;
import com.manideep.swiftpay.merchant_service.dto.response.MerchantResponseDTO;
import com.manideep.swiftpay.merchant_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/signup")
    public ResponseEntity<MerchantResponseDTO> registerMerchant(@RequestBody @Valid MerchantRequestDTO merchantRequestDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerMerchant(merchantRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO){
        return ResponseEntity.ok(authService.login(loginRequestDTO));
    }
}
