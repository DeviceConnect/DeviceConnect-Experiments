package org.deviceconnect.codegen;


public class DConnectPathFormatException extends Exception {

    private final Reason reason;

    enum Reason {
        NOT_STARTED_WITH_ROOT,
        TOO_LONG,
        TOO_SHORT
    }

    DConnectPathFormatException(final Reason reason) {
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
