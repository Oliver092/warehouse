package com.example.warehouse.exception;

public class SkuConflictException extends RuntimeException {
    public SkuConflictException(String shelfCode, String existingSku) {
        super("Shelf " + shelfCode + " already contains SKU: " + existingSku + ". Cannot mix SKUs on the same shelf.");
    }
}
