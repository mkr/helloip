package io.mkr.helloip.io;

import java.io.IOException;
import java.io.Reader;

/**
 * Strategy interface to create {@link Reader}s on resources from different locations. This enables offering different ways to obtain
 * a textual resource (e.g. a CSV via HTTP, S3 or local file cache).
 * <p/>
 * Note: The caller (the reader's consumer) is responsible for releasing any resources acquired during the reader's creation.
 */
public interface ReaderProvider {

    /**
     * Creates a reader on a resource.
     *
     * @return a {@link Reader}
     * @throws IOException
     */
    Reader createReader() throws IOException;

}
