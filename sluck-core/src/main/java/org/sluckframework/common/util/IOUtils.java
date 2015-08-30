package org.sluckframework.common.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * IO工具类
 *
 * @author sunxy
 * @since 1.0
 */
public final class IOUtils {

    /**
     * Represents the UTF-8 character set.
     */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    private IOUtils() {
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) { // NOSONAR - empty catch block on purpose
                // ignore
            }
        }
    }

    public static void closeQuietlyIfCloseable(Object closeable) {
        if (closeable instanceof Closeable) {
            closeQuietly((Closeable) closeable);
        }
    }

    public static void closeIfCloseable(Object closeable) throws IOException {
        if (closeable instanceof Closeable) {
            ((Closeable) closeable).close();
        }
    }
}
