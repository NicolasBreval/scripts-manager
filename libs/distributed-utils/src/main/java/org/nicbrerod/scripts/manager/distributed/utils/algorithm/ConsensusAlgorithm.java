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

public class ConsensusAlgorithm {
    private Random rand;
    private long heartBeatRate;
    private long milliStart;
    private long millisLimit;
    private CommInterface<Long> commInterface;
    private CountDownLatch firstWait;
    @Getter
    private ConsensusNode node;
    private Set<HeartBeatMessage<Long>> clusterNodes;
    private Long term;
    @Getter
    private Long leader;
    private AtomicInteger totalVotes = new AtomicInteger(0);
    private AtomicInteger currentNodeVotes = new AtomicInteger(0);
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

    public void sendPeriodicalHeartBeat() {
        executor.scheduleAtFixedRate(() -> 
            commInterface.sendBroadcast(new HeartBeatMessage<Long>(node.getId(), true, term, 0, 0)), 
        0, heartBeatRate, TimeUnit.MILLISECONDS);
    }

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
