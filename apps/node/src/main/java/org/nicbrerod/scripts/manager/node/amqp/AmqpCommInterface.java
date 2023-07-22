package org.nicbrerod.scripts.manager.node.amqp;

import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.nicbrerod.scripts.manager.distributed.utils.communication.CommInterface;
import org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg.CommInterfaceMessage;

import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import jakarta.inject.Singleton;

/**
 * Instance of CommInterface used to define a service used by a cluster's node to send messages to another ones and 
 * specify how to process received messages. This implementation is based on RabbitMQ and Smallrye Reactive Messaging.
 * @see CommInterface
 * @see https://smallrye.io/smallrye-reactive-messaging/3.18.0/rabbitmq/rabbitmq/
 */
@Singleton
public class AmqpCommInterface implements CommInterface {

    /**
     * Consumer object used to process received files
     */
    private Consumer<CommInterfaceMessage> consumer = null;

    /**
     * Channel to send messages to a RabbitMQ's exchange
     */
    @Channel("consensus") 
    Emitter<CommInterfaceMessage> commInterfaceMessageEmitter;

    /**
     * Node's ID
     */
    @ConfigProperty(name = "node.id")
    UUID nodeId;

    /**
     * Sends a message to all nodes. In this case, all consumers in all nodes have a "consensus" topic configured, so, routing messages with this key, all 
     * nodes will receive the messages
     */
    @Override
    public void sendBroadcast(CommInterfaceMessage message) {
        commInterfaceMessageEmitter.send(Message.of(message, Metadata.of(new OutgoingRabbitMQMetadata.Builder().withRoutingKey("consensus").build())));
    }

    /**
     * Sends a message to only one node, All consumers in all nodes have another topic configured, with the same name of their ID, so, to send a message only 
     * to an specific node, using its ID as routing key, only these node will receive the message
     */
    @Override
    public void sendMessage(CommInterfaceMessage message, UUID recipient) {
        commInterfaceMessageEmitter.send(Message.of(message, Metadata.of(new OutgoingRabbitMQMetadata.Builder().withRoutingKey(recipient.toString()).build())));
    }

    /**
     * Links the consumer passed as argument as consumer that will process all received messages
     */
    @Override
    public void configureMessageProcessing(Consumer<CommInterfaceMessage> consumer) {
        this.consumer = consumer;
    }

    /**
     * Makes anything before CommInterface stop. In this case, RabbitMQ is managed by SmallRye and Quarkus, so, it's not needed an implementation
     */
    @Override
    public void onStop() {
        // do nothing
    }

    /**
     * Receives a message and process it using configured consumer
     * @param message Message received
     */
    public void receive(CommInterfaceMessage message) {
        if (consumer != null && !message.getSender().equals(nodeId))
            consumer.accept(message);
    }
    
}
