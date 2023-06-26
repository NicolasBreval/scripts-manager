package org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.msg;

import java.io.Serializable;

import org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.CommInterfaceMessageType;

import lombok.Getter;

public class VoteMessage<I extends Serializable & Comparable<I>> extends CommInterfaceMessage {

    @Getter
    private I candidate;

    public VoteMessage(I candidate) {
        super(CommInterfaceMessageType.VOTE);
        this.candidate = candidate;
    }
    
}
