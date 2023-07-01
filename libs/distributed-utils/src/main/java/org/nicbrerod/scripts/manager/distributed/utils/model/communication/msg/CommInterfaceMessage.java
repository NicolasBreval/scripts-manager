package org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.nicbrerod.scripts.manager.distributed.utils.model.communication.CommInterfaceMessageType;

import lombok.Getter;

/**
 * Message sent from a cluster node to another through a CommInterface object.
 * @see org.nicbrerod.scripts.manager.distributed.utils.communication.CommInterface 
 */
public abstract class CommInterfaceMessage implements Serializable {

    /**
     * Type of message. This prooerty is highly important, because parsers can use it to 
     * deserialize their content
     */
    @Getter
    private final CommInterfaceMessageType type;

    /**
     * Auto-generated ID, used to register message
     */
    @Getter
    private final UUID id = UUID.randomUUID();

    /**
     * Date when message was created. This property allows to identify messages, next to 
     * the id
     */
    @Getter
    private final OffsetDateTime timestamp = OffsetDateTime.now();

    public CommInterfaceMessage(CommInterfaceMessageType type) {
        this.type = type;
    }
}
