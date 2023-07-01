package org.nicbrerod.scripts.manager.distributed.utils.model.communication;

/**
 * Different types of message that a CommInterface can process
 * @see org.nicbrerod.scripts.manager.distributed.utils.communication.CommInterface
 */
public enum CommInterfaceMessageType {
    /**
     * Message sent periodically by all nodes with some usefull information 
     * and used to identify which node is the leader
     */
    HEARTBEAT,
}
