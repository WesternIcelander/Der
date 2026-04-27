package io.siggi.der;

import java.util.regex.Pattern;

public final class Der19PrintableString extends Der {
    private static final Tag TAG = new Tag(DerClass.UNIVERSAL, false, 0x13);

    private static Pattern pattern = Pattern.compile("[A-Za-z0-9'+,\\-./:=?() ]*");

    public Der19PrintableString(byte[] data) {
        super(TAG, data);
        if (!pattern.matcher(new String(data)).matches()) {
            throw new IllegalArgumentException("Invalid PrintableString");
        }
    }

    public Der19PrintableString(String string) {
        this(string.getBytes());
    }

    @Override
    public String toString() {
        return "PrintableString:" + new String(getData());
    }
}
