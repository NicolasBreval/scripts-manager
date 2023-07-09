package org.nicbrerod.scripts.manager.node.amqp;

import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg.CommInterfaceMessage;

import io.smallrye.reactive.messaging.annotations.Merge;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Consumer used to process all incoming messages received from another nodes
 */
@ApplicationScoped
public class ConsensusConsumer {

    /**
     * CommInterface instance used to process incoming messages
     */
    @Inject
    AmqpCommInterface commInterface;

    /**
     * Consumer's method used to process received messages
     * @param message Message received
     * @return A completion stage, sending the ACK to RabbitMQ server
     */
    @Incoming("consensus")
    @Merge
    public CompletionStage<Void> consume(Message<CommInterfaceMessage> message) {
        commInterface.receive(message.getPayload());
        return message.ack();
    }
}
