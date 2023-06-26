package org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.msg;

import java.io.Serializable;
import java.util.UUID;

import org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.CommInterfaceMessageType;

import lombok.Getter;

public abstract class CommInterfaceMessage implements Serializable {
    @Getter
    private final CommInterfaceMessageType type;

    @Getter
    private final UUID id = UUID.randomUUID();

    public CommInterfaceMessage(CommInterfaceMessageType type) {
        this.type = type;
    }
}
