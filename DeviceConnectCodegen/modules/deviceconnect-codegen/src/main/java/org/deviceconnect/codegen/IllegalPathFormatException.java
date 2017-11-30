package org.deviceconnect.codegen;


public class IllegalPathFormatException extends IllegalProfileSpecException {

    private final Reason reason;
    private final String path;

    enum Reason {
        NOT_STARTED_WITH_ROOT,
        TOO_LONG,
        TOO_SHORT
    }

    IllegalPathFormatException(final Reason reason, final String path) {
        if (reason == null) {
            throw new NullPointerException("reason is null.");
        }
        if (path == null) {
            throw new NullPointerException("path is null.");
        }

        this.reason = reason;
        this.path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    public Reason getReason() {
        return reason;
    }

    public String getPath() {
        return path;
    }
}
