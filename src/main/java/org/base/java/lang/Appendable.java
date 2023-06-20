package org.base.java.lang;


import org.base.java.io.IOException;

public interface Appendable {
    Appendable append(CharSequence csq) throws IOException;

    Appendable append(CharSequence csq, int start, int end) throws IOException;

    Appendable append(char c) throws IOException;
}
