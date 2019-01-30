package cs455.wireformats;

import cs455.util.Utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DeregisterResponse implements Event {
    private int status;
    private String info;

    private DeregisterResponse(int status, String info) {
        this.status = status;
        this.info = info;
    }

    public static DeregisterResponse of(int status, String info) {
        return new DeregisterResponse(status, info);
    }

    @Override
    public String toString() {
        return "DeregisterResponse{" +
            "status=" + status +
            ", info='" + info + '\'' +
            '}';
    }

    public int getStatus() {
        return status;
    }

    @Override
    public int getProtocol() {
        return Protocol.DEREGISTER_RESPONSE;
    }

    @Override
    public byte[] getBytes() throws IOException {
        /**
         * Event Type (int): DEREGISTER_RESPONSE
         * Status Code (byte): SUCCESS or FAILURE
         * Additional Info (String):
         */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(getProtocol());
        dataOutputStream.write(Utils.intToByte(status));
        dataOutputStream.write(info.getBytes());
        dataOutputStream.flush();

        byte[] data = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return data;
    }

}
