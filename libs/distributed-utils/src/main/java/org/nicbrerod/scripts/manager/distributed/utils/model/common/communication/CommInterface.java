package org.nicbrerod.scripts.manager.distributed.utils.model.common.communication;

import java.io.Serializable;
import java.util.function.Consumer;

import org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.msg.CommInterfaceMessage;

public interface CommInterface<I extends Serializable & Comparable<I>> {
    
    void sendBroadcast(CommInterfaceMessage message);

    void sendMessage(CommInterfaceMessage message, Long recipient);

    void configureMessageProcessing(Consumer<CommInterfaceMessage> consumer);

    void onStop();
}
