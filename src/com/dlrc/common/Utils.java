package com.dlrc.common;

/**
 * Created by Administrator on 2016/4/29.
 */
public class Utils {

    /**
     * Utility method to convert a byte array to a hexadecimal string.
     *
     * @param bytes
     *            Bytes to convert
     * @return String, containing hexadecimal representation.
     */
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };

        if (bytes == null)
            return null;

        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex

        // characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned
            // value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from
            // upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character
            // from lower nibble
        }
        return new String(hexChars);
    }

    static public String Byte2Hex(byte[] input) {
        return Byte2Hex(input, " ");
    }

    static public String Byte2Hex(byte[] input, String space) {
        StringBuilder result = new StringBuilder();

        for (byte inputbyte : input) {
            result.append(String.format("%02X" + space, inputbyte));
        }
        return result.toString();
    }

    static public byte[] HexStringToByteArray(String hexstr) {
        String s = hexstr.replaceAll(" ", "");
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException(
                    "Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift
            // into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static boolean BytesEqual(byte[] array1, byte[] array2, int length) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length < length || array2.length < length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

}
