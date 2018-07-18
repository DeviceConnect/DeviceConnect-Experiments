/*
 DuplicatedPathException.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen;


import java.util.ArrayList;
import java.util.List;

public class DuplicatedPathException extends IllegalProfileSpecException {

    private final List<NameDuplication> duplications;

    DuplicatedPathException(final List<NameDuplication> duplications) {
        this.duplications = duplications;
    }

    public List<NameDuplication> getDuplications() {
        return new ArrayList<>(duplications);
    }
}