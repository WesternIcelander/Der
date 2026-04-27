package io.siggi.der;

import java.io.ByteArrayOutputStream;

public final class OID {
    private OID() {
    }

    public static String decode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        sb.append((data[0] & 0xff) / 40);
        sb.append(".");
        sb.append((data[0] & 0xff) % 40);
        for (int i = 1; i < data.length; i++) {
            int byteValue = data[i] & 0xff;
            if ((byteValue & 0x80) == 0) {
                sb.append(".").append(byteValue);
                continue;
            }
            i -= 1;
            int value = 0;
            do {
                i += 1;
                byteValue = data[i] & 0xff;
                value <<= 7;
                value |= byteValue & 0x7f;
            } while ((byteValue & 0x80) != 0);
            sb.append(".").append(value);
        }
        return sb.toString();
    }

    public static byte[] encode(String oid) {
        String[] pieces = oid.split("\\.");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write((Integer.parseInt(pieces[0]) * 40) + Integer.parseInt(pieces[1]));
        byte[] buffer = new byte[5];
        for (int i = 2; i < pieces.length; i++) {
            int value = Integer.parseInt(pieces[i]);
            if (value >= 0 && value < 0x80) {
                out.write(value);
                continue;
            }
            int size = 0;
            while (value != 0) {
                size += 1;
                buffer[buffer.length - size] = (byte) ((value & 0x7f) | (size == 1 ? 0x00 : 0x80));
                value >>>= 7;
            }
            out.write(buffer, buffer.length - size, size);
        }
        return out.toByteArray();
    }
}
