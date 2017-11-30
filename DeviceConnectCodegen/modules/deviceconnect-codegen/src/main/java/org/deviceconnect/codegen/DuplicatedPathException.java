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
