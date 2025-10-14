package com.example.j2ee_project.model.request.email;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.*;

class EmailRequestValidator implements ConstraintValidator<ValidEmailRequest, EmailRequest> {
    @Override
    public boolean isValid(EmailRequest request, ConstraintValidatorContext context) {
        if (request.getSendType() == null) {
            return false;
        }

        context.disableDefaultConstraintViolation();
        boolean isValid = true;

        switch (request.getSendType()) {
            case USER:
                if (request.getUserId() == null) {
                    context.buildConstraintViolationWithTemplate("userId là bắt buộc cho sendType USER")
                            .addPropertyNode("userId")
                            .addConstraintViolation();
                    isValid = false;
                }
                if (request.getUserIds() != null || request.getRoleId() != null) {
                    context.buildConstraintViolationWithTemplate("userIds và roleId phải null khi sendType là USER")
                            .addConstraintViolation();
                    isValid = false;
                }
                break;
            case LIST:
                if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
                    context.buildConstraintViolationWithTemplate("userIds là bắt buộc và không được rỗng cho sendType LIST")
                            .addPropertyNode("userIds")
                            .addConstraintViolation();
                    isValid = false;
                }
                if (request.getUserId() != null || request.getRoleId() != null) {
                    context.buildConstraintViolationWithTemplate("userId và roleId phải null khi sendType là LIST")
                            .addConstraintViolation();
                    isValid = false;
                }
                break;
            case ROLE:
                if (request.getRoleId() == null) {
                    context.buildConstraintViolationWithTemplate("roleId là bắt buộc cho sendType ROLE")
                            .addPropertyNode("roleId")
                            .addConstraintViolation();
                    isValid = false;
                }
                if (request.getUserId() != null || request.getUserIds() != null) {
                    context.buildConstraintViolationWithTemplate("userId và userIds phải null khi sendType là ROLE")
                            .addConstraintViolation();
                    isValid = false;
                }
                break;
        }

        return isValid;
    }
}