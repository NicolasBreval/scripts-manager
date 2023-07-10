package org.nicbrerod.scripts.manager.distributed.utils.node;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg.RequestMessage;
import org.nicbrerod.scripts.manager.distributed.utils.util.LocalCommInterface;
import org.nicbrerod.scripts.manager.distributed.utils.util.TestNode;

public class ClusterNodeRequestTests {
    
    @Test
    public void requestTests() {
        var commInterface = new LocalCommInterface();
        var node = new TestNode(commInterface);
        commInterface.registerNode(node);
        node.configureCommInterface();

        commInterface.sendMessage(new RequestMessage<String>(node.getId(), "ADD", "TYPE 1"), node.getId());
        commInterface.sendMessage(new RequestMessage<String>(node.getId(), "ADD", "TYPE 2"), node.getId());
        commInterface.sendMessage(new RequestMessage<String>(node.getId(), "ADD", "TYPE 2"), node.getId());
        
        assertEquals("Error on test, count of TYPE 1 message is incorrect", 1, node.getCount("TYPE 1"));
        assertEquals("Error on test, count of TYPE 2 message is incorrect", 2, node.getCount("TYPE 2"));
        assertEquals("Error on test, count of TYPE 3 message is incorrect", 0, node.getCount("TYPE 3"));
        
        commInterface.sendMessage(new RequestMessage<String>(node.getId(), "DELETE", "TYPE 1"), node.getId());
        commInterface.sendMessage(new RequestMessage<String>(node.getId(), "CLEAR", "TYPE 2"), node.getId());

        assertEquals("Error on test, count of TYPE 1 message is incorrect", 0, node.getCount("TYPE 1"));
        assertEquals("Error on test, count of TYPE 2 message is incorrect", 0, node.getCount("TYPE 2"));
        assertEquals("Error on test, count of TYPE 3 message is incorrect", 0, node.getCount("TYPE 3"));
    }
}
