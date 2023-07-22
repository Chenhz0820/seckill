package com.xxxx.seckill.util;


import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

    private static final String salt = "1a2b3c4d";

    //md5加密
    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    //前端来到的密码混合盐加密
    public static String inputPassToFromPass(String inputPass) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    public static void main(String[] args) {
        String s = "123456";
        System.out.println(inputPassToFromPass(s));
    }

}
