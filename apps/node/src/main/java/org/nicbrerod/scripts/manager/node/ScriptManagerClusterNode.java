package org.nicbrerod.scripts.manager.node;

import java.util.UUID;

import org.nicbrerod.scripts.manager.distributed.utils.communication.CommInterface;
import org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg.RequestMessage;
import org.nicbrerod.scripts.manager.distributed.utils.node.ClusterNode;
import org.nicbrerod.scripts.manager.script.runner.model.Script;

public class ScriptManagerClusterNode extends ClusterNode<Script> {

    public ScriptManagerClusterNode(CommInterface commInterface, UUID id, long heartbeatRate, long millisStart, long millisEnd) {
        super(commInterface, id, heartbeatRate, millisStart, millisEnd);
    }

    @Override
    protected void onRequest(RequestMessage<Script> request) {
        // TODO: Call scriptsManager
    }
    
}
