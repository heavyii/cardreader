package com.dlrc.idcard;

import com.dlrc.common.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by Administrator on 2016/5/1.
 */
public class IdReader {

    private byte[] CMD_FIND = Utils.HexStringToByteArray("AA AA AA 96 69 00 03 20 01 22");
    private byte[] CMD_FIND_SUCC = Utils.HexStringToByteArray("AA AA AA 96 69 00 08 00 00 9F 00 00 00 00 97");
    private byte[] CMD_FIND_FAIL = Utils.HexStringToByteArray("AA AA AA 96 69 00 04 00 00 80 84");

    private byte[] CMD_SELECT = Utils.HexStringToByteArray("AA AA AA 96 69 00 03 20 02 21");
    private byte[] CMD_SELECT_SUCC = Utils.HexStringToByteArray("AA AA AA 96 69 00 0C 00 00 90 00 00 00 00 00 00 00 00 9C");
    private byte[] CMD_SELECT_FAIL = Utils.HexStringToByteArray("AA AA AA 96 69 00 04 00 00 81 85");

    private byte[] CMD_READ = Utils.HexStringToByteArray("AA AA AA 96 69 00 03 30 01 32");
    //返回成功：返回1295字节数据
    private byte[] CMD_READ_FAIL = Utils.HexStringToByteArray("AA AA AA 96 69 00 04 00 00 41 45");

    private final int READ_TIMEOUT = 500;
    private InputStream in;
    private OutputStream out;

    public IdReader(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    public byte[] recvPackage() {
        return recvPackage(READ_TIMEOUT);
    }

    /**
     * 读取一个数据帧
     */
    public byte[] recvPackage(int timeout) {

        int counter = 0;
        long beginTime = System.currentTimeMillis();

        int i = 0;
        byte[] buf = new byte[2048];
        int dataLen = 0;
        final int HEADLEN = 7;
        for (i = 0; i < buf.length; ) {
            try {
                int d = -1;
                if (in.available() > 0)
                    d = in.read();
                if (d >= 0) {
                    buf[i++] = (byte) d;

                    //前三个开始字符是AA AA AA
                    if (i < 3 && d != 0xAA)
                        i = 0;
                }

                if (i == HEADLEN) {
                    dataLen = buf[5] << 8 | buf[6];
                }

                if (i >= dataLen + HEADLEN) {
                    return Arrays.copyOfRange(buf, 0, dataLen + HEADLEN);
                }

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            //超时
            if (counter++ > 1024) {
                if (System.currentTimeMillis() > beginTime + timeout) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * 发送一个命令，成功才返回数据，失败返回null
     * @param cmd
     * @return
     */
    private byte[] sendCMD(byte[] cmd) {
        try {
            out.write(cmd);
            byte[] data = recvPackage();
            //失败返回的数据包是11字节
            if (data != null && data.length > 11) {
                System.out.println(Utils.ByteArrayToHexString(data));
                return data;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取身份证信息
     * @return
     */
    public IdInfo readIdInfo() {
        try {
            byte[] buf = new byte[1024];
            int readLen;

            byte[] rdata;
            if (sendCMD(CMD_FIND) == null)
                return null;

            if (sendCMD(CMD_SELECT) == null)
                return null;

            int counter = 5;
            while (counter-- > 0) {
                rdata = sendCMD(CMD_READ);
                if (rdata != null && rdata[0] != 0x00) {
                    return new IdInfo(rdata);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
