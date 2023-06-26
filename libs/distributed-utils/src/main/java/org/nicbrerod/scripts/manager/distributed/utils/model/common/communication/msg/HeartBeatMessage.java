package org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.msg;

import java.io.Serializable;

import org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.CommInterfaceMessageType;

import lombok.Getter;

public class HeartBeatMessage<I extends Serializable & Comparable<I>> extends CommInterfaceMessage {
    @Getter
    private I node;
    @Getter
    private boolean leader;
    @Getter
    private long term;
    @Getter
    private float cpuUsage;
    @Getter    
    private float memoryUsage;

    public HeartBeatMessage(I node, boolean leader, long term, float cpuUsage, float memoryUsage) {
        super(CommInterfaceMessageType.HEARTBEAT);
        this.node = node;
        this.leader = leader;
        this.term = term;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((node == null) ? 0 : node.hashCode());
        result = prime * result + (leader ? 1231 : 1237);
        result = prime * result + (int) (term ^ (term >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HeartBeatMessage<?> other = (HeartBeatMessage<?>) obj;
        if (node == null) {
            if (other.node != null)
                return false;
        } else if (!node.equals(other.node))
            return false;
        if (leader != other.leader)
            return false;
        if (term != other.term)
            return false;
        return true;
    }

    

    
    
}
