package io.siggi.der;

public final class Der06OID extends Der {
    private static final Tag TAG = new Tag(DerClass.UNIVERSAL, false, 0x6);

    private final String oid;

    public Der06OID(byte[] data) {
        this(OID.decode(data));
    }

    public Der06OID(String oid) {
        super(TAG, EMPTY);
        this.oid = oid;
    }

    @Override
    public byte[] getData() {
        return OID.encode(getOID());
    }

    public String getOID() {
        return oid;
    }

    @Override
    public String toString() {
        return "OID:" + oid;
    }
}
