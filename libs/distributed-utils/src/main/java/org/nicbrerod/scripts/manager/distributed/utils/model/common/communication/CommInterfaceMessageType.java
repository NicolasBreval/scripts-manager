package org.nicbrerod.scripts.manager.distributed.utils.model.common.communication;

/**
 * Different types of message that a {@linkplain org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.CommInterface}
 * can process.
 */
public enum CommInterfaceMessageType {
    /**
     * Message sent when a node want to be the new leader
     */
    ELECTION,
    /**
     * Message sent when a node votes for another to be the new leader
     */
    VOTE,
    /**
     * Message sent periodically by all nodes with some usefull information 
     * and used to identify which node is the leader
     */
    HEARTBEAT
}
