package io.siggi.der;

import io.siggi.tools.Hex;

import java.util.Arrays;

public final class Der03BitString extends Der {
    private static final Tag TAG = new Tag(DerClass.UNIVERSAL, false, 0x3);

    public Der03BitString(byte[] data) {
        super(TAG, data);
        if (data.length == 0) {
            throw new IllegalArgumentException("Empty bit string is not allowed.");
        }
        if (data.length == 1 && data[0] != (byte) 0) {
            throw new IllegalArgumentException("Zero length bit string must have unused bits set to 0.");
        }
        if (data.length > 1 && (data[0] & 0xff) > 7) {
            throw new IllegalArgumentException("Invalid unused bits value");
        }
    }

    public static Der03BitString create(byte[] data, int unusedBits) {
        if (unusedBits < 0 || unusedBits > 7) {
            throw new IllegalArgumentException("unusedBits must be between 0 and 7");
        }
        byte[] newData = Arrays.copyOf(data, data.length + 1);
        newData[0] = (byte) unusedBits;
        return new Der03BitString(newData);
    }

    public int getUnusedBits() {
        return data[0] & 0xff;
    }

    public byte[] getBytes() {
        return Arrays.copyOfRange(data, 1, data.length);
    }

    @Override
    public String toString() {
        return "BitString:" + Hex.toHex(getData());
    }
}
