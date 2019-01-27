package cs455.wireformats;

import cs455.util.Utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterResponse implements Message {
    private int status;
    private String info;

    private RegisterResponse(int status, String info) {
        this.status = status;
        this.info = info;
    }

    public static RegisterResponse of(int status, String info) {
        return new RegisterResponse(status, info);
    }

    @Override
    public int getProtocol() {
        return Protocol.REGISTER_RESPONSE;
    }

    @Override
    public byte[] getBytes() throws IOException {
        /**
         * Message Type (int): REGISTER_RESPONSE
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

    @Override
    public String toString() {
        return "RegisterResponse{" +
            "status=" + status +
            ", info='" + info + '\'' +
            '}';
    }
}
