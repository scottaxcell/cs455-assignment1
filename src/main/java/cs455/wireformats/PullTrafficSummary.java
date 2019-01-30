package cs455.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PullTrafficSummary implements Event {
    public static PullTrafficSummary of() {
        return new PullTrafficSummary();
    }

    @Override
    public int getProtocol() {
        return Protocol.PULL_TRAFFIC_SUMMARY;
    }

    @Override
    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(getProtocol());
        dataOutputStream.flush();

        byte[] data = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return data;
    }

    @Override
    public String toString() {
        return "PullTrafficSummary{}";
    }
}
