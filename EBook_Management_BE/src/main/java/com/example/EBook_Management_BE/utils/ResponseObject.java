package com.example.EBook_Management_BE.utils;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseObject {
	@JsonProperty("message")
    private String message;

    @JsonProperty("status")
    private HttpStatus status;
    
    @JsonProperty("data")
    private Object data;
}
