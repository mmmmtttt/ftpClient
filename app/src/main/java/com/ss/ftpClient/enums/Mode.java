package com.ss.ftpClient.enums;

public enum Mode {
    STREAM("S", 0), BLOCK("B", 1), COMPRESSED("C", 2);
    private String abbr;
    private int index;

    private Mode(String abbr, int index) {
        this.abbr = abbr;
        this.index = index;
    }

    public String getAbbr() {
        return abbr;
    }

    public int getIndex() {
        return index;
    }

    public static Mode getMode(int index) {
        for (Mode m : Mode.values()) {
            if (m.getIndex() == index) {
                return m;
            }
        }
        return null;
    }
}
