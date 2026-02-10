package com.Hotelazo.exceptions;


public class InvalidBookingStateAndDateException extends RuntimeException {
    public InvalidBookingStateAndDateException(String message) {
        super(message);
    }
}
