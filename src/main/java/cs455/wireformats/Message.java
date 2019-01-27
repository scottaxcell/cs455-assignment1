package cs455.wireformats;

import java.io.IOException;

public interface Message {
    int getProtocol();

    byte[] getBytes() throws IOException;
}
