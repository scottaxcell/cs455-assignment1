package cs455.node;

import cs455.wireformats.Event;

public interface Node {
    void onEvent(Event event);
}
