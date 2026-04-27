package io.siggi.der;

import java.util.Arrays;

public final class Der01Boolean extends Der {
    private static final Tag TAG = new Tag(DerClass.UNIVERSAL, false, 0x1);
    public static final Der01Boolean TRUE = new Der01Boolean(true);
    public static final Der01Boolean FALSE = new Der01Boolean(false);

    private Der01Boolean(boolean value) {
        super(TAG, new byte[]{(byte) (value ? 0xff : 0x00)});
    }

    @Override
    public byte[] getData() {
        byte[] data = super.getData();
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public String toString() {
        return "Boolean:" + ((getData()[0] & 0xff) == 0xff ? "true" : "false");
    }
}
