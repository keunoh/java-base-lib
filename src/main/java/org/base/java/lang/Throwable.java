package org.base.java.lang;

import org.base.java.io.Serializable;

public class Throwable implements Serializable {
    private static final long serialVersionUID = -3042686055658047285L;

    private transient Object backtrace;
    private String detailMessage;

    private static class SentinelHolder {
        public static final StackTraceElement STACK_TRACE_ELEMENT =
                new StackTraceElement("", "", null, Integer.MIN_VALUE);

        public static final StackTraceElement[] STACK_TRACE_SENTINEL =
                new StackTraceElement[] {STACK_TRACE_ELEMENT_SENTINEL};
    }
}
