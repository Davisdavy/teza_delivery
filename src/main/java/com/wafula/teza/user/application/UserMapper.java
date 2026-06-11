package com.wafula.teza.user.application;

import com.wafula.teza.user.api.dto.UserResponse;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(UserAccount account) {
        if (account == null) {
            return null;
        }
        return new UserResponse(
                account.id(),
                account.email(),
                account.role(),
                account.enabled()
        );
    }
}
