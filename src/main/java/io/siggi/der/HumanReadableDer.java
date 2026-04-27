package io.siggi.der;

import io.siggi.tools.Hex;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class HumanReadableDer {
    private static final Map<String, Function<String, Der>> defaultDeserializers = new HashMap<>();

    static {
        defaultDeserializers.put("boolean", string -> {
            string = string.trim();
            if (string.equalsIgnoreCase("true")) return Der01Boolean.TRUE;
            if (string.equalsIgnoreCase("false")) return Der01Boolean.FALSE;
            throw new IllegalArgumentException("Invalid boolean value");
        });
        defaultDeserializers.put("integer", string -> {
            if (string.startsWith("0x")) return new Der02Integer(new BigInteger(string.substring(2).trim(), 16));
            if (string.startsWith("-0x")) return new Der02Integer(new BigInteger("-" + string.substring(3).trim(), 16));
            return new Der02Integer(new BigInteger(string.trim()));
        });
        defaultDeserializers.put("bitstring", string -> new Der03BitString(Hex.toBytes(string)));
        defaultDeserializers.put("octetstring", string -> new Der04OctetString(Hex.toBytes(string)));
        defaultDeserializers.put("null", string -> Der05Null.INSTANCE);
        defaultDeserializers.put("oid", Der06OID::new);
        defaultDeserializers.put("utf8string", string -> new Der12UTF8String(string.getBytes(StandardCharsets.UTF_8)));
        defaultDeserializers.put("utf8stringhex", string -> new Der12UTF8String(Hex.toBytes(string)));
        defaultDeserializers.put("numericstring", string -> new Der18NumericString(string.getBytes(StandardCharsets.UTF_8)));
        defaultDeserializers.put("printablestring", string -> new Der19PrintableString(string.getBytes(StandardCharsets.UTF_8)));
        defaultDeserializers.put("ia5string", string -> new Der22IA5String(string.getBytes(StandardCharsets.UTF_8)));
        defaultDeserializers.put("ia5stringhex", string -> new Der22IA5String(Hex.toBytes(string)));
        defaultDeserializers.put("utctime", string -> new Der23UTCTime(string.getBytes(StandardCharsets.UTF_8)));
        defaultDeserializers.put("generalizedtime", string -> new Der24GeneralizedTime(string.getBytes(StandardCharsets.UTF_8)));
    }

    private final Map<String, Function<String, Der>> deserializers = new HashMap<>(defaultDeserializers);

    public HumanReadableDer() {
    }

    public void registerDeserializer(String type, Function<String, Der> deserializer) {
        deserializers.put(type.toLowerCase(), deserializer);
    }

    public void print(Der der, PrintStream stream) {
        print(der, stream, "");
    }

    private void print(Der der, PrintStream stream, String tab) {
        if (der instanceof DerSequence) {
            DerSequence sequence = (DerSequence) der;
            String typeComment = getTypeComment(sequence.getTag());
            stream.println(tab + "Sequence-" + sequence.getTag().toString() + ":" + (typeComment == null ? "" : (" " + typeComment)));
            for (Der subDer : sequence.getItems()) {
                print(subDer, stream, tab + "  ");
            }
            return;
        }
        stream.println(tab + der);
    }

    protected String getTypeComment(Der.Tag tag) {
        if (tag.derClass == Der.DerClass.UNIVERSAL && !tag.constructed) {
            switch (tag.type) {
                case 0x3:
                    return "(BitString)";
                case 0x4:
                    return "(OctetString)";
            }
        }
        return null;
    }

    public Der parse(BufferedReader reader) throws Exception {
        return parse(reader, "");
    }

    private Der parse(BufferedReader reader, String tab) throws Exception {
        reader.mark(16384);
        String line;
        do {
            line = reader.readLine();
            if (line == null) return null;
            String trimmed = line.trim();
            if (!trimmed.startsWith("#") && !trimmed.isEmpty()) break;
        } while (true);
        if (!line.startsWith(tab)) {
            reader.reset();
            return null;
        }
        reader.mark(0);
        line = line.substring(tab.length());
        if (line.startsWith("Sequence-")) {
            int colonPos = line.indexOf(":");
            if (colonPos != -1) line = line.substring(0, colonPos);
            Der.Tag tag = Der.Tag.fromString(line.substring(9));
            reader.mark(16384);
            String nextLine = reader.readLine();
            reader.reset();
            String sequenceTab = getIndentation(nextLine);
            DerSequence sequence = new DerSequence(tag);
            if (sequenceTab.length() <= tab.length()) return sequence;
            Der subDer;
            while ((subDer = parse(reader, sequenceTab)) != null) {
                sequence.getItems().add(subDer);
            }
            return sequence;
        }
        int colonPos = line.indexOf(":");
        String typeString = line.substring(0, colonPos).trim();
        String dataString = line.substring(colonPos + 1);
        if (typeString.startsWith("Hex-")) {
            Der.Tag tag = Der.Tag.fromString(typeString.substring(4));
            return new Der(tag, Hex.toBytes(dataString));
        } else if (typeString.startsWith("String-")) {
            Der.Tag tag = Der.Tag.fromString(typeString.substring(7));
            return new Der(tag, dataString.getBytes(StandardCharsets.UTF_8));
        }
        Function<String, Der> deserializer = deserializers.get(typeString.toLowerCase());
        return deserializer.apply(dataString);
    }

    private String getIndentation(String line) {
        int i = 0;
        while (i < line.length() && line.charAt(i) == ' ') i++;
        return line.substring(0, i);
    }
}
