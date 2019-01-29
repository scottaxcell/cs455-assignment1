package cs455.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class TaskInitiate implements Message {
    private int numRounds;

    public TaskInitiate(int numRounds) {
        this.numRounds = numRounds;
    }

    public static TaskInitiate of(int numRounds) {
        return new TaskInitiate(numRounds);
    }

    public int getNumRounds() {
        return numRounds;
    }

    @Override
    public int getProtocol() {
        return Protocol.TASK_INITIATE;
    }

    @Override
    public byte[] getBytes() throws IOException {
        /**
         * Message Type: TASK_INITIATE
         * Rounds: X
         */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(getProtocol());
        dataOutputStream.writeInt(numRounds);
        dataOutputStream.flush();

        byte[] data = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return data;
    }

    @Override
    public String toString() {
        return "TaskInitiate{" +
            "numRounds=" + numRounds +
            '}';
    }
}
