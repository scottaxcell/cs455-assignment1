package cs455.overlay.wireformats;

import java.io.IOException;

public interface Event {
    int getProtocol();

    byte[] getBytes() throws IOException;
}
