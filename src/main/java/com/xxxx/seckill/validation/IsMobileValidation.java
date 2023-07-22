package com.xxxx.seckill.validation;


import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IsMobileValidation implements ConstraintValidator<IsMobile,String> {

    private boolean required = false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    //使用正则表达式验证手机号码
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (required) {
            return ValidationUtil.isMobile(value);
        }else {
            if (StringUtils.isEmpty(value)) {
                return true;
            } else {
                return ValidationUtil.isMobile(value);
            }
        }
    }
}
