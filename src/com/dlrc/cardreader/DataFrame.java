package com.dlrc.cardreader;

import com.dlrc.common.Utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * ATS522读卡器模块串口通信协议
 */
public class DataFrame {

    public byte frameLength; //数据长度
    public byte cmdType; //命令类型
    public byte cmd; //命令
    public byte InfoLength; //信息长度
    public byte[] info; //信息
    public byte bcc; //校验
    public static final byte etx = 0x03; //结束

    private final int PKGLEN = 6;

    /**
     * 读取一个数据帧
     * @param in
     * @param timeout
     * @return
     */
    public boolean read(InputStream in, long timeout) {
        int frameLength = 0;
        byte[] buffer = null;
        int i = 0;
        int counter = 0;
        long beginTime = System.currentTimeMillis();

        while (frameLength == 0 || i < frameLength) {
            try {
                int d = in.read();
                if (d >= 0) {
                    if (frameLength == 0) {
                        frameLength = d;
                        buffer = new byte[frameLength];
                    }

                    if (i < frameLength)
                        buffer[i++] = (byte) d;
                }

            } catch (IOException e) {
                return false;
            }

            //超时
            if (counter++ > frameLength) {
                if (System.currentTimeMillis() > beginTime + timeout)
                    return false;
            }
        }

        return unPack(buffer);
    }

    /**
     * 数据解包，计算bcc校验
     * @param data 接收到的数据
     * @return bcc校验通过，返回true，否则false
     */
    private boolean unPack(byte[] data) {

        if (data == null)
            return false;

        int i = 0;
        frameLength = data[i++];
        cmdType = data[i++];
        cmd = data[i++];
        InfoLength = data[i++];
        info = null;
        if (InfoLength > 0) {
            info = new byte[InfoLength];
            for (int j = 0; j < InfoLength; j++)
                info[j] = data[i++];
        }

        byte bbc = data[i++];

        calculateBCC(data);

        if (this.bcc == bbc)
            return true;

        return false;
    }

    /**
     * 计算数据包长度
     * @return
     */
    private int calculateFrameLength() {
        int len = PKGLEN + InfoLength;
        frameLength = (byte)len;
        return len;
    }

    /**
     * 计算校验和
     * @param data 计算校验和data
     * @return
     */
    private void calculateBCC(byte[] data) {
        int len = data.length;
        byte bcc = 0x00;
        for (int i = 0; i < len - 2; i++) {
            bcc ^= data[i];
        }
        bcc ^= 0xFF;

        this.bcc = bcc;
        data[data.length-2] = bcc;
    }

    /**
     * 打包数据用于发送
     * @param seq 通信序号
     * @param cmdType
     * @param cmd
     * @param info
     * @return
     */
    public byte[] pack(int seq, byte cmdType, byte cmd, byte[] info) {
        cmdType &= 0x0F;
        cmdType = (byte)((seq << 4) | cmdType);
        return pack(cmdType, cmd, info);
    }

    public byte[] pack(byte cmdType, byte cmd, byte[] info) {
        this.cmdType = cmdType;
        this.cmd = cmd;
        this.info = info;

        if (info == null) {
            this.InfoLength = 0;
        } else {
            this.InfoLength = (byte)info.length;
        }

        return pack();
    }

    /**
     * 制作数据包
     * @return
     */
    private byte[] pack() {
        int len = calculateFrameLength();
        byte[] data = new byte[len];
        int i = 0;
        data[i++] = frameLength;
        data[i++] = cmdType;
        data[i++] = cmd;
        data[i++] = InfoLength;

        for (int j = 0 ; j < InfoLength; j++)
            data[i++] = info[j];

        data[i+1] = this.etx;
        calculateBCC(data);

        return data;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("DataFrame#\n");
        sb.append("\tframeLength: " + String.format("%02X", frameLength));
        sb.append("\tcmdType: " + String.format("%02X", cmdType));
        sb.append("\tcmd: " + String.format("%02X", cmd));
        sb.append("\tinfoLength: " + String.format("%02X", InfoLength));
        sb.append("\tinfo: " + Utils.ByteArrayToHexString(info));
        sb.append("\tbcc: " + String.format("%02X", bcc));
        sb.append("\tetx: " + String.format("%02X", etx));
        return sb.toString();
    }
}
