package com.example.warehouse.security;

public class RoleConstants {
    public static final String ADMIN = "hasRole('ROLE_ADMIN')";
    public static final String MANAGER_OR_ABOVE = "hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')";
    public static final String ALL_STAFF = "hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_WORKER', 'ROLE_READONLY')";
}
