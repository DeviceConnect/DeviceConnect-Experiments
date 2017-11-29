package org.deviceconnect.codegen;


class NameDuplication {
    private final String name;
    private int count = 0;

    NameDuplication(final String name) {
        this.name = name;
    }

    public void countUp() {
        this.count++;
    }

    public boolean isDuplicated() {
        return count > 1;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }
}
