package org.base.java.lang;

import org.base.java.annotation.Native;
import org.base.java.io.Serializable;
import org.base.jdk.internal.HotSpotIntrinsicCandidate;

import java.io.ObjectStreamField;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

public final class String
    implements Serializable, Comparable<String>, CharSequence {

    @Stable
    private final byte[] value;

    private final byte coder;

    private int hash;

    private static final long serialVersionUID = -6849794470754667710L;

    static final boolean COMPACT_STRINGS;

    static {
        COMPACT_STRINGS = true;
    }

    private static final ObjectStreamField[] serialPersistentFields =
            new ObjectStreamField[0];

    public String() {
        this.value = "".value;
        this.coder = "".coder;
    }

    @HotSpotIntrinsicCandidate
    public String(String original) {
        this.value = original.value;
        this.coder = original.coder;
        this.hash = original.hash;
    }

    public String(char value[]) {
        this(value, 0, value.length, null);
    }

    public String(char value[], int offset, int count) {
        this(value, offset, count, rangeCheck(value, offset, count));
    }

    private static Void rangeCheck(char[] value, int offset, int count) {
        checkBoundsOffCount(offset, count, value.length);
        return null;
    }

    public String(int[] codePoints, int offset, int count) {
        checkBoundsOffCount(offset, count, codePoints.length);
        if (count == 0) {
            this.value = "".value;
            this.coder = "".coder;
            return;
        }
        if (COMPACT_STRINGS) {
            byte[] val = StringLatin1.toBytes(codePoints, offset, count);
            if (val != null) {
                this.coder = LATIN1;
                this.value = val;
                return;
            }
        }
        this.coder = UTF16;
        this.value = StringUTF16.toBytes(codePoints, offset, count);
    }

    @Deprecated
    public String(byte ascii[], int hibyte, int offset, int count) {
        checkBoundsOffCount(offset, count, ascii.length);
        if (count == 0) {
            this.value = "".value;
            this.coder = "".coder;
            return;
        }
        if (COMPACT_STRINGS && (byte)hibyte == 0) {
            this.value = Arrays.copyOfRange(ascii, offset, offset + count);
            this.coder = LATIN1;
        } else {
            hibyte <<= 8;
            byte[] val = StringUTF16.newBytesFor(count);
            for (int i = 0; i < count; i++) {
                StringUTF16.putChar(val, i, hibyte | (ascii[offset++] & 0xff));
            }
            this.value = val;
            this.coder = UTF16;
        }
    }

    @Deprecated(since = "1.1")
    public String(byte ascii[], int hibyte) { this(ascii, hibyte, 0, ascii.length); }

    public String(byte bytes[], int offset, int length, String charsetName)
            throws UnsupportedEncodingException {
        if (charsetName == null)
            throw new NullPointerException("charsetName");
        checkBoundsOffCount(offset, length, bytes.length);
        StringCoding.Result ret =
                StringCoding.decode(charsetName, bytes, offset, length);
        this.value = ret.value;
        this.coder = ret.coder;
    }

    public String(byte bytes[], int offset, int length, Charset charset) {
        if (charset == null)
            throw new NullPointerException("charset");
        checkBoundsOffCount(offset, length, bytes.length);
        StringCoding.Result ret =
                StringCoding.decode(charset, bytes, offset, length);
        this.value = ret.value;
        this.coder = ret.coder;
    }

    public String(byte bytes[], String charsetName)
        throws UnsupportedEncodingException {
        this(bytes, 0, bytes.length, charsetName);
    }

    public String(byte bytes[], Charset charset) {
        this(bytes, 0, bytes.length, charset);
    }

    public String(byte bytes[], int offset, int length) {
        checkBoundsOffCount(offset, length, bytes.length);
        StringCoding.Result ret = StringCoding.decode(bytes, offset, length);
    }

    public String(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public String(StringBuffer buffer) {
        this(buffer.toString());
    }

    public String(StringBuilder builder) {
        this(builder, null);
    }

    public int length() {
        return value.length >> coder();
    }

    public boolean isEmpty() {
        return value.length == 0;
    }

    public char charAt(int index) {
        if (isLatin1()) {
            return StringLatin1.charAt(value, index);
        } else {
            return StringUTF16.charAt(value, index);
        }
    }

    public int codePointAt(int index) {
        if (isLatin1()) {
            checkIndex(index, value.length);
            return value[index] & 0xff;
        }
        int length = value.length >> 1;
        checkIndex(index, length);
        return StringUTF16.codePointAt(value, index, length);
    }

    public int codePointBefore(int index) {
        int i = index - 1;
        if (i < 0 || i >= length()) {
            throw new StringIndexOutOfBoundsException(index);
        }
        if (isLatin1()) {
            return (value[i] & 0xff);
        }
        return StringUTF16.codePointBefore(value, index);
    }


    private boolean isLatin1() {
        return COMPACT_STRINGS && coder == LATIN1;
    }

    @Native static final byte LATIN1 = 0;
    @Native static final byte UTF16 = 1;

    static void checkIndex(int index, int length) {
        if (index < 0 || index >= length) {
            throw new StringIndexOutOfBoundsException("index " + index + ",length " + length);
        }
    }

    static void checkBoundsOffCount(int offset, int count, int length) {
        if (offset < 0 || count < 0 || offset > length - count) {
            throw new StringIndexOutOfBoundsException(
                    "offset " + offset + ", count" + count + ", length " + length);
        }
    }
}























