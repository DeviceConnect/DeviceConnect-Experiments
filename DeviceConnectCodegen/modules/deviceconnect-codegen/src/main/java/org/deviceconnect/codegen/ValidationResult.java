/*
 ValidationResult.java
 Copyright (c) 2018 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.codegen;


public class ValidationResult {

    private String paramName;

    private boolean isValid;

    private String errorMessage;

    private ValidationResult() {}

    public static ValidationResult valid(final String paramName) {
        ValidationResult result = new ValidationResult();
        result.paramName = paramName;
        result.isValid = true;
        return result;
    }

    public static ValidationResult invalid(final String paramName, final String errorMessage) {
        ValidationResult result = new ValidationResult();
        result.paramName = paramName;
        result.isValid = false;
        result.errorMessage = errorMessage;
        return result;
    }

    public String getParamName() {
        return paramName;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}