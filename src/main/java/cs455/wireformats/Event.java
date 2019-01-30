package cs455.wireformats;

import java.io.IOException;

public interface Event {
    int getProtocol();

    byte[] getBytes() throws IOException;
}
