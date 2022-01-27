package com.ss.ftpClient.enums;

public enum Structure {
    FILE("F",0),RECORD("R",1),PAGE("P",2);

    private String abbr;
    private int index;
    private Structure(String abbr,int index){
        this.abbr =abbr;
        this.index = index;
    }

    public String getAbbr() {
        return abbr;
    }

    public int getIndex() {
        return index;
    }

    public static Structure getStru(int index) {
        for (Structure s : Structure.values()) {
            if (s.getIndex() == index) {
                return s;
            }
        }
        return null;
    }
}
