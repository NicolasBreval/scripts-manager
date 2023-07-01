package org.nicbrerod.scripts.manager.distributed.utils.node;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.nicbrerod.scripts.manager.distributed.utils.communication.CommInterface;
import org.nicbrerod.scripts.manager.distributed.utils.model.communication.msg.HeartBeatMessage;

import lombok.Getter;

/**
 * Element to represent a server inside a custer. A ClusterNode has the ability to communicate 
 * between nodes in their same cluster and agree on who is the leader to organize the work
 */
public class ClusterNode {

    /**
     * Random object used to generate random numbers for some operations.
     * @see Random
     */
    private Random rand;

    /**
     * Time, in milliseconds, that a cluster node sends its own information to the other ones. 
     * This is used to notify all clusters which nodes are part of cluster and to make easy 
     * select a leader, when it's needed
     */
    private long heartbeatRate;

    /**
     * Minimum value, in milliseconds, that a wait can have in the consensus algorithm when 
     * a new random wait is generated
     */
    private long millisStart;

    /**
     * maximum value, in milliseconds, that a wait can have in the consensus algorithm when
     * a new random wait is generated
     */
    private long millisEnd;

    /**
     * Communication interface used to connect all nodes in a cluster and allow communication 
     * between them using messages
     * @see CommInterface
     */
    private CommInterface commInterface;

    /**
     * Auto-generated id used to identify a server in the cluster
     */
    @Getter
    private UUID id;

    /**
     * Map to store current information about the rest of nodes in the cluster
     */
    private Map<UUID, HeartBeatMessage> clusterNodesInfo;

    /**
     * Term value used to check which node in the cluster is the oldest. This value 
     * is usefull when two or more nodes want to become the leader and algorithm needs to
     * select one 
     */
    private long term;

    /**
     * Id related to current leader in the cluster
     */
    @Getter
    private UUID leader;

    /**
     * Term value related to current leader in the cluster
     */
    private long leaderTerm;

    /**
     * Executor service used to send own information to the other nodes periodically
     */
    private ScheduledExecutorService heartbeatExecutor;

    /**
     * Boolean value to indicate that the node is ready to operate with it
     */
    @Getter
    private boolean active;

    public ClusterNode(CommInterface commInterface) {
        this.id = UUID.randomUUID();
        this.commInterface = commInterface;
        this.rand = new Random();
        this.heartbeatRate = 1000;
        this.millisStart = 1000;
        this.millisEnd = 5000;
        this.clusterNodesInfo = new ConcurrentHashMap<>();
        this.term = OffsetDateTime.now().atZoneSameInstant(ZoneId.of("Europe/Madrid")).getNano();
        this.leader = null;
        this.leaderTerm = Long.MAX_VALUE;
        this.active = false;
        this.heartbeatExecutor = Executors.newScheduledThreadPool(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> heartbeatExecutor.shutdownNow()));
    }

    public ClusterNode(CommInterface commInterface, long heartbeatRate) {
        this(commInterface);
        this.heartbeatRate = heartbeatRate;
    }

    public ClusterNode(CommInterface commInterface, long millisStart, long millisEnd) {
        this(commInterface);
        this.millisStart = millisStart;
        this.millisEnd = millisEnd;
    }

    public ClusterNode(CommInterface commInterface, Random rand) {
        this(commInterface);
        this.rand = rand;
    }

    public ClusterNode(CommInterface commInterface, long heartbeatRate, long millisStart, long millisEnd) {
        this(commInterface);
        this.heartbeatRate = heartbeatRate;
        this.millisStart = millisStart;
        this.millisEnd = millisEnd;
    }

    public ClusterNode(CommInterface commInterface, long heartbeatRate, Random rand) {
        this(commInterface);
        this.heartbeatRate = heartbeatRate;
        this.rand = rand;
    }

    public ClusterNode(CommInterface commInterface, long millisStart, long millisEnd, Random rand) {
        this(commInterface);
        this.millisStart = millisStart;
        this.millisEnd = millisEnd;
        this.rand = rand;
    }

    public ClusterNode(CommInterface commInterface, long heartbeatRate, long millisStart, long millisEnd, Random rand) {
        this(commInterface);
        this.heartbeatRate = heartbeatRate;
        this.millisStart = millisStart;
        this.millisEnd = millisEnd;
        this.rand = rand;
    }

    /**
     * Starts periodically heartbeat message sending
     */
    public void sendPeriodicalHeartBeat() {
        // TODO: Get system's cpu and memory usage
        heartbeatExecutor.scheduleAtFixedRate(() -> commInterface.sendBroadcast(
            new HeartBeatMessage(this.id, this.leader == this.id, term, 0, 0)), 
            0, heartbeatRate, TimeUnit.MILLISECONDS);
    }

    /**
     * Checks if this node instance is the leader in cluster
     * @return True if this instance is the leader, false else
     */
    public boolean imLeader() {
        return this.leader == this.id;
    }

    /**
     * Indicates to CommInterface how to process a type of message received from another
     * node in cluster
     */
    public void configureCommInterface() {
        this.commInterface.configureMessageProcessing((message) -> {
            switch (message.getType()) {
                case HEARTBEAT:
                    var heartbeat = (HeartBeatMessage)message;

                    if (heartbeat.isLeader() && heartbeat.getTerm() < this.term) {
                        this.leader = heartbeat.getNode();
                        this.leaderTerm = heartbeat.getTerm();
                    }

                    clusterNodesInfo.put(heartbeat.getNode(), heartbeat);
                    break; 
                default:
                    break;
            }
        });
    }

    /**
     * Generates a random long number from {@link #millisStart} to {@link #millisEnd}. This is commonly used 
     * to generate random waits for algorithm
     * @return Random long number
     */
    public long generateRandomWaitTime() {
        return this.rand.longs(millisStart, millisEnd).findFirst().getAsLong();
    }

    /**
     * Starts the consensus algorithm to check if exists a leader node and, if not exists, 
     * try to become it
     * @throws InterruptedException Throw if any thread-based operation is interrupted by system
     */
    public void consensus() throws InterruptedException {
        // first of all, start heartbeat sending to notify of your existence
        sendPeriodicalHeartBeat();

        // wait a random amount of milliseconds. This is usefull to wait to receive messages from the leader and 
        // don't to try become it
        Thread.sleep(generateRandomWaitTime());

        // If leader term registered if less than this instance term, marks it as 
        // his own to send it to another nodes and notify that it tries to become leader
        if (leaderTerm > this.term) {
            this.leader = this.id;
            this.leaderTerm = this.term;
        }

        // Now, node is active and can accept requests
        this.active = true;
    }
}
