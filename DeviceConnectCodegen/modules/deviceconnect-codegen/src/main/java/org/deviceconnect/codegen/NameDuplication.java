/*
 NameDuplication.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
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