package io.siggi.der;

import java.nio.charset.StandardCharsets;

public final class Der23UTCTime extends Der {
    private static final Tag TAG = new Tag(DerClass.UNIVERSAL, false, 0x17);
    private static final int[] MAX_DAY = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private final String value;

    public Der23UTCTime(int year, int month, int day, int hour, int minute, int second) {
        this(s(year) + s(month) + s(day) + s(hour) + s(minute) + s(second) + "Z");
    }

    public Der23UTCTime(byte[] data) {
        this(new String(data, StandardCharsets.UTF_8));
    }

    public Der23UTCTime(String value) {
        super(TAG, EMPTY);
        this.value = value;
        if (!validate()) {
            throw new IllegalArgumentException("Invalid UTCTime value");
        }
    }

    private static String s(int value) {
        if (value < 0) throw new IllegalArgumentException("Invalid UTCTime value");
        if (value < 10) return "0" + value;
        if (value > 99) throw new IllegalArgumentException("Invalid UTCTime value");
        return "" + value;
    }

    private boolean validate() {
        if (value.length() != 13) return false;
        try {
            int year = Integer.parseInt(value.substring(0, 2));
            int month = Integer.parseInt(value.substring(2, 4));
            int day = Integer.parseInt(value.substring(4, 6));
            int hour = Integer.parseInt(value.substring(6, 8));
            int minute = Integer.parseInt(value.substring(8, 10));
            int second = Integer.parseInt(value.substring(10, 12));
            if (value.charAt(12) != 'Z') return false;
            if (year < 0
                    || month < 1 || month > 12
                    || day < 1
                    || hour < 0 || hour >= 24
                    || minute < 0 || minute >= 60
                    || second < 0 || second >= 60
            ) return false;
            int maxDay;
            if (month == 2) {
                boolean leap = year % 4 == 0;
                maxDay = leap ? 29 : 28;
            } else {
                maxDay = MAX_DAY[month - 1];
            }
            return day <= maxDay;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public byte[] getData() {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "UTCTime:" + value;
    }
}
