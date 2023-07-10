package org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.nicbrerod.scripts.manager.distributed.utils.model.communication.CommInterfaceMessageType;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Message sent automatically from all nodes to report all nodes about its situation. 
 * This type of message is usefull to check which nodes are unavailable, checking if 
 * no messages from them have been received within a period of time
 */
@EqualsAndHashCode(callSuper = false)
public class HeartBeatMessage extends CommInterfaceMessage {

    /**
     * A boolean to indicate if sender node is the leader
     */
    @Getter
    private boolean leader;

    /**
     * Term value, a number needed to take decisions in the consensus algorithm
     */
    @Getter
    private long term;

    /**
     * Amount of CPU using on system
     */
    @Getter
    private float cpuUsage;

    /**
     * Amount of memory using on system
     */
    @Getter    
    private float memoryUsage;

    @Getter
    private OffsetDateTime dateTime;

    public HeartBeatMessage(UUID sender, boolean leader, long term, float cpuUsage, float memoryUsage) {
        super(CommInterfaceMessageType.HEARTBEAT, sender);
        this.leader = leader;
        this.term = term;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.dateTime = OffsetDateTime.now();
    }
}
