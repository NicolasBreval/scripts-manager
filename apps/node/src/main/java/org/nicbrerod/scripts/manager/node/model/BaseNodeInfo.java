package org.nicbrerod.scripts.manager.node.model;

import java.util.UUID;

/**
 * Basic information of a node
 */
public record BaseNodeInfo(
    /**
     * Id related to node
     */
    UUID id,
    /**
     * Term value of node
     */
    long term,
    /**
     * True if node is the leader of its cluster, else false
     */
    boolean isLeader
) {}
