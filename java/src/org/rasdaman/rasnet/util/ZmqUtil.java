package org.rasdaman.rasnet.util;

public final class ZmqUtil {

    private static final String TCP_PREFIX = "tcp://";
    public static final String ALL_LOCAL_INTERFACES = TCP_PREFIX + "*";
    private static final String INPROC_PREFIX = "inproc://";

    private static final char ADDRESS_PORT_SEPARATOR = ':';

    private ZmqUtil() {
    }

    public static String toAddressPort(String address, int port) {
        StringBuilder result = new StringBuilder();

        result.append(address);
        result.append(ADDRESS_PORT_SEPARATOR);
        result.append(port);

        return result.toString();
    }

    public static String toTcpAddress(String address) {
        return addPrefixIfMissing(address, TCP_PREFIX);
    }

    public static String toInprocAddress(String address) {
        return addPrefixIfMissing(address, INPROC_PREFIX);
    }

    private static String addPrefixIfMissing(String str, String prefix) {
        if (!str.startsWith(prefix)) {
            str = prefix + str;
        }
        return str;
    }

}
