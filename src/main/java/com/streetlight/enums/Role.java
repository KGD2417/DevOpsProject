package com.streetlight.enums;

import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

public enum Role {
    CITIZEN,
    TECHNICIAN,
    ADMIN;

    @Enumerated(EnumType.STRING)
    public String getValue() {
        return name();
    }
}