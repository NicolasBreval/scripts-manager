package org.nicbrerod.scripts.manager.distributed.utils.model.common;

/**
 * Enum object used to declare different roles a Node can have during their lifetime.
 */
public enum Role {
    FOLLOWER, // With follower role, a node waits for LEADER instructions and don't take any decision
    CANDIDATE, // This role is taken by a node only when not exists any LEADER and tries to act as one
    LEADER // Role that only one node on system can have. With this node, all requests received by an user are sent to FOLLOWER nodes to be processed.
}
