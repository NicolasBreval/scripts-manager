package org.nicbrerod.scripts.manager.distributed.utils.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nicbrerod.scripts.manager.distributed.utils.communication.CommInterface;
import org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg.RequestMessage;
import org.nicbrerod.scripts.manager.distributed.utils.node.ClusterNode;

public class TestNode extends ClusterNode<String> {
    public static final String REQUEST_ADD = "ADD";
    public static final String REQUEST_DELETE = "DELETE";
    public static final String REQUEST_CLEAR = "CLEAR";

    private Map<String, Integer> received = new ConcurrentHashMap<>();

    public TestNode(CommInterface commInterface) {
        super(commInterface);
    }

    @Override
    protected void onRequest(RequestMessage<String> request) {
        Integer current = received.computeIfAbsent(request.getRequestContent(), x -> 0);
        
        switch (request.getRequestType()) {
            case REQUEST_ADD:
                received.put(request.getRequestContent(), current + 1);
                break;
            case REQUEST_DELETE:
                received.put(request.getRequestContent(), current - 1 < 0 ? 0 : current - 1);
                break;
            case REQUEST_CLEAR:
                received.put(request.getRequestContent(), 0);
                break;
        }
    }

    public int getCount(String key) {
        return received.getOrDefault(key, 0);
    }
    
}
