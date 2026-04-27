package io.siggi.der;

import java.util.regex.Pattern;

public final class Der18NumericString extends Der {
    private static final Tag TAG = new Tag(DerClass.UNIVERSAL, false, 0x12);

    private static Pattern pattern = Pattern.compile("[0-9 ]*");

    public Der18NumericString(byte[] data) {
        super(TAG, data);
        if (!pattern.matcher(new String(data)).matches()) {
            throw new IllegalArgumentException("Invalid NumericString");
        }
    }

    public Der18NumericString(String string) {
        this(string.getBytes());
    }

    @Override
    public String toString() {
        return "NumericString:" + new String(getData());
    }
}
