import com.dlrc.cardreader.CardMifare1;
import com.dlrc.cardreader.DataFrame;
import com.dlrc.common.Utils;

import com.dlrc.idcard.IdInfo;
import com.dlrc.idcard.IdReader;
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

            comPort.connect("COM7", 115200);
            IOStream ioStream = comPort;
            OutputStream out = ioStream.getOutputStream();
            InputStream in = ioStream.getInputStream();

            for (int i = 0; i < 3; i++){
                IdReader idReader = new IdReader(in, out);

                IdInfo idInfo = null;
                while (idInfo == null) {
                    idInfo  = idReader.readIdInfo();
                }
                System.out.println("身份证：" + idInfo);
            }

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
