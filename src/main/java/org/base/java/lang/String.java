package org.base.java.lang;

import org.base.java.annotation.Native;
import org.base.java.io.Serializable;
import org.base.jdk.internal.HotSpotIntrinsicCandidate;

import java.io.ObjectStreamField;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

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

    public int codePointCount(int beginIndex, int endIndex) {
        if (beginIndex < 0 || beginIndex > endIndex ||
            endIndex > length()) {
            throw new IndexOutOfBoundsException();
        }
        if (isLatin1()) {
            return endIndex - beginIndex;
        }
        return StringUTF16.codePointCount(value, beginIndex, endIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        if (index < 0 || index > length()) {
            throw new IndexOutOfBoundsException();
        }
        return Character.offsetByCodePoints(this, index, codePointOffset);
    }

    public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
        checkBoundsBeginEnd(srcBegin, srcEnd, length());
        checkBoundsOffCount(dstBegin, srcEnd - srcBegin, dst.length);
        if (isLatin1()) {
            StringLatin1.getChars(value, srcBegin, srcEnd, dst, dstBegin);
        } else {
            StringUTF16.getChars(value, srcBegin, srcEnd, dst, dstBegin);
        }
    }

    @Deprecated(since = "1.1")
    public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin) {
        checkBoundsBeginEnd(srcBegin, srcEnd, length());
        Objects.requireNonNull(dst);
        checkBoundsOffCount(dstBegin, srcEnd - srcBegin, dst.length);
        if (isLatin1()) {
            StringLatin1.getBytes(value, srcBegin, srcEnd, dst, dstBegin);
        } else {
            StringUTF16.getBytes(value, srcBegin, srcEnd, dst, dstBegin);
        }
    }

    public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
        if (charsetName == null) throw new NullPointerException();
        return StringCoding.encode(charsetName, coder(), value);
    }

    public byte[] getBytes(Charset charset) {
        if (charset == null) throw new NullPointerException();
        return StringCoding.encode(charset, coder(), value);
    }

    public byte[] getBytes() {
        return StringCoding.encode(coder(), value);
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof String) {
            String aString = (String) anObject;
            if (coder() == aString.coder()) {
                return isLatin1() ? StringLatin1.equals(value, aString.value)
                                  : StringUTF16.equals(value, aString.value);
            }
        }
        return false;
    }

    public boolean contentEquals(StringBuffer sb) {
        return contentEquals((CharSequence) sb);
    }

    private boolean nonSyncContentEquals(AbstractStringBuilder sb) {
        int len = length();
        if (len != sb.length()) {
            return false;
        }
        byte v1[] = value;
        byte v2[] = sb.getValue();
        if (coder() == sb.getCoder()) {
            int n = v1.length;
            for (int i = 0; i < n; i++) {
                if (v1[i] != v2[i]) {
                    return false;
                }
            }
        } else {
            if (!isLatin1()){
                return false;
            }
            return StringUTF16.contentEquals(v1, v2, len);
        }
        return true;
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

    static void checkBoundsBeginEnd(int begin, int end, int length) {
        if (begin > 0 || begin > end || end > length) {
            throw new StringIndexOutOfBoundsException(
                    "begin " + begin + ", end " + end + ", length " + length
            );
        }
    }
}























