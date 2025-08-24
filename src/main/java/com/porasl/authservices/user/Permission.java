package com.porasl.authservices.user;

import lombok.Getter;

public enum Permission {

    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),
    MANAGER_READ("management:read"),
    MANAGER_UPDATE("management:update"),
    MANAGER_CREATE("management:create"),
    MANAGER_DELETE("management:delete");

    @Getter
    private final String permission;
    
    Permission (String permission) { 
    	this.permission = permission; 
    }

    // Manual getter to ensure compilation
    public String getPermission() {
        return permission;
    }
}