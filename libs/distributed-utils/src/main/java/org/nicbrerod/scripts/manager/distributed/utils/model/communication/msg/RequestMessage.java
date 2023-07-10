package org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg;

import java.io.Serializable;
import java.util.UUID;

import org.nicbrerod.scripts.manager.distributed.utils.model.communication.CommInterfaceMessageType;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = false)
public class RequestMessage<T extends Serializable> extends CommInterfaceMessage {

    @Getter
    private String requestType;

    @Getter
    private T requestContent;

    public RequestMessage(UUID sender, String requestType, T requestContent) {
        super(CommInterfaceMessageType.REQUEST, sender);
        this.requestType = requestType;
        this.requestContent = requestContent;
    }
    
}
