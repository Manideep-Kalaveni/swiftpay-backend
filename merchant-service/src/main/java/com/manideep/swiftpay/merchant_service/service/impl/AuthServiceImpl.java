package com.manideep.swiftpay.merchant_service.service.impl;

import com.manideep.swiftpay.common_lib.enums.MerchantStatus;
import com.manideep.swiftpay.common_lib.enums.UserRole;
import com.manideep.swiftpay.common_lib.exception.AlreadyExistsException;
import com.manideep.swiftpay.common_lib.exception.BusinessRuleViolationException;
import com.manideep.swiftpay.common_lib.exception.ResourceNotFoundException;
import com.manideep.swiftpay.merchant_service.dto.request.LoginRequestDTO;
import com.manideep.swiftpay.merchant_service.dto.request.MerchantRequestDTO;
import com.manideep.swiftpay.merchant_service.dto.response.LoginResponseDTO;
import com.manideep.swiftpay.merchant_service.dto.response.MerchantResponseDTO;
import com.manideep.swiftpay.merchant_service.entity.AppUser;
import com.manideep.swiftpay.merchant_service.entity.Merchant;
import com.manideep.swiftpay.merchant_service.mapper.MerchantMapper;
import com.manideep.swiftpay.merchant_service.repository.AppUserRepository;
import com.manideep.swiftpay.merchant_service.repository.MerchantRepository;
import com.manideep.swiftpay.merchant_service.security.JwtUtil;
import com.manideep.swiftpay.merchant_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MerchantRepository merchantRepository;
    private final AppUserRepository appUserRepository;
    private final MerchantMapper merchantMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Override
    public MerchantResponseDTO registerMerchant(MerchantRequestDTO merchantRequestDTO) {

        if (merchantRepository.existsByEmail(merchantRequestDTO.email())) {
            throw new AlreadyExistsException("Merchant already present with email id : " + merchantRequestDTO.email(),"MERCHANT_ALREADY_EXISTS");
        }

        Merchant merchant = Merchant.builder()
                .name(merchantRequestDTO.name())
                .email(merchantRequestDTO.email())
                .businessType(merchantRequestDTO.businessType())
                .status(MerchantStatus.PENDING_KYC)
                .businessName(merchantRequestDTO.businessName())
                .build();

        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser.builder()
                .role(UserRole.OWNER)
                .email(merchantRequestDTO.email())
                .merchant(merchant)
                .hashedPassword(passwordEncoder.encode(merchantRequestDTO.password()))
                .build();

        appUserRepository.save(appUser);

        return merchantMapper.toMerchantResponseDTO(merchant);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        AppUser user = appUserRepository.findByEmail(loginRequestDTO.email()).orElseThrow(() -> new ResourceNotFoundException("User", loginRequestDTO.email()));

        if(!passwordEncoder.matches(loginRequestDTO.password(), user.getHashedPassword())){
            throw new BusinessRuleViolationException("INVALID_CREDENTIALS","Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(),user.getMerchant().getId(),user.getRole().name());

        return new LoginResponseDTO(accessToken);
    }
}
