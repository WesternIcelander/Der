package io.siggi.der;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DerReader {

    private static final Map<Der.Tag, Deserializer> defaultDeserializers = new HashMap<>();

    static {
        defaultDeserializers.put(new Der.Tag(Der.DerClass.UNIVERSAL, false, 0x1), (data) -> {
            if (data[0] == 0x00 && data.length == 1) {
                return Der01Boolean.FALSE;
            } else if ((data[0] & 0xff) == 0xff && data.length == 1) {
                return Der01Boolean.TRUE;
            } else {
                throw new IllegalArgumentException("Invalid boolean value");
            }
        });
        defaultDeserializers.put(new Der.Tag(Der.DerClass.UNIVERSAL, false, 0x2), Der02Integer::new);
        defaultDeserializers.put(new Der.Tag(Der.DerClass.UNIVERSAL, false, 0x3), Der03BitString::new);
        defaultDeserializers.put(new Der.Tag(Der.DerClass.UNIVERSAL, false, 0x4), Der04OctetString::new);
        defaultDeserializers.put(new Der.Tag(Der.DerClass.UNIVERSAL, false, 0x5), (data) -> {
            if (data.length != 0) throw new IllegalArgumentException("Invalid null value");
            return Der05Null.INSTANCE;
        });
        defaultDeserializers.put(new Der.Tag(Der.DerClass.UNIVERSAL, false, 0x6), Der06OID::new);
        defaultDeserializers.put(new Der.Tag(Der.DerClass.UNIVERSAL, false, 0xC), Der12UTF8String::new);
        defaultDeserializers.put(new Der.Tag(Der.DerClass.UNIVERSAL, false, 0x12), Der18NumericString::new);
        defaultDeserializers.put(new Der.Tag(Der.DerClass.UNIVERSAL, false, 0x13), Der19PrintableString::new);
        defaultDeserializers.put(new Der.Tag(Der.DerClass.UNIVERSAL, false, 0x16), Der22IA5String::new);
        defaultDeserializers.put(new Der.Tag(Der.DerClass.UNIVERSAL, false, 0x17), Der23UTCTime::new);
        defaultDeserializers.put(new Der.Tag(Der.DerClass.UNIVERSAL, false, 0x18), Der24GeneralizedTime::new);
    }

    private final Map<Der.Tag, Deserializer> deserializers = new HashMap<>(defaultDeserializers);

    public Der read(InputStream in) throws IOException {
        Der.Tag tag = Der.Tag.read(in);
        if (tag == null) return null;
        int length = in.read();
        if (length == -1) throw new EOFException();
        if (length >= 0x80L) {
            int count = (length & 0x7f);
            length = 0;
            for (int i = 0; i < count; i++) {
                int v = in.read();
                if (v == -1) throw new EOFException();
                length <<= 8;
                length |= v;
            }
        }
        byte[] data = new byte[length];
        int read = 0;
        while (read < data.length) {
            int c = in.read(data, read, data.length - read);
            if (c == -1) throw new EOFException();
            read += c;
        }
        Der der = createDer(tag, data);
        boolean attemptDecodeAsSequence = der.getTag().constructed;
        if (der instanceof Der03BitString) {
            Der03BitString bitString = (Der03BitString) der;
            if (bitString.getUnusedBits() == 0) {
                data = bitString.getBytes();
                attemptDecodeAsSequence = true;
            }
        } else if (der instanceof Der04OctetString) {
            attemptDecodeAsSequence = true;
        }
        if (attemptDecodeAsSequence) {
            try {
                der = new DerSequence(tag, this, data);
            } catch (Exception ignored) {
            }
        }
        return der;
    }

    public void registerDeserializer(Der.Tag tag, Deserializer deserializer) {
        deserializers.put(tag, deserializer);
    }

    private Der createDer(Der.Tag tag, byte[] data) {
        Deserializer deserializer = deserializers.get(tag);
        try {
            if (deserializer != null) return deserializer.deserialize(data);
        } catch (Exception ignored) {
        }
        return new Der(tag, data);
    }

    @FunctionalInterface
    public interface Deserializer {
        Der deserialize(byte[] data);
    }
}
