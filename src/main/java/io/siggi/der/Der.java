package io.siggi.der;

import io.siggi.tools.Hex;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Der {
    public static final byte[] EMPTY = new byte[0];
    protected final Tag tag;
    protected final byte[] data;
    public Der(Tag tag, byte[] data) {
        if (tag == null) throw new NullPointerException("tag");
        if (data == null) throw new NullPointerException("data");
        this.tag = tag;
        this.data = data;
    }

    static boolean canPrint(byte[] data, boolean asciiRestriction) {
        if (data.length == 0) return false;
        for (byte b : data) {
            if (b < 0x20 || (asciiRestriction && b > 0x7e)) return false;
        }
        return true;
    }

    static byte[] encodeLength(int length) {
        byte[] out = new byte[5];
        int i = out.length - 1;
        while (length != 0) {
            byte b = (byte) (length & 0xff);
            out[i--] = b;
            length >>>= 8;
        }
        if (i < 3 || (out[out.length - 1] & 0xff) >= 0x80) {
            int count = out.length - i - 1;
            out[i--] = (byte) (0x80 | count);
        }
        return out;
    }

    static int getLength(byte[] data) {
        int length = data.length;
        for (byte b : data) {
            if (b != 0)
                break;
            length -= 1;
        }
        if (length == 0) length = 1;
        return length;
    }

    public Tag getTag() {
        return tag;
    }

    public byte[] getData() {
        return data;
    }

    public byte[] encode() {
        byte[] data = getData();
        byte[] lengthBytes = encodeLength(data.length);
        int lengthOfLength = getLength(lengthBytes);
        byte[] tagBytes = tag.encode();
        int lengthOfTag = tagBytes.length;
        byte[] output = new byte[lengthOfTag + lengthOfLength + data.length];
        System.arraycopy(tagBytes, 0, output, 0, lengthOfTag);
        System.arraycopy(lengthBytes, lengthBytes.length - lengthOfLength, output, lengthOfTag, lengthOfLength);
        System.arraycopy(data, 0, output, lengthOfTag + lengthOfLength, data.length);
        return output;
    }

    @Override
    public String toString() {
        byte[] data = getData();
        if (canPrint(data, true)) {
            return "String-" + tag.toString() + ":" + new String(data);
        } else {
            return "Hex-" + tag.toString() + ":" + Hex.toHex(data);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Der)) return false;
        Der o = (Der) other;
        return getTag().equals(o.getTag()) && Arrays.equals(getData(), o.getData());
    }

    @Override
    public int hashCode() {
        return getTag().hashCode() * 31 + Arrays.hashCode(getData());
    }

    public enum DerClass {
        UNIVERSAL(0, "U"), APPLICATION(1, "A"), CONTEXT_SPECIFIC(2, "C"), PRIVATE(3, "P");
        private static DerClass[] values;
        private static Map<String, DerClass> shortForms;
        public final int value;
        public final String shortForm;
        DerClass(int value, String shortForm) {
            this.value = value;
            this.shortForm = shortForm;
        }

        public static DerClass of(int value) {
            if (values == null) values = values();
            return values[value];
        }

        public static DerClass of(String shortForm) {
            if (shortForms == null) {
                Map<String, DerClass> map = new HashMap<>();
                for (DerClass value : values()) {
                    map.put(value.shortForm.toLowerCase(), value);
                }
                shortForms = Collections.unmodifiableMap(map);
            }
            return shortForms.get(shortForm.toLowerCase());
        }
    }

    public static final class Tag {
        public final DerClass derClass;
        public final boolean constructed;
        public final int type;
        private byte[] encoded = null;

        public Tag(DerClass derClass, boolean constructed, int type) {
            if (derClass == null) throw new NullPointerException("derClass");
            if (type < 0) throw new IllegalArgumentException("type must be >= 0");
            this.derClass = derClass;
            this.constructed = constructed;
            this.type = type;
        }

        public static Tag read(InputStream in) throws IOException {
            int value = in.read();
            if (value == -1) return null;
            DerClass derClass = DerClass.of((value >> 6) & 0x3);
            boolean constructed = (value & 0x20) != 0;
            int type = value & 0x1F;
            if (type == 0x1F) {
                type = 0;
                int b;
                do {
                    b = in.read();
                    if (b == -1) throw new EOFException();
                    type <<= 7;
                    type |= (b & 0x7f);
                } while ((b & 0x80) != 0);
            }
            return new Tag(derClass, constructed, type);
        }

        public static Tag fromString(String string) {
            try {
                DerClass derClass = DerClass.of(string.substring(0, 1));
                boolean constructed = string.charAt(1) == 'C';
                int type = Integer.parseInt(string.substring(2), 16);
                return new Tag(derClass, constructed, type);
            } catch (Exception e) {
                System.out.println(string);
                throw new IllegalArgumentException("Illegal string", e);
            }
        }

        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Tag)) return false;
            Tag o = (Tag) other;
            return derClass == o.derClass && constructed == o.constructed && type == o.type;
        }

        public int hashCode() {
            return derClass.hashCode() * 31 + (constructed ? 1 : 0) * 31 + type;
        }

        public byte[] encode() {
            if (encoded == null) {
                if (type < 0x1F) {
                    encoded = new byte[]{(byte) (derClass.value << 6 | (constructed ? 0x20 : 0) | type)};
                } else {
                    byte[] buffer = new byte[5];
                    int i = buffer.length;
                    int value = type;
                    while (value != 0) {
                        i--;
                        buffer[i] = (byte) (value & 0x7f);
                        if (i != buffer.length - 1) buffer[i] |= (byte) 0x80;
                        value >>>= 7;
                    }
                    byte[] data = new byte[buffer.length - i];
                    System.arraycopy(buffer, i, data, 0, data.length);
                    encoded = data;
                }
            }
            return encoded;
        }

        public String toString() {
            return derClass.shortForm + (constructed ? "C" : "P") + (type < 0x10 ? "0" : "") + Integer.toString(type, 16);
        }
    }
}
