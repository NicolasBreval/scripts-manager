package org.nicbrerod.scripts.manager.distributed.utils.algorithm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.nicbrerod.scripts.manager.distributed.utils.util.TestCommInterface;

public class ConsensusAlgorithmTest {

    private List<ConsensusAlgorithm> generateResources(int number) {
        final List<ConsensusAlgorithm> instances = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            final TestCommInterface ci = new TestCommInterface();
            final ConsensusAlgorithm algorithm = new ConsensusAlgorithm(ci);
            ci.registerNode(algorithm.getNode());
            instances.add(algorithm);
        }

        return instances;
    }

    private void parameterizedConsensusTest(int instancesNumber, long wait) {
        final List<ConsensusAlgorithm> instances = generateResources(instancesNumber);
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));

        try {
            for (var instance : instances) {
                executor.schedule(() -> {
                    try {
                        instance.run();
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }, 0, TimeUnit.MILLISECONDS);
            }

            Thread.sleep(wait);

            assertTrue("Error, no instance has acquired the role of leader",
                    instances.stream().anyMatch(instance -> instance.getLeader() == instance.getNode().getId()));
            assertFalse("Error, there are more than one leader in cluster", instances.stream()
                    .filter(instance -> instance.getLeader() == instance.getNode().getId()).count() > 1);
        } catch (Exception e) {
            // do nothing
        } finally {
            for (var instance : instances) {
                instance.stop();
            }
        }
    }

    @Test
    public void basicConsensusTest() throws InterruptedException {
        parameterizedConsensusTest(2, 10000);
    }

    @Test
    public void hugeConsensusTest() {
        parameterizedConsensusTest(10, 10000);
    }

    @Test
    public void absurdConsensusTest() {
        parameterizedConsensusTest(100, 10000);
    }
}
