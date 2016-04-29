import com.dlrc.cardreader.CardMifare1;
import com.dlrc.cardreader.DataFrame;
import com.dlrc.common.Utils;

import com.dlrc.serial.IOStream;
import com.dlrc.serial.TwoWaySerialComm;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Main {


    public static void main(String[] args) {
        System.out.println("Hello World!");


        TwoWaySerialComm comPort = new TwoWaySerialComm();
        try
        {

            comPort.connect("COM4");

            IOStream ioStream = comPort;
            OutputStream out = ioStream.getOutputStream();
            InputStream in = ioStream.getInputStream();


            CardMifare1 card = new CardMifare1(in, out);
            if (card.initCard()) {
                for (int i = 0; i < 3; i++) {
                    byte[] data = card.read(i);
                    System.out.println("read block: " + i + "# " + Utils.ByteArrayToHexString(data));
                }
            }
            card.halt();

            comPort.close();
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            comPort.close();
        }


    }
}
