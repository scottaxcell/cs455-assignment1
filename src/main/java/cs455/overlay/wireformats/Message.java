package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Message implements Event {
    private int payload;
    private String destination;

    private Message(int payload, String destination) {
        this.payload = payload;
        this.destination = destination;
    }

    public static Message of(int payload, String randomNode) {
        return new Message(payload, randomNode);
    }

    public int getPayload() {
        return payload;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public int getProtocol() {
        return Protocol.MESSAGE;
    }

    @Override
    public byte[] getBytes() throws IOException {
        /**
         * Event Type (int): MESSAGE
         * Payload (int)
         * Destination node (String)
         */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(getProtocol());
        dataOutputStream.writeInt(payload);
        dataOutputStream.writeInt(destination.length());
        dataOutputStream.write(destination.getBytes());
        dataOutputStream.flush();

        byte[] data = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return data;
    }

    @Override
    public String toString() {
        return "Message{" +
            "payload=" + payload +
            ", destination='" + destination + '\'' +
            '}';
    }
}
