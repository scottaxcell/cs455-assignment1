package cs455.util;

import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

public class OverlayCreatorTest {

    @Test
    public void testSetup() {
        String[] nodes = new String[]{"host0:0", "host1:1", "host2:2", "host3:3", "host4:4",
            "host5:5", "host6:6", "host7:7", "host8:8", "host9:9"};

        for (int i = 0; i < 10000; i++) {
            for (int j = 1; j < 10; j++) {
                OverlayCreator overlayCreator = OverlayCreator.of(Stream.of(nodes).collect(Collectors.toSet()), j);
                assertTrue(overlayCreator.setup());
            }
        }
    }
}