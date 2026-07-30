package com.example.demo.dto;

public class ResponseDTO<T> {
    private String status; // "SUCCESS" hoặc "ERROR"
    private String message;
    private T data;

    public ResponseDTO(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseDTO<T> success(String message, T data) {
        return new ResponseDTO<>("SUCCESS", message, data);
    }

    public static <T> ResponseDTO<T> error(String message) {
        return new ResponseDTO<>("ERROR", message, null);
    }

    // Getters
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}