package org.deviceconnect.codegen;


import static org.deviceconnect.codegen.DConnectPathFormatException.Reason;

public class DConnectPath {

    public static final String SEPARATOR = "/";

    private final String pathExp;
    private final String apiName;
    private final String profileName;
    private final String interfaceName;
    private final String attributeName;
    private final int size;

    public static DConnectPath parsePath(final String basePath, final String pathExp) throws DConnectPathFormatException {
        if (!basePath.startsWith(SEPARATOR)) {
            throw new DConnectPathFormatException(Reason.NOT_STARTED_WITH_ROOT);
        }
        if (!pathExp.startsWith(SEPARATOR)) {
            throw new DConnectPathFormatException(Reason.NOT_STARTED_WITH_ROOT);
        }
        return parsePath(basePath + pathExp);
    }

    public static DConnectPath parsePath(final String pathExp) throws DConnectPathFormatException {
        return new DConnectPath(pathExp);
    }

    private DConnectPath(final String pathExp) throws DConnectPathFormatException {
        if (pathExp == null) {
            throw new NullPointerException("pathExp is null");
        }
        if (!pathExp.startsWith(SEPARATOR)) {
            throw new DConnectPathFormatException(Reason.NOT_STARTED_WITH_ROOT);
        }
        String[] parts = pathExp.split(SEPARATOR);
        int length = parts.length;
        if (length < 3) {
            throw new DConnectPathFormatException(Reason.TOO_SHORT);
        } else if (length == 3) {
            this.apiName = parts[1];
            this.profileName = parts[2];
            this.interfaceName = null;
            this.attributeName = null;
        } else if (length == 4) {
            this.apiName = parts[1];
            this.profileName = parts[2];
            this.interfaceName = null;
            this.attributeName = parts[3];
        } else if (length == 5) {
            this.apiName = parts[1];
            this.profileName = parts[2];
            this.interfaceName = parts[3];
            this.attributeName = parts[4];
        } else {
            throw new DConnectPathFormatException(Reason.TOO_LONG);
        }
        this.pathExp = pathExp;
        this.size = length - 1;
    }

    public String getApiName() {
        return apiName;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getBathPath() {
        return concatParts(apiName, profileName);
    }

    public String getSubPath() {
        if (size == 2) {
            return concatParts();
        } else if (size == 3) {
            return concatParts(attributeName);
        } else if (size == 4) {
            return concatParts(interfaceName, attributeName);
        } else {
            throw new RuntimeException();
        }
    }

    private String concatParts(String... parts) {
        if (parts == null) {
            return SEPARATOR;
        }
        String path = SEPARATOR;
        for (int i = 0; i < parts.length; i++) {
            path += parts[i];
            if (i < parts.length - 1) {
                path += SEPARATOR;
            }
        }
        return path;
    }

    @Override
    public String toString() {
        return pathExp;
    }
}
