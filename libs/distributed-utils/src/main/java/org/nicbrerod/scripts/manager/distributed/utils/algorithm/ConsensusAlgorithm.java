package org.nicbrerod.scripts.manager.distributed.utils.algorithm;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.nicbrerod.scripts.manager.distributed.utils.model.common.Role;
import org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.CommInterface;
import org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.msg.ElectionMessage;
import org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.msg.HeartBeatMessage;
import org.nicbrerod.scripts.manager.distributed.utils.model.common.communication.msg.VoteMessage;
import org.nicbrerod.scripts.manager.distributed.utils.model.custom.ConsensusNode;

import lombok.Getter;

/**
 * Algorithm used in distributed systems to assign roles to nodes in a cluster and 
 * keep which node is the leader
 */
public class ConsensusAlgorithm {
    /**
     * Used to generate some random numbers
     */
    private Random rand;

    /**
     * Time to wait between two heartbeat message sent
     */
    private long heartBeatRate;

    /**
     * Used to generate random wait times. This is the minimum value of a wait
     */
    private long milliStart;

    /**
     * Used to generate random wait times. This is the maximum value of a wait
     */
    private long millisLimit;

    /**
     * Object used to allow communication between nodes in a cluster
     */
    private CommInterface<Long> commInterface;

    /**
     * Used to keep thread waiting for a leader in the startup of node
     */
    private CountDownLatch firstWait;

    /**
     * Current node instance. This algorithm is applied on each node of a cluster, 
     * so, each node instance have always a node related to it
     */
    @Getter
    private ConsensusNode node;

    /**
     * The rest of nodes in cluster. This set is initially empty and is being filled 
     * when new heartbeat messages are received from another nodes
     */
    private Set<HeartBeatMessage<Long>> clusterNodes;

    /**
     * Term value is a number used when two nodes have the leader role. In a distributed system 
     * only one node can be the leader, but, in some cases, due to connection problems, two nodes 
     * can be the leader, so, this value is used to choose the leader in these situations. When two 
     * or more nodes are leaders and are sending their heartbeat message, this value is sending too, and, 
     * if a leader node receives a message of another leader node with a term value greater than yours, 
     * these node is no longer leader and become to follower node
     */
    private Long term;

    /**
     * Identifier of current leader node in cluster. If this value is equals to {@link ConsensusAlgorithm#node} id, 
     * it means that this node is the leader
     */
    @Getter
    private Long leader;

    /**
     * Number used to registry all votes received from another nodes when this node is trying to be the leader
     */
    private AtomicInteger totalVotes = new AtomicInteger(0);

    /**
     * Number used to registry votes received from another node, choosing this node as leader.
     */
    private AtomicInteger currentNodeVotes = new AtomicInteger(0);

    /**
     * Executor service used for multiple periodical tasks, like send periodicaally the heartbeat message
     */
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public ConsensusAlgorithm(CommInterface<Long> ci) {
        node = new ConsensusNode();
        rand = new Random();
        heartBeatRate = 1000;
        milliStart = 1;
        millisLimit = 5000;
        commInterface = ci;
        clusterNodes = new HashSet<>();
        term = -1L;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));
        configureCommInterface();
    }

    public ConsensusAlgorithm(CommInterface<Long> ci, long heartBeatRate) {
        this(ci);
        this.heartBeatRate = heartBeatRate;
    }

    public ConsensusAlgorithm(CommInterface<Long> ci, long heartBeatRate, long seed) {
        this(ci, heartBeatRate);
        this.rand = new Random(seed);
    }

    public ConsensusAlgorithm(CommInterface<Long> ci, long heartBeatRate, long seed, long millisLimit) {
        this(ci, heartBeatRate, seed);
        this.millisLimit = millisLimit;
    }

    public ConsensusAlgorithm(CommInterface<Long> ci, long heartBeatRate, long seed, long milliStart, long millisLimit) {
        this(ci, heartBeatRate, seed, millisLimit);
        this.milliStart = milliStart;
    }

    private long generateRandomWait() {
        return rand.longs(milliStart, millisLimit).findFirst().getAsLong();
    }

    /**
     * Configures {@link #commInterface} to indicate how it should process messages received by other nodes
     */
    @SuppressWarnings("unchecked")
    public void configureCommInterface() {
        commInterface.configureMessageProcessing(message -> {
            switch (message.getType()) {
                case ELECTION:
                    if (message instanceof ElectionMessage) {
                        final Long candidate = ((ElectionMessage<Long>) message).getCandidate();
                        commInterface.sendBroadcast(new VoteMessage<Long>(candidate));
                    }
                    break;
                case VOTE:
                    if (message instanceof VoteMessage) {
                        if (node.getRole() == Role.CANDIDATE) {
                            final Long candidate = ((VoteMessage<Long>) message).getCandidate();
                            totalVotes.incrementAndGet();
                            if (candidate == node.getId()) {
                                currentNodeVotes.incrementAndGet();
                            }
                        }
                    }
                    break;
                case HEARTBEAT:
                    if (message instanceof HeartBeatMessage) {
                        final HeartBeatMessage<Long> received = (HeartBeatMessage<Long>)message;

                        // if exists another leader on cluster and this leader receives their heartbeat,
                        // first, check their term value, if term is greater than current term value,
                        // then, the another leader becomes to unique leader
                        if (received.isLeader() && received.getTerm() > term) {
                            node.setRole(Role.FOLLOWER);
                            leader = received.getNode();
                            firstWait.countDown();
                        }

                        clusterNodes.add((HeartBeatMessage<Long>)message);
                    }
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * Sends a heartbeat message in broadcast mode with some information. The heartbeat message is used to indentify new nodes, 
     * identify the leader node and send some system information of sender node
     */
    public void sendPeriodicalHeartBeat() {
        // TODO: calculate cpu usage and memory usage to be sent in heartbeat message
        executor.scheduleAtFixedRate(() -> 
            commInterface.sendBroadcast(new HeartBeatMessage<Long>(node.getId(), true, term, 0, 0)), 
        0, heartBeatRate, TimeUnit.MILLISECONDS);
    }

    /**
     * Starts algorithm execution. To learn more about, read project's README file
     * @throws InterruptedException
     */
    public void run() throws InterruptedException {
        final long waitTime = generateRandomWait();
        firstWait = new CountDownLatch(1);
        firstWait.await(waitTime, TimeUnit.MILLISECONDS);

        // If leader id is already null, then starts the election operation
        if (leader == null) {
            node.setRole(Role.CANDIDATE);

            // First, communicate to all nodes that current node is a candidate
            commInterface.sendBroadcast(new ElectionMessage<Long>(node.getId()));

            // Then, waits for a random amount of time for the rest of the nodes to vote
            Thread.sleep(generateRandomWait());

            // To win the election, it must to receive, at least, (N / 2) + 1 votes, where N is the total amount of votes
            if (currentNodeVotes.get() >= (totalVotes.get() / 2) + 1) {
                node.setRole(Role.LEADER);
                leader = node.getId();
                term = System.nanoTime();

                // Send new heartbeat message indicating current node is the leader
                sendPeriodicalHeartBeat();
            } else {
                // If current node doesn't the winner, repeats the algorithm until one node becomes a leader
                run();
            }
        } else {
            node.setRole(Role.FOLLOWER);
            // If already exists a leader, start to send a periodical heart beat
            sendPeriodicalHeartBeat();
        }
    }

    public void stop() {
        executor.shutdownNow();
    }

}
