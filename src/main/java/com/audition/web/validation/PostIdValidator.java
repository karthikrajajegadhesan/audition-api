package com.audition.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PostIdValidator implements ConstraintValidator<ValidPostId, String> {

    @Override
    public boolean isValid(final String postId, final ConstraintValidatorContext context) {
        if (postId == null || !postId.matches("\\d+")) {
            return reject(postId, context);
        }
        try {
            if (Integer.parseInt(postId) <= 0) {
                return reject(postId, context);
            }
            return true;
        } catch (final NumberFormatException exception) {
            return reject(postId, context);
        }
    }

    private boolean reject(final String postId, final ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Invalid post id: " + postId)
            .addConstraintViolation();
        return false;
    }
}
