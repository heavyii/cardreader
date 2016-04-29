package com.dlrc.cardreader;

import com.dlrc.common.Utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * 使用例子：
 * 先执行initCard()完成初始化，然后调用read()&write()读写数据块，最后调用halt()结束。
 */
public class CardMifare1 {

    //防碰撞等级
    private static final byte ANTICOLL_LEVEL1 = (byte) 0x93;
    private static final byte ANTICOLL_LEVEL2 = (byte) 0x95;
    private static final byte ANTICOLL_LEVEL3 = (byte) 0x97;

    private static final byte ISO14443A_TYPE = 0x02;
    private static final byte CMD_REQUEST = 'A';
    private static final byte CMD_ANTICOLL = 'B';
    private static final byte CMD_SELECT = 'C';
    private static final byte CMD_HALT = 'D';
    private static final byte CMD_AUTHKEY = 'F';
    private static final byte CMD_READ = 'G';
    private static final byte CMD_WRITE = 'H';

    //密码验证
    private static final byte AUTHKEY_TYPE_A = 0x60;
    private static final byte AUTHKEY_TYPE_B = 0x61;
    private static final byte[] AUTHKEY_A = new byte[]{(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
    private static final byte[] AUTHKEY_B = new byte[]{(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};

    private InputStream in;
    private OutputStream out;
    private DataFrame sendDataFrame;
    private DataFrame recvDataFrame;

    private byte[] UID = null;
    private byte antiCollLevel = ANTICOLL_LEVEL1;

    CardType cardType;

    private int seq = 0;

    protected CardMifare1() {};

    public CardMifare1(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    /**
     * 初始化卡片： 准备好读写
     * @return
     */
    public boolean initCard() {
        //发起请求
        if (request() == false && request() == false) {
            return false;
        }

        //防碰撞 & 选择卡 & 验证秘钥A
        if (!anticoll() || !select() || !authKey()) {
            return false;
        }

        return true;
    }

    /**
     * 发起request, 获取卡类型
     * @return
     */
    public boolean request() {

        /**
         * 复位后卡处于IDLE 模式，用两种请
         求模式的任一种请求时，此时的卡均能回应；若对某一张卡成功进行了挂起操作（Halt命令或
         DeSelect*命令），则进入了Halt 模式，此时的卡只响应ALL（0x52）模式的请求，除非将卡离
         开天线感应区后再进入。
         */
        final byte REQUEST_MODE_IDLE = 0x26;
        final byte REQUEST_MODE_ALL = 0x52;
        byte[] info = new byte[] {REQUEST_MODE_IDLE};

        if (sendAndRecv(ISO14443A_TYPE, CMD_REQUEST, info)) {
            cardType = CardType.Mifare1S50;
            return true;
        }

        return false;
    }

    /**
     * 防碰撞
     * @return
     */
    public boolean anticoll() {

        byte[] info = new byte[] {antiCollLevel, 0x00};

        if (sendAndRecv(ISO14443A_TYPE, CMD_ANTICOLL, info)) {
            UID = recvDataFrame.info;
            return  true;
        }

        return false;
    }

    /**
     * 选择卡片
     * @return
     */
    public boolean select() {
        if (UID == null)
            return false;

        byte[] info = new byte[]{antiCollLevel, UID[0], UID[1], UID[2], UID[3]};

        if (sendAndRecv(ISO14443A_TYPE, CMD_SELECT, info)) {
            //SAK, get card type
            return  true;
        }

        return false;
    }

    /**
     * 读卡结束
     * @return
     */
    public boolean halt() {

        if (sendAndRecv(ISO14443A_TYPE, CMD_HALT, null)) {
            UID = recvDataFrame.info;
            return  true;
        }

        return false;
    }

    /**
     * 验证秘钥A，验证后就可以读写数据
     * @return
     */
    public boolean authKey() {

        byte[] info = new byte[12];

        int i = 0;
        info[i++] = AUTHKEY_TYPE_A;

        //UID
        for (int j = 0; j < 4; j++)
            info[i++] = UID[j];

        for (int j = 0; j < 6; j++)
            info[i++] = AUTHKEY_A[j];

        //选择块
        info[i++] = 1;

        if (sendAndRecv(ISO14443A_TYPE, CMD_AUTHKEY, info)) {
            if (recvDataFrame.cmd == 0x00)
                return  true;
        }

        return false;
    }

    /**
     * 读一个数据块
     * @param block 数据块0-63
     * @return 返回byte[16]， 失败返回null
     */
    public byte[] read(int block) {

        //数据块1
        byte[] info = new byte[] { (byte)block };

        if (sendAndRecv(ISO14443A_TYPE, CMD_READ, info)) {
            if (recvDataFrame.cmd == 0x00)
                return  recvDataFrame.info;
        }

        return null;
    }

    /**
     * 写一个数据块
     * @param block 数据块，0-63
     * @param data 数据，16字节
     * @return
     */
    public boolean write(int block, byte[] data) {

        byte[] info = new byte[17];

        //数据块选择
        int i = 0;
        info[i++] = (byte) block;

        //数据
        for (int j = 0; j < 16; j++) {
            info[i++] = data[j];
        }

        if (sendAndRecv(ISO14443A_TYPE, CMD_WRITE, info)) {
            if (recvDataFrame.cmd == 0x00)
                return  true;
        }

        return false;
    }

    /**
     * 发送一个数据包，并接收返回
     * @param cmdType
     * @param cmd
     * @param info
     * @return
     */
    private boolean sendAndRecv(byte cmdType, byte cmd, byte[] info) {

        sendDataFrame = new DataFrame();
        byte[] buf = sendDataFrame.pack(++seq, cmdType, cmd, info);

        try {
            out.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        recvDataFrame = new DataFrame();
        if (recvDataFrame.read(in, 500L)) {
            if (recvDataFrame.cmdType == sendDataFrame.cmdType
                    && recvDataFrame.cmd == 0x00)
                return true;
        }

        return false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CardMifare1 {\n");
        sb.append("\tcardType{" + cardType + "}\n");
        sb.append("\tsendDataFrame{" + sendDataFrame + "}\n");
        sb.append("\trecvDataFrame{" + recvDataFrame + "}\n");
        sb.append("}\n");
        return sb.toString();
    }
}
