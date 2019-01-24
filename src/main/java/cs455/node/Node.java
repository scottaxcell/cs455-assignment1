package cs455.node;

import cs455.transport.Message;

public interface Node {
    void onMessage(Message message);
}
