package org.nicbrerod.scripts.manager.distributed.utils.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.jboss.logging.Logger;
import org.nicbrerod.scripts.manager.distributed.utils.communication.CommInterface;
import org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg.CommInterfaceMessage;
import org.nicbrerod.scripts.manager.distributed.utils.node.ClusterNode;

/**
 * Implementation of CommInterface used in tests. This implementation register all nodes in test to redirect messages.
 */
public class LocalCommInterface implements CommInterface {

    /**
     * Map used to register all nodes. This property needs to be static because all instances of LocalCommInterface
     * need to see this map to redirect messages
     */
    private final static Map<UUID, EventQueue<CommInterfaceMessage>> registeredNodes = new ConcurrentHashMap<>();

    /**
     * Id of node related to this instance. This node can be called "sender" of this instance
     */
    private UUID nodeId;

    private static Logger log = Logger.getLogger(CommInterface.class);

    /**
     * Registers a node to be the sender of this instance
     * @param node Node to register
     */
    public void registerNode(ClusterNode<?> node) {
        registeredNodes.put(node.getId(), new EventQueue<>(new LinkedBlockingQueue<>()));
        nodeId = node.getId();
    }

    /**
     * Sends the same message to all nodes except the one that sent it
     */
    @Override
    public void sendBroadcast(CommInterfaceMessage message) {
        log.info("Sending broadcast message");
        registeredNodes.forEach((id, queue) -> {
            if (!id.equals(message.getId())) {
                queue.add(message);
            }
        });
    }

    /**
     * Sends a message to a specific node. If node not exists, thows {@link NullPointerException} 
     */
    @Override
    public void sendMessage(CommInterfaceMessage message, UUID recipient) {
        log.info("Sending direct broadcast message");
        var queue = registeredNodes.get(recipient);
        queue.add(message);
    }

    /**
     * Indicates to this instance how should process a incoming message. If incoming message's node id
     * is not registered, throws a {@link NullPointerException}
     */
    @Override
    public void configureMessageProcessing(Consumer<CommInterfaceMessage> consumer) {
        registeredNodes.get(nodeId).registerListener(consumer);
    }

    /**
     * Method to be processed when node wants to stop its execution. In this implementation, the instance 
     * does nothing
     */
    @Override
    public void onStop() {
        // do nothing
    }
    
}
