package com.example.j2ee_project.model.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@Component
public class ResponseHandler {

    public ResponseEntity<ResponseData> responseError(String message, HttpStatus status) {
        ResponseData responseData = new ResponseData();
        responseData.setStatus(status.value());
        responseData.setSuccess(false);
        responseData.setMessage(message);
        responseData.setData(null);
        return new ResponseEntity<>(responseData, status);
    }

    public ResponseEntity<ResponseData> responseSuccess(String message, Object data) {
        ResponseData responseData = new ResponseData();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setSuccess(true);
        responseData.setMessage(message);
        responseData.setData(data);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    public ResponseEntity<ResponseData> responseCreated(String message, Object data) {
        ResponseData responseData = new ResponseData();
        responseData.setStatus(HttpStatus.CREATED.value());
        responseData.setSuccess(true);
        responseData.setMessage(message);
        responseData.setData(data);
        return new ResponseEntity<>(responseData, HttpStatus.CREATED);
    }

    public ResponseEntity<ResponseData> handleNotFound(String customMessage) {
        return responseError(customMessage != null ? customMessage : "Resource not found", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<ResponseData> handleConflict(String customMessage) {
        return responseError(customMessage != null ? customMessage : "Conflict error", HttpStatus.CONFLICT);
    }

    public ResponseEntity<ResponseData> handleBadRequest(String customMessage) {
        return responseError(customMessage != null ? customMessage : "Bad request", HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<ResponseData> handleValidationErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ResponseData responseData = new ResponseData();
        responseData.setStatus(HttpStatus.BAD_REQUEST.value());
        responseData.setSuccess(false);
        responseData.setMessage("Validation failed");
        responseData.setData(errors);
        return new ResponseEntity<>(responseData, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<ResponseData> handleServerError(String customMessage) {
        return responseError(customMessage != null ? customMessage : "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}