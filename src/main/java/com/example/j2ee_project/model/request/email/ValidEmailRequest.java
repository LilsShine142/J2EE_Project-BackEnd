package com.example.j2ee_project.model.request.email;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailRequestValidator.class)
@Documented
public @interface ValidEmailRequest {
    String message() default "Validation failed for EmailRequest";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}