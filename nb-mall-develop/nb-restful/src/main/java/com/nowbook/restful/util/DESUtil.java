package com.nowbook.restful.util;


import com.nowbook.weixin.weixin4j.misc.BASE64Decoder;
import com.nowbook.weixin.weixin4j.misc.BASE64Encoder;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;

/**
 * 加密工具类
 * 先使用DES加密，再使用BASE64进行转码，再使用URLEncoder转换为安全的参数传递形式
 * Date: 2017/8/17
 * Author: Romo
 */
@Slf4j
public class DESUtil {

    // 密钥
    private static String secretKey = "qiantanghui12345";

    /**
     * 加密
     *
     * @param datasource String
     * @return String
     */
    public static String encrypt(String datasource) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(secretKey.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
            byte[] result = cipher.doFinal(datasource.getBytes());
            String unSafeStr = new BASE64Encoder().encode(result);
            return URLEncoder.encode(unSafeStr, "utf-8");
        } catch (Exception e) {
            log.error("加密失败", e);
        }
        return null;
    }

    /**
     * 解密
     *
     * @param datasource String
     * @return String
     */
    public static String decrypt(String datasource) {
        try {
            String unSafeStr = URLDecoder.decode(datasource, "utf-8");
            byte[] data = new BASE64Decoder().decodeBuffer(unSafeStr);
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(secretKey.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, securekey, random);
            return new String(cipher.doFinal(data));
        } catch (Exception e) {
            log.error("解密失败", e);
        }
        return null;
    }

}