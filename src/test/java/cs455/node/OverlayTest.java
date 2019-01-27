package cs455.node;

import cs455.util.Overlay;
import org.junit.jupiter.api.Assertions;

import java.util.stream.Collectors;
import java.util.stream.Stream;

class OverlayTest {
    private static Overlay overlay;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @org.junit.jupiter.api.Test
    void testSetup() {
        String[] nodes = new String[]{"host0:0", "host1:1", "host2:2", "host3:3", "host4:4",
            "host5:5", "host6:6", "host7:7", "host8:8", "host9:9"};

        for (int i = 0; i < 10000; i++) {
            for (int j = 1; j < 10; j++) {
                overlay = Overlay.of(Stream.of(nodes).collect(Collectors.toSet()), j);
                Assertions.assertTrue(overlay.setup());
            }
        }
    }
}