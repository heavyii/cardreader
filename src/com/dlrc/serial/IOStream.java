package com.dlrc.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IOStream {

    InputStream getInputStream();

    OutputStream getOutputStream();

}
