package com.manideep.swiftpay.common_lib.web;

import com.manideep.swiftpay.common_lib.context.MerchantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class MerchantContextFilter extends OncePerRequestFilter {

    public static final String MERCHANT_ID_HEADER = "X-Merchant-Id";
    public static final String KEY_ID_HEADER = "X-Key-Id";

    private final MerchantContext merchantContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String merchantHeader = request.getHeader(MERCHANT_ID_HEADER);

        if(merchantHeader != null && !merchantHeader.isBlank()){
            merchantContext.setMerchantId(UUID.fromString(merchantHeader));
        }

        String keyIdHeader = request.getHeader(KEY_ID_HEADER);

        if(keyIdHeader != null && !keyIdHeader.isBlank()){
            merchantContext.setKeyId(keyIdHeader);
        }

        filterChain.doFilter(request,response);
    }
}
