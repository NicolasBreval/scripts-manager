package org.nicbrerod.scripts.manager.distributed.utils.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.awaitility.Awaitility;
import org.jboss.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.nicbrerod.scripts.manager.distributed.utils.util.LocalCommInterface;
import org.nicbrerod.scripts.manager.distributed.utils.util.TestNode;

/**
 * Test class used to check if consensus node works in a virtual environment, where cluster is 
 * simulated with object instances and communication is directly in same system.
 * 
 * For nodes communication, an instance of {@link org.nicbrerod.scripts.manager.distributed.utils.communication.CommInterface} has been 
 * implemented to redirect all nodes messages to another ones.
 * 
 * This test is parameterized to check it with different number of nodes in cluster.
 * 
 * @see LocalCommInterface
 */
@RunWith(Parameterized.class)
public class ClusterNodeTests {

    /**
     * Logger object used to show some messages about test execution
     */
    private final static Logger log = Logger.getLogger(ClusterNodeTests.class);

    /**
     * List of nodes created in the test
     */
    private List<TestNode> nodes = new ArrayList<>();
    
    /**
     * Parameter list to define the number of nodes in each test execution
     * @return List of parameters of each test execution
     */
    @Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(2, 5, 10, 20).stream().map(x -> new Object[]{ x }).toList();
    }

    public ClusterNodeTests(int nodeCount) {
        for (int i = 0; i < nodeCount; i++) {
            var commInterface = new LocalCommInterface();
            var node = new TestNode(commInterface);
            commInterface.registerNode(node);
            node.configureCommInterface();
            this.nodes.add(node);
        }
    }

    /**
     * Checks that, when all nodes have execute their consensus algorithm, all nodes have the same leader 
     * and there is no other leader in the system
     * @throws InterruptedException Thrown if any thread created by nodes is broken by a system interruption
     */
    @Test
    public void checkOnlyOneLeader() throws InterruptedException {
        log.info(String.format("Test for %d nodes", nodes.size()));
        var executor = Executors.newScheduledThreadPool(nodes.size());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));

        for (var node : nodes) {
            executor.schedule(() -> {
                try {
                    node.consensus();
                } catch (InterruptedException e) {
                    // do nothing
                }
            }, 0, TimeUnit.MILLISECONDS);
        }

        Awaitility.await().atMost(20, TimeUnit.SECONDS)
            .until(() -> {
                var leaders = nodes.stream().map(x -> x.getLeader()).collect(Collectors.toSet());
                log.info("Current leaders: " + leaders.size());
                return leaders.size() == 1;
            });
    }
}
