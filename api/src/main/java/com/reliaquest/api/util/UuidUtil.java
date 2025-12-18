package com.reliaquest.api.util;

import java.util.UUID;

public class UuidUtil {
    public static boolean isValidUUID(String id) {
        try {
            UUID.fromString(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}