package cs455.node;

import static org.junit.jupiter.api.Assertions.*;

class OverlayTest {
    private static Overlay overlay;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        overlay = Overlay.of();
    }

    @org.junit.jupiter.api.Test
    void testSetup() {
        String[] nodes = new String[]{"host1:123", "host2:23983", "host1:2345", "host0:45381", "host54:6456",
            "host1:88", "host2:10873", "host1:55555", "host0:4921", "host4:3356"};

        overlay.setup(nodes, 4);
    }
}