package com.example.warehouse.entity;

public enum Role {
    ROLE_ADMIN,        // full access, system config
    ROLE_MANAGER,      // can add/move products, create shelves
    ROLE_WORKER,       // can view and update quantities only
    ROLE_READONLY      // forklift driver, can only look up locations
}
