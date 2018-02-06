package com.nowbook.restful.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class MobileDESUtil {

    public static final String SPKEY = "qiantanghui";

    private final static String IVPARAMETERSPEC = "01020304";   //初始化向量参数，AES 为16bytes. DES 为8bytes.
    private final static String TRANSFORMATION = "DES/CBC/PKCS5Padding";    //DES是加密方式 CBC是工作模式 PKCS5Padding是填充模式
    private final static String ALGORITHM = "DES";  //DES是加密方式

    public static String encrypt(String message) {

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            DESKeySpec desKeySpec = new DESKeySpec(SPKEY.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(IVPARAMETERSPEC.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            return byte2HexString(cipher.doFinal(message.getBytes("GB2312")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * byte[]转换成字符串
     *
     * @param b
     * @return
     */
    public static String byte2HexString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            String stmp = Integer.toHexString(b[i] & 0xff);
            if (stmp.length() == 1)
                sb.append("0" + stmp);
            else
                sb.append(stmp);
        }
        return sb.toString();
    }

    public static String decrypt(String message) {
        try {
            byte[] bytesrc = convertHexString(message);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            DESKeySpec desKeySpec = new DESKeySpec(SPKEY.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(IVPARAMETERSPEC.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] retByte = cipher.doFinal(bytesrc);
            return new String(retByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] convertHexString(String ss) {
        byte digest[] = new byte[ss.length() / 2];
        for (int i = 0; i < digest.length; i++) {
            String byteString = ss.substring(2 * i, 2 * i + 2);
            int byteValue = Integer.parseInt(byteString, 16);
            digest[i] = (byte) byteValue;
        }
        return digest;
    }

    public static void main(String[] args) {
        try {
            /*String oldString = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMzUiLCJkZXZpY2VJZCI6ImMxMjEwZjA3NGVlM2ZlNDQyNmNkNThlNmNhODc0M2M4IiwidHlwZSI6MiwiZGV2aWNlVHlwZSI6MiwiaXNzIjoibmIiLCJpYXQiOjE1MDM2NTUyOTIsImV4cCI6MTUwNjI0NzI5Mn0.ctvtqM_jmDGM2U9xCKeaIjlUjvx_j1PlmwSRsC2-ZUpe9r-FFtljNsp7Y8IAnysp1XBBwzcToRw4YfjkKRi2QA";

            System.out.println("1。分配的SPKEY为: " + SPKEY);
            System.out.println("2。的内容为: " + oldString);
            String reValue = MobileDESUtil.encrypt(oldString);

            System.out.println("进行DES加密后的内容: " + reValue);
            String reValue2 = MobileDESUtil.decrypt(reValue);
            System.out.println("进行DES解密后的内容: " + reValue2);*/

            String a = "122ed515fbb04f5330c71e6d14fe949cf6a4f14915887d4eb3e53e79505303ef4b1150c1c67757cda028ace161be8fa2706b776c41fe13f3039ec9d0262d5d5e8c88a8082a9f5a2d5207afbae1c0e9a904cdc4f1beb793e25bf7af27bb307ef4af80134c6e818f8954441b15e3945484230401b1b943cac787dfffad2c4c1ddebeaba05af61d8730fdb9048c5653bc96ab02dfcf306fb9b6c8d61515026ec8067b90eade11cf36be08e486cc047b371720b461b44ce426462c664345c85ed87ba306f8a55625ea055bb4c0c10419df074086cda53679d3d0664c7a73fcbc8f58d684ea71e89eb573130387a0403920ac27fe75d9febcf98ab542a0c5a6cfbc8e69fc7669873725c6063d95c1df31a153577f1bd1ee453c00692a8400c61145a0";
            String b = decrypt(a);
            String c = b;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}