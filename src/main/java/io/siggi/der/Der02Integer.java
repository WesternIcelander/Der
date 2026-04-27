package io.siggi.der;

import java.math.BigInteger;

public final class Der02Integer extends Der {
    private static final Tag TAG = new Tag(DerClass.UNIVERSAL, false, 0x2);

    public Der02Integer(BigInteger bigInteger) {
        super(TAG, bigInteger.toByteArray());
    }

    public Der02Integer(byte[] data) {
        super(TAG, data);
    }

    public BigInteger getBigInteger() {
        return new BigInteger(getData());
    }

    @Override
    public String toString() {
        String string = getBigInteger().toString(16);
        if (string.startsWith("-")) {
            string = "-0x" + string.substring(1);
        } else {
            string = "0x" + string;
        }
        return "Integer:" + string;
    }
}
