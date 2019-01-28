package cs455.dijkstra;

import cs455.util.Link;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RoutingCacheTest {

    @Test
    public void testInitializeGraph() {
        List<Link> links = new ArrayList<>();
        links.add(Link.of("host0:0", "host1:0", 2));
        links.add(Link.of("host1:0", "host2:0", 4));
        links.add(Link.of("host2:0", "host3:0", 3));
        links.add(Link.of("host3:0", "host4:0", 2));
        links.add(Link.of("host4:0", "host5:0", 5));
        links.add(Link.of("host5:0", "host6:0", 2));
        links.add(Link.of("host6:0", "host7:0", 8));
        links.add(Link.of("host7:0", "host8:0", 9));
        links.add(Link.of("host8:0", "host9:0", 1));
        links.add(Link.of("host9:0", "host0:0", 1));
        RoutingCache routingCache = RoutingCache.of("host0:0", links.toArray(new Link[links.size()]));
    }

}