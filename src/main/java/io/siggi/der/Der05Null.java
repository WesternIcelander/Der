package io.siggi.der;

public final class Der05Null extends Der {
    private static final Tag TAG = new Tag(DerClass.UNIVERSAL, false, 0x5);
    public static final Der05Null INSTANCE = new Der05Null();

    private Der05Null() {
        super(TAG, new byte[0]);
    }

    @Override
    public String toString() {
        return "Null:";
    }
}
