package io.siggi.der;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class DerSequence extends Der {
    private static final Tag BITSTREAM = new Tag(DerClass.UNIVERSAL, false, 0x3);

    private final List<Der> items;

    public DerSequence(Tag tag, DerReader reader, byte[] data) {
        this(tag);
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            Der item;
            while ((item = reader.read(in)) != null) {
                items.add(item);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public DerSequence(Tag tag) {
        this(tag, new ArrayList<>());
    }

    public DerSequence(Tag tag, List<Der> items) {
        super(tag, EMPTY);
        if (items == null) throw new NullPointerException("items");
        this.items = items;
    }

    public List<Der> getItems() {
        return items;
    }

    public byte[] getData() {
        int length = 0;
        List<byte[]> datas = new ArrayList<>();
        for (Der item : items) {
            byte[] d = item.encode();
            length += d.length;
            datas.add(d);
        }
        byte[] output;
        int offset;
        if (tag.equals(BITSTREAM)) {
            output = new byte[length + 1];
            output[0] = (byte) 0;
            offset = 1;
        } else {
            output = new byte[length];
            offset = 0;
        }
        for (byte[] d : datas) {
            System.arraycopy(d, 0, output, offset, d.length);
            offset += d.length;
        }
        return output;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof DerSequence) {
            DerSequence o = (DerSequence) other;
            if (getItems().size() != o.getItems().size()) return false;
        }
        return super.equals(other);
    }
}
