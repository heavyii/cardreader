package com.dlrc.idcard;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Administrator on 2016/5/1.
 */
public class IdInfo {

    private byte[] data;

    private byte[] textData;

    private String ID;

    /**
     * data[16:256] 文字信息
     * @param data
     */

    public IdInfo(byte[] data) {
        this.data = data;

        textData = Arrays.copyOfRange(data, 14, 256);
        ByteInputStream in = new ByteInputStream(textData, 0, textData.length);

        try {
            byte[] buf;

            //姓名
            buf = new byte[30];
            in.read(buf);

            //姓别
            buf = new byte[2];
            in.read(buf);

            //民族
            buf = new byte[4];
            in.read(buf);

            //出生
            buf = new byte[16];
            in.read(buf);

            //住址
            buf = new byte[70];
            in.read(buf);

            //身份证号码
            buf = new byte[36];
            in.read(buf);
            parseId(buf);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseId(byte[] idNumber) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 18; i++)
            sb.append((char)idNumber[i*2]);
        ID = sb.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("IdInfo# " + ID);
        return sb.toString();
    }
}
