package io.siggi.der;

import io.siggi.tools.Hex;

public final class Der12UTF8String extends Der {
    private static final Tag TAG = new Tag(DerClass.UNIVERSAL, false, 0xC);

    public Der12UTF8String(byte[] data) {
        super(TAG, data);
    }

    public Der12UTF8String(String string) {
        this(string.getBytes());
    }

    @Override
    public String toString() {
        byte[] data = getData();
        if (canPrint(data, false)) {
            return "IA5String:" + new String(data);
        } else {
            return "IA5StringHex:" + Hex.toHex(data);
        }
    }
}
