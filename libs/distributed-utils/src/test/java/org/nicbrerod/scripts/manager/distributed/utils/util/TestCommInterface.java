package org.nicbrerod.scripts.manager.distributed.utils.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.CommInterface;
import org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.msg.CommInterfaceMessage;
import org.nicbrerod.scripts.manager.distributed.utils.model.custom.ConsensusNode;

/**
 * {@link Comminterface} implementation used only for local tests. This implementation registers all nodes in their static 
 * context and allow to send messages to all nodes easilly
 */
public class TestCommInterface implements CommInterface<Long> {

    private final static Map<ConsensusNode, LinkedBlockingQueue<CommInterfaceMessage>> clusterNodes = new ConcurrentHashMap<>();
    private ConsensusNode current;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private Consumer<CommInterfaceMessage> messageConsumer = null;

    public void registerNode(ConsensusNode node) {
            clusterNodes.put(node, new LinkedBlockingQueue<>());
            current = node;
            executor.scheduleAtFixedRate(() -> {
                CommInterfaceMessage message = clusterNodes.get(current).poll();
                if (message != null) {
                    messageConsumer.accept(message);
                }
            }, 0, 1, TimeUnit.MILLISECONDS);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));
    }

    @Override
    public void sendBroadcast(CommInterfaceMessage message) {
        clusterNodes.entrySet().stream().filter(entry -> !entry.getKey().equals(current)).forEach(entry -> entry.getValue().add(message));
    }

    @Override
    public void sendMessage(CommInterfaceMessage message, Long recipient) {
        clusterNodes.entrySet().stream().filter(node -> node.getKey().getId() == recipient).findFirst().ifPresent(entry -> entry.getValue().add(message));
    }

    @Override
    public void configureMessageProcessing(Consumer<CommInterfaceMessage> consumer) {
        messageConsumer = consumer;
    }

    @Override
    public void onStop() {
        executor.shutdownNow();
        if (current != null)
            clusterNodes.remove(current);
    }
    
}
