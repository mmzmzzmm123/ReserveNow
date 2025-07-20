package com.room.hd.common;

import lombok.Data;

/**
 * Unified Response Result Class
 * @param <T> Data Type
 */
@Data
public class Result<T> {
    /**
     * Status Code
     */
    private Integer code;
    
    /**
     * Response Message
     */
    private String message;
    
    /**
     * Response Data
     */
    private T data;
    
    /**
     * Private Constructor
     */
    private Result() {}
    
    /**
     * Check if the operation is successful
     * @return Whether successful
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
    
    /**
     * Return success result
     * @param data Response data
     * @param <T> Data type
     * @return Response result
     */
    public static <T> Result<T> success(T data) {
        return success(data, "Operation successful");
    }
    
    /**
     * Return success result
     * @param data Response data
     * @param message Response message
     * @param <T> Data type
     * @return Response result
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    
    /**
     * Return error result
     * @param code Status code
     * @param message Response message
     * @param <T> Data type
     * @return Response result
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
    
    /**
     * Return parameter validation failed result
     * @param message Response message
     * @param <T> Data type
     * @return Response result
     */
    public static <T> Result<T> validateFailed(String message) {
        return error(400, message);
    }
    
    /**
     * Return unauthorized result (not logged in)
     * @param <T> Data type
     * @return Response result
     */
    public static <T> Result<T> unauthorized() {
        return error(401, "Not logged in or token expired");
    }
    
    /**
     * Return forbidden result
     * @param <T> Data type
     * @return Response result
     */
    public static <T> Result<T> forbidden() {
        return error(403, "No permission");
    }
    
    /**
     * Return forbidden result
     * @param message Error message
     * @param <T> Data type
     * @return Response result
     */
    public static <T> Result<T> forbidden(String message) {
        return error(403, message);
    }
    
    /**
     * Return not found result
     * @param <T> Data type
     * @return Response result
     */
    public static <T> Result<T> notFound() {
        return error(404, "Requested resource does not exist");
    }
    
    /**
     * Return server internal error
     * @param <T> Data type
     * @return Response result
     */
    public static <T> Result<T> serverError() {
        return error(500, "Internal server error");
    }
} 