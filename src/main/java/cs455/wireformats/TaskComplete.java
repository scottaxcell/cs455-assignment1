package cs455.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskComplete implements Event {
    private String ip;
    private int port;

    private TaskComplete(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static TaskComplete of(String ip, int port) {
        return new TaskComplete(ip, port);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int getProtocol() {
        return Protocol.TASK_COMPLETE;
    }

    @Override
    public byte[] getBytes() throws IOException {
        /**
         * Event Type: TASK_COMPLETE
         * Node IP address:
         * Node Port number:
         */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(getProtocol());
        dataOutputStream.writeInt(ip.length());
        dataOutputStream.write(ip.getBytes());
        dataOutputStream.writeInt(port);
        dataOutputStream.flush();

        byte[] data = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return data;
    }

    @Override
    public String toString() {
        return "TaskComplete{" +
            "ip='" + ip + '\'' +
            ", port=" + port +
            '}';
    }
}
