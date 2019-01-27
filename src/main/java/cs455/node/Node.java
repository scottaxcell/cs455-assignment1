package cs455.node;

import cs455.wireformats.Message;

public interface Node {
    void onMessage(Message message);
}
