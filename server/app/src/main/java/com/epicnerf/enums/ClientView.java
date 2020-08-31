package com.epicnerf.enums;

public enum ClientView {
    FINANCE("Finance"), CHORE("Chore");

    private final String name;

    private ClientView(String brand) {
        this.name = brand;
    }

    @Override
    public String toString() {
        return name;
    }
}