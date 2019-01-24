package cs455.overlay.node;

import cs455.overlay.messages.Message;

public interface Node {
    void onMessage(Message message);
}
