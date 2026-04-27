package io.siggi.der;

import io.siggi.tools.Hex;

public final class Der22IA5String extends Der {
    private static final Tag TAG = new Tag(DerClass.UNIVERSAL, false, 0x16);

    public Der22IA5String(byte[] data) {
        super(TAG, data);
        for (byte b : data) {
            if ((b & 0xff) > 0x7F) {
                throw new IllegalArgumentException("Invalid IA5String");
            }
        }
    }

    public Der22IA5String(String string) {
        this(string.getBytes());
    }

    @Override
    public String toString() {
        byte[] data = getData();
        if (canPrint(data, true)) {
            return "IA5String:" + new String(data);
        } else {
            return "IA5StringHex:" + Hex.toHex(data);
        }
    }
}
