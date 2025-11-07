package com.iccues.metaanimebackend.entity;

public enum Season {
    WINTER,
    SPRING,
    SUMMER,
    FALL;

    public int toMonth() {
        return switch (this) {
            case WINTER -> 1;
            case SPRING -> 4;
            case SUMMER -> 7;
            case FALL -> 10;
        };
    }

    public String toLowerName() {
        return this.name().toLowerCase();
    }
}
