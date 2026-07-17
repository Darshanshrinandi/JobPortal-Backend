package com.jobPortal.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;

@Data
@Getter
@Setter
public class ErrorResponse {

    private LocalDate timestamp;
    private int status;
    private String message;


    public ErrorResponse(LocalDate timestamp, int status, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
    }


}
