package com.jobPortal.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private LocalDateTime timeStamp;

    private int status;
    private String message;
    private T data;

    public ApiResponse(int status, String message, T data) {
        this.timeStamp = LocalDateTime.now();
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
