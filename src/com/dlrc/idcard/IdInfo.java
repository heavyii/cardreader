package com.dlrc.idcard;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.Arrays;

/**
 * Created by Administrator on 2016/5/1.
 */
public class IdInfo {

    private String name;
    private String sex;
    private String nation;
    private String birthday;
    private String address;
    private String ID;
    private String issue;
    private String beginTime;
    private String endTime;

    private static final String CHARTSETNAME = "utf-16le";

    /**
     * data[16:256] 文字信息
     * @param data
     */

    public IdInfo(byte[] data) {
        byte[] textData = Arrays.copyOfRange(data, 14, 256);
        ByteInputStream in = new ByteInputStream(textData, 0, textData.length);

        try {
            byte[] buf;

            //姓名
            buf = new byte[30];
            in.read(buf);
            name = new String(buf, CHARTSETNAME).trim();

            //姓别
            buf = new byte[2];
            in.read(buf);
            sex = parseShortString(buf);
            if (sex.equals("1"))
                sex = "男";
            else
                sex = "女";

            //民族
            buf = new byte[4];
            in.read(buf);
            nation = NationCode.getName(parseShortString(buf));

            //出生
            buf = new byte[16];
            in.read(buf);
            birthday = parseShortString(buf);

            //住址
            buf = new byte[70];
            in.read(buf);
            address = new String(buf, CHARTSETNAME).trim();

            //身份证号码
            buf = new byte[36];
            in.read(buf);
            ID = parseShortString(buf);

            //签发机关
            buf = new byte[30];
            in.read(buf);
            issue = new String(buf, CHARTSETNAME).trim();

            //有效日期起始
            buf = new byte[16];
            in.read(buf);
            beginTime = parseShortString(buf);

            //有效日期截止
            buf = new byte[16];
            in.read(buf);
            endTime = parseShortString(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String parseShortString(byte[] buf) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length/2; i++)
            sb.append((char)buf[i*2]);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "IdInfo{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", nation='" + nation + '\'' +
                ", birthday='" + birthday + '\'' +
                ", address='" + address + '\'' +
                ", ID='" + ID + '\'' +
                ", issue='" + issue + '\'' +
                ", beginTime='" + beginTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}
