package io.mkr.helloip.base;

import io.mkr.helloip.io.ReaderProvider;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class StringReaderProvider implements ReaderProvider {

    private final String s;

    public StringReaderProvider(String s) {
        this.s = s;
    }

    @Override
    public Reader createReader() throws IOException {
        return new StringReader(s);
    }
}
