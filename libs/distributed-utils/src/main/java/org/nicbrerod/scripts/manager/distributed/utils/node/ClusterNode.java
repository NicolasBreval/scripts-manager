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

import org.jboss.logging.Logger;
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
    @Getter
    private Map<UUID, HeartBeatMessage> clusterNodesInfo;

    /**
     * Term value used to check which node in the cluster is the oldest. This value 
     * is usefull when two or more nodes want to become the leader and algorithm needs to
     * select one 
     */
    @Getter
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
     * Executor service used to check if any nodes have stopped communicating with it, in order to remove them from the registry
     */
    private ScheduledExecutorService checkClusterNodesExecutor;

    /**
     * Boolean value to indicate that the node is ready to operate with it
     */
    @Getter
    private boolean active;

    private Logger log;

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
        this.checkClusterNodesExecutor = Executors.newScheduledThreadPool(1);
        this.log = Logger.getLogger(this.id.toString());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> heartbeatExecutor.shutdownNow()));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> checkClusterNodesExecutor.shutdownNow()));
    }

    public ClusterNode(CommInterface commInterface, UUID id) {
        this(commInterface);
        this.id = id;
    }

    public ClusterNode(CommInterface commInterface, long heartbeatRate) {
        this(commInterface);
        this.heartbeatRate = heartbeatRate;
    }

    public ClusterNode(CommInterface commInterface, UUID id, long heartbeatRate) {
        this(commInterface, heartbeatRate);
        this.id = id;
    }

    public ClusterNode(CommInterface commInterface, long millisStart, long millisEnd) {
        this(commInterface);
        this.millisStart = millisStart;
        this.millisEnd = millisEnd;
    }

    public ClusterNode(CommInterface commInterface, UUID id, long millisStart, long millisEnd) {
        this(commInterface, millisStart, millisEnd);
        this.id = id;
    }

    public ClusterNode(CommInterface commInterface, Random rand) {
        this(commInterface);
        this.rand = rand;
    }

    public ClusterNode(CommInterface commInterface, UUID id, Random rand) {
        this(commInterface, rand);
        this.id = id;
    }

    public ClusterNode(CommInterface commInterface, long heartbeatRate, long millisStart, long millisEnd) {
        this(commInterface);
        this.heartbeatRate = heartbeatRate;
        this.millisStart = millisStart;
        this.millisEnd = millisEnd;
    }

    public ClusterNode(CommInterface commInterface, UUID id, long heartbeatRate, long millisStart, long millisEnd) {
        this(commInterface, heartbeatRate, millisStart, millisEnd);
        this.id = id;
    }

    public ClusterNode(CommInterface commInterface, long heartbeatRate, Random rand) {
        this(commInterface);
        this.heartbeatRate = heartbeatRate;
        this.rand = rand;
    }

    public ClusterNode(CommInterface commInterface, UUID id, long heartbeatRate, Random rand) {
        this(commInterface, heartbeatRate, rand);
        this.id = id;
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

    public ClusterNode(CommInterface commInterface, UUID id, long heartbeatRate, long millisStart, long millisEnd, Random rand) {
        this(commInterface, heartbeatRate, millisStart, millisEnd, rand);
        this.id = id;
    }

    /**
     * Maximum time a node can go without communicating with the current node before the current node deregisters it
     */
    private long getCheckRegisteredClusterNodesRate() {
        return Math.round(this.heartbeatRate * 2);
    }

    /**
     * Starts periodically heartbeat message sending
     */
    public void sendPeriodicalHeartBeat() {
        // TODO: Get system's cpu and memory usage
        heartbeatExecutor.scheduleAtFixedRate(() -> commInterface.sendBroadcast(
            new HeartBeatMessage(this.id, this.leader.equals(this.id), term, 0, 0)), 
            0, heartbeatRate, TimeUnit.MILLISECONDS);
    }

    /**
     * Starts periodically nodes checking
     */
    public void checkClusterNodeRegistry() {
        // TODO: Add logic if leader is the cluster node to remove
        checkClusterNodesExecutor.scheduleAtFixedRate(() -> {
            var currentMillis = OffsetDateTime.now().toInstant().toEpochMilli();

            this.clusterNodesInfo.entrySet().stream().filter(entry -> {
                var millisDateTime = entry.getValue().getDateTime().toInstant().toEpochMilli();
                return currentMillis - millisDateTime > getCheckRegisteredClusterNodesRate();
            }).forEach(entry -> {
                log.warn(String.format("New inactive node: '%s'", entry.getKey()));
                this.clusterNodesInfo.remove(entry.getKey());
            });

        }, 0, getCheckRegisteredClusterNodesRate(), TimeUnit.MILLISECONDS);
    }

    /**
     * Checks if this node instance is the leader in cluster
     * @return True if this instance is the leader, false else
     */
    public boolean imLeader() {
        return this.leader.equals(this.id);
    }

    /**
     * Indicates to CommInterface how to process a type of message received from another
     * node in cluster
     */
    public void configureCommInterface() {
        this.commInterface.configureMessageProcessing((message) -> {
            if (message.getId().equals(this.id))
                return;

            switch (message.getType()) {
                case HEARTBEAT:
                    var heartbeat = (HeartBeatMessage)message;

                    log.info(String.format("New message received from '%s'", heartbeat.getNode()));

                    if (heartbeat.getTerm() < this.term) {
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

        // then, starts to check for another nodes registered as neighbours of same cluster
        checkClusterNodeRegistry();

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
