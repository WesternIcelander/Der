package io.siggi.der;

import io.siggi.tools.Hex;

public final class Der04OctetString extends Der {
    private static final Tag TAG = new Tag(DerClass.UNIVERSAL, false, 0x4);

    public Der04OctetString(byte[] data) {
        super(TAG, data);
    }

    @Override
    public String toString() {
        return "OctetString:" + Hex.toHex(getData());
    }
}
