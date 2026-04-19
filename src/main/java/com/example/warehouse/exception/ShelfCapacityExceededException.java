package com.example.warehouse.exception;

public class ShelfCapacityExceededException extends RuntimeException {
    public ShelfCapacityExceededException(String shelfCode, int available) {
        super("Shelf " + shelfCode + " capacity exceeded. Available space: " + available);
    }
}