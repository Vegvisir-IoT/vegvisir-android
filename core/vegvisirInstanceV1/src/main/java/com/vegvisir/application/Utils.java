package com.vegvisir.application;

public class Utils {


    /**
     * UTF-8 string to a hex decimal string. A 1 byte 8 bits char is converted to a 2 bytes hex
     * decimal representation. For instance, 'A' [65 = b01000001 = 0x41] => '41' [0x41].
     * @param data
     * @return
     */
    public static String str2Hex(String data) {
        String ret = "";
        for (char i : data.toCharArray()) {
            ret += Integer.toHexString(i);
        }
        return ret;
    }

    public static String hex2str(String hex) {
        String ret = "";
        char t;
        for (int i =0; i+1 < hex.length(); ) {
            t = (char)Integer.valueOf(hex.substring(i, i+2), 16).intValue();
            ret += t;
            i += 2;
        }
        return ret;
    }
}
