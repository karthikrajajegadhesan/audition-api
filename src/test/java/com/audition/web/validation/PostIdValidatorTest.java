package com.audition.web.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostIdValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest
    @ValueSource(strings = {"1", "42", "999"})
    void isValid_acceptsPositiveNumericIds(final String postId) {
        assertTrue(validator.validate(new PostIdHolder(postId)).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "0", "-1", "1.5", ""})
    void isValid_rejectsInvalidIds(final String postId) {
        assertFalse(validator.validate(new PostIdHolder(postId)).isEmpty());
    }

    @Test
    void isValid_rejectsNullId() {
        assertFalse(validator.validate(new PostIdHolder(null)).isEmpty());
    }

    private static final class PostIdHolder {

        @ValidPostId
        private final String postId;

        private PostIdHolder(final String postId) {
            this.postId = postId;
        }
    }
}
