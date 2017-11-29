package org.deviceconnect.codegen;


class NameDuplication {
    private final String name;
    private int count;

    NameDuplication(final String name) {
        this.name = name;
    }

    public void count() {
        this.count++;
    }

    public boolean isDuplicated() {
        return count > 1;
    }

    public String getName() {
        return name;
    }
}
