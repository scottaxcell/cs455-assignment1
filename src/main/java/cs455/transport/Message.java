package cs455.transport;

import java.io.IOException;

public interface Message {
    int getProtocol();

    byte[] getBytes() throws IOException;
}
