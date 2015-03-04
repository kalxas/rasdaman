package org.rasdaman.rasnet.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by rasdaman on 2/23/15.
 */
public final class Constants {
    //TODO:Add comments
    //The number of I/O threads the ServiceManager should use.
    //One for each Gb/s
    public static final int SERVER_IO_THREAD_NO = 1;

    //The number of CPU threads to use for executing service request
    //TODO-GM: should we use the number of machine CPU - Runtime.getRuntime().availableProcessors()
    public static final int CPU_THREAD_NO = 2;

    public static final int SERVER_ALIVE_TIMEOUT = 1000;

    public static final int SERVER_ALIVE_RETRIES = 1;

    public static final int MAX_OPEN_SOCKETS = 2048;

    public static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    private Constants() {

    }
}
