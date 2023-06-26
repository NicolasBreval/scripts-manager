package org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.msg;

import java.io.Serializable;

import org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.CommInterfaceMessageType;

import lombok.Getter;

public class ElectionMessage<I extends Serializable & Comparable<I>> extends CommInterfaceMessage {

    @Getter
    private I candidate;

    public ElectionMessage(I candidate) {
        super(CommInterfaceMessageType.ELECTION);
        this.candidate = candidate;
    }
    
}
