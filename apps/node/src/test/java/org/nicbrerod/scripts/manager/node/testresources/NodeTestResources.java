package org.nicbrerod.scripts.manager.node.testresources;

import java.util.Map;
import java.util.Optional;

import org.testcontainers.containers.RabbitMQContainer;

import com.google.common.collect.ImmutableMap;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * Used to set required resources for testing classes. This class takes some properties from 
 * RabbitMQ testcontainer to allow connection from Quarkus test class
 */
public class NodeTestResources implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
    private Optional<String> containerNetworkId;
    private RabbitMQContainer container;

    @Override
    public Map<String, String> start() {
        container = new RabbitMQContainer("rabbitmq:3");

        containerNetworkId.ifPresent(container::withNetworkMode);

        container.start();

        String amqpUrl = container.getAmqpUrl();

        if (containerNetworkId.isPresent()) {
            String hostPort = container.getHost() + ":" + container.getMappedPort(5672);
            String networkHostPort = container.getCurrentContainerInfo().getConfig().getHostName() +
                ":" + 5672;
            
            amqpUrl = amqpUrl.replace(hostPort, networkHostPort);
        }

        return ImmutableMap.of(
            "rabbitmq-host", container.getAmqpUrl(),
            "rabbitmq-port", container.getAmqpPort().toString(),
            "rabbitmq-username", container.getAdminUsername(),
            "rabbitmq-password", container.getAdminPassword()
        );
    }

    @Override
    public void stop() {
        // closes container
    }

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        containerNetworkId = context.containerNetworkId();
    }
    
}
