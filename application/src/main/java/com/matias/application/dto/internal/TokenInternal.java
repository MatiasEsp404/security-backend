package com.matias.application.dto.internal;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public record TokenInternal(
        String accessToken,
        String refreshToken
) {

}
