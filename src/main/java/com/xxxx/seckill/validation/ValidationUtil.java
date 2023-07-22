package com.xxxx.seckill.validation;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtil {

    //定义手机号码匹配模板
    public static final Pattern mobile_pattern =  Pattern.compile("[1]([3-9])[0-9]{9}$");

    public static boolean isMobile(String mobile) {
        if (mobile.isEmpty() || mobile.length() != 11) {
            return false;
        }
        //开启匹配
        Matcher matcher = mobile_pattern.matcher(mobile);
        //返回匹配结果
        return matcher.matches();
    }
}
