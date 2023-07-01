package org.nicbrerod.scripts.manager.distributed.utils.communication;

import java.util.UUID;
import java.util.function.Consumer;

import org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg.CommInterfaceMessage;

/**
 * Class used by algorithms to send broadcast and direct messages and also to define listeners for
 * incoming messages. All messages sent and received by this class are children of type {@link CommInterfaceMessafe}
 * 
 */
public interface CommInterface {
    
    /**
     * Sends a message to all nodes in a cluster
     * @param message Message to be sent
     */
    void sendBroadcast(CommInterfaceMessage message);

    /**
     * Sends a message only a node, represented by its id
     * @param message Message to be sent
     * @param recipient Id of node to send the message
     */
    void sendMessage(CommInterfaceMessage message, UUID recipient);

    /**
     * Configures how the comm interface must to process an incoming message
     * @param consumer Consumer object to process incoming messages
     */
    void configureMessageProcessing(Consumer<CommInterfaceMessage> consumer);

    /**
     * Operations to execute when algorithm wants to stop using comm interface. This method is 
     * usefull when your implementation have some asynchronous elements, like threads, 
     * thread executors, listeners,... and you want to force kill them before comm interface 
     * stopping
     */
    void onStop();
}
