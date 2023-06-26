package org.nicbrerod.scripts.manager.distributed.utils.model.common.communication;

public enum CommInterfaceMessageType {
    ELECTION,
    VOTE,
    HEARTBEAT,
    REQUEST,
    COMMIT,
    ACK,
    LEADER_CHECK
}
