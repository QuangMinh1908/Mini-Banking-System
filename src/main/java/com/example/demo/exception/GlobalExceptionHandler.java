package com.example.demo.exception;

import com.example.demo.dto.ResponseDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.RestController;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ResponseDTO<Void>> handleAccountNotFound(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseDTO.error(ex.getMessage()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ResponseDTO<Void>> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseDTO.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<ResponseDTO<Void>> handleInvalidTransfer(InvalidTransferException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseDTO.error(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseDTO<Void>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseDTO.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Void>> handleGeneralException(Exception ex) {
        logger.error("Lỗi hệ thống không xác định: ", ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error("Đã xảy ra lỗi hệ thống, vui lòng thử lại sau!"));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ResponseDTO<Void>> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ResponseDTO.error("Hệ thống đang bận xử lý giao dịch khác trên tài khoản này, vui lòng thử lại sau!"));
    }
}