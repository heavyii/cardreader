package com.dlrc.cardreader;

/**
 * Created by Administrator on 2016/4/29.
 */
public enum CardType {
    NOCARD(0x0000),
    Mifare1S50(0x0004),
    Mifare1S70(0x3300);

    private int value;
    private CardType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
//
//    Mifare1S50("Mifare1S50", 0x0004),
//    Mifare1S70("Mifare1S70", 0x3300);
//
//    private String name;
//    private int value;
//
//    public String getName() {
//        return name;
//    }
//
//    public int getValue() {
//        return value;
//    }
//
//    public boolean setValue(int value) {
//        for (CardType c : CardType.values()) {
//            if (c.getValue() == value) {
//                this.value = c.getValue();
//                this.name = c.getName();
//                return true;
//            }
//        }
//        return false;
//    }
//    private CardType(String name, int value) {
//        this.value = value;
//        this.name = name;
//    }
}
