package org.nicbrerod.scripts.manager.node;

import org.awaitility.Awaitility;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.junit.jupiter.api.Test;
import org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg.CommInterfaceMessage;
import org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg.HeartBeatMessage;
import org.nicbrerod.scripts.manager.distributed.utils.node.ClusterNode;
import org.nicbrerod.scripts.manager.node.testresources.NodeTestResources;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@QuarkusTest
@QuarkusTestResource(NodeTestResources.class)
public class NodeTests {
    
    @Channel("consensus") 
    Emitter<CommInterfaceMessage> commInterfaceMessageEmitter;

    @Inject
    ClusterNode node;

    /**
     * Test used to simulate some situations, like receive new messages and register new nodes, a previously registered node that 
     * falls, ot another node that is the new leader
     */
    @Test
    public void checkMultipleNodesTest() {
        var neighbourId = UUID.randomUUID();
        var neighbourId2 = UUID.randomUUID();

        // Wait, at least, 10 seconds to ensure node is active
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> node.isActive());

        // Send a message to simulate a new node in cluster
        commInterfaceMessageEmitter.send(Message.of(new HeartBeatMessage(neighbourId, false, node.getTerm() + 1000, 0, 0), 
            Metadata.of(new OutgoingRabbitMQMetadata.Builder().withRoutingKey("consensus").build())));

        // Gets current node info and checks if id is the same as configured for current node
        given()
            .when().get("/node")
                .then()
                    .statusCode(200)
                    .body("id", equalTo(node.getId().toString()))
                    .body("isLeader", equalTo(true));

        // Gets current node's neighbours to check if has only one node and its information is the same as sent
        given()
            .when().get("/node/neighbours")
                .then()
                    .statusCode(200)
                    .body("size()", is(1))
                    .body("get(0).term", equalTo((int) node.getTerm() + 1000))
                    .body("get(0).id", equalTo(neighbourId.toString()))
                    .body("get(0).isLeader", equalTo(false));

        // Wait to ensure current node detects that simulated node is fallen
        Awaitility.await().pollDelay(3, TimeUnit.SECONDS).until(() -> true);

        // Check if simulated node has been unregistered
        given()
            .when().get("/node/neighbours")
                .then()
                    .statusCode(200)
                    .body("size()", is(0));

        // Send information about same simulated node to register it again
        commInterfaceMessageEmitter.send(Message.of(new HeartBeatMessage(neighbourId, false, -1, 0, 0), 
            Metadata.of(new OutgoingRabbitMQMetadata.Builder().withRoutingKey("consensus").build())));

        // Send information about new node with a term value less than current node to force it become to follower
        commInterfaceMessageEmitter.send(Message.of(new HeartBeatMessage(neighbourId2, false, node.getTerm() - 1000, 0, 0), 
            Metadata.of(new OutgoingRabbitMQMetadata.Builder().withRoutingKey("consensus").build())));

        // Wait to ensure current node receives messages
        Awaitility.await().pollDelay(500, TimeUnit.MILLISECONDS).until(() -> true);

        // Check if current node now isn't the leader
        given()
            .when().get("/node")
                .then()
                    .statusCode(200)
                    .body("id", equalTo(node.getId().toString()))
                    .body("isLeader", equalTo(false));

        // Check if registered nodes contains new simulated nodes
        given()
            .when().get("/node/neighbours")
                .then()
                    .statusCode(200)
                    .body("size()", is(2))
                    .body(String.format("find { it.id == '%s' }.term", neighbourId), equalTo(-1))
                    .body(String.format("find { it.id == '%s' }.term", neighbourId2), equalTo((int) node.getTerm() - 1000));
                    
    }

}
