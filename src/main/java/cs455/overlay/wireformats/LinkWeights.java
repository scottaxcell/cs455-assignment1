package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class LinkWeights implements Event {
    private String[] links;

    private LinkWeights(String[] links) {
        this.links = links;
    }

    public static LinkWeights of(String[] links) {
        return new LinkWeights(links);
    }

    @Override
    public int getProtocol() {
        return Protocol.LINK_WEIGHTS;
    }

    @Override
    public byte[] getBytes() throws IOException {
        /**
         * Event Type: Link_Weights
         * Number of links: L
         * Linkinfo1
         * Linkinfo2
         * ...
         * LinkinfoL
         *
         * where LinkinfoL: hostnameA:portnumA hostnameB:portnumB weight
         */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(getProtocol());
        dataOutputStream.writeInt(links.length);
        for (String link : links) {
            dataOutputStream.writeInt(link.length());
            dataOutputStream.write(link.getBytes());
        }
        dataOutputStream.flush();

        byte[] data = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return data;
    }

    public String[] getLinks() {
        return links;
    }

    @Override
    public String toString() {
        return "LinkWeights{" +
            "links=" + Arrays.toString(links) +
            '}';
    }
}
