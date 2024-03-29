package org.nicbrerod.scripts.manager.node;

import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.nicbrerod.scripts.manager.distributed.utils.communication.CommInterface;
import org.nicbrerod.scripts.manager.distributed.utils.node.ClusterNode;
import org.nicbrerod.scripts.manager.script.runner.model.Script;

import io.quarkus.arc.log.LoggerName;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Configuration class used to generate a Node instance to represent a node inside a cluster
 */
@ApplicationScoped
public class ClusterNodeProducer {
    
    /**
     * Time, in milliseconds, that the node will send its status to other ones
     */
    @ConfigProperty(name = "node.heartbeat-rate")
    long heartbeatRate;

    /**
     * Mininum value of milliseconds, included, that the node will waits in its startup
     */
    @ConfigProperty(name = "node.millis-start")
    long millisStart;

    /**
     * Maximum value of milliseconds, excluded, that the node will waits in its startup
     */
    @ConfigProperty(name = "node.millis-end")
    long millisEnd;

    /**
     * Node's ID
     */
    @ConfigProperty(name = "node.id")
    UUID id;

    /**
     * CommInterface instance used to send messages to the other nodes
     */
    @Inject
    CommInterface commInterface;

    @LoggerName("node")
    Logger log;
    
    /**
     * Method to generate a unique instance of node in this system
     * @return The generated ClusterNode instance, based on properties
     */
    @Startup
    @Produces
    @Singleton
    public ClusterNode<Script> createNode() {
        var node = new ScriptManagerClusterNode(commInterface, id, heartbeatRate, millisStart, millisEnd);
        node.configureCommInterface();
        new Thread(() -> {
            try {
                node.consensus();
            } catch (InterruptedException e) {
                log.error("Error running consensus algorithm for node", e);
            }
        }).start();
        return node;
    }

}
