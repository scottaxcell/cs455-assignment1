package cs455.overlay.messages;

import java.io.IOException;

public interface Message {
    int getProtocol();

    byte[] getBytes() throws IOException;
}
