package com.manideep.swiftpay.common_lib.util;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomizerUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String randomBase64(int length){
        byte[] buf = new byte[length/2];

        SECURE_RANDOM.nextBytes(buf);

        return Base64.getEncoder().withoutPadding().encodeToString(buf);
    }
}
