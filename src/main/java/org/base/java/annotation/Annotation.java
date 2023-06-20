package org.base.java.annotation;

import org.base.java.lang.Object;

public interface Annotation {
    boolean equals(Object obj);

    int hashCode();

    String toString();

    Class<? extends Annotation> annotationType();
}
