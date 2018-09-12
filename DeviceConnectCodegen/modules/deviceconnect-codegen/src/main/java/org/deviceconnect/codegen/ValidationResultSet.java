/*
 ValidationResultSet.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen;


import java.util.HashMap;
import java.util.Map;

public class ValidationResultSet {

    private final Map<String, ValidationResult> resultMap = new HashMap<>();

    public void addResult(final ValidationResult result) {
        resultMap.put(result.getParamName(), result);
    }

    public boolean isValid() {
        for (ValidationResult result : resultMap.values()) {
            if (!result.isValid()) {
                return false;
            }
        }
        return true;
    }

    public Map<String, ValidationResult> getResults() {
        return new HashMap<>(resultMap);
    }
}