package com.multicloud.auth.dto.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralApiResponse<T> {
    private String status;
    private String message;
    private T data;

    public GeneralApiResponse() {
    }

    public GeneralApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> GeneralApiResponse<T> success(String message, T data) {
        return new GeneralApiResponse<>("success", message, data);
    }

    public static <T> GeneralApiResponse<T> fail(String message) {
        return new GeneralApiResponse<>("fail", message, null);
    }

    public static <T> GeneralApiResponse<T> error(String message) {
        return new GeneralApiResponse<>("error", message, null);
    }
}