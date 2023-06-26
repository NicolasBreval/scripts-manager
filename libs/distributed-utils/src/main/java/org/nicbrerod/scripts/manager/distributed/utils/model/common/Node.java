package org.nicbrerod.scripts.manager.distributed.utils.model.common;

import java.io.Serializable;

/**
 * Represents a server in a cluster. A node is an element that can be identified by a unique id and has a state, 
 * indicating what kind of operations it may be performing at that moment. There are two main types of nodes, 
 * the leader node and the follower node. The leader node is in charge of accepting external requests to 
 * perform different types of actions, while the follower node accepts the execution of such requests, demanded by the leader node.
 * 
 * @param <I> Generic type to define node id's type. The type of a node id must implements Serializable, to be able to
 * serialize the value when sending it in a message, and implements Comparable too, because it's needed to compare ids in most 
 * cases when two nodes are being compared.
 */
public interface Node<I extends Serializable & Comparable<I>> {
    
    /**
     * Current status of node. This indicates what kind of operations is performing at moment.
     * @return Current role of node
     * @see Role
     */
    Role getRole();

    /**
     * Identifier of a node in a cluster. This value is usefull to distinguish two nodes in a same cluster and also to 
     * keep a trace of node operations.
     * @return Identifier of node in its cluster
     */
    I getId();
}
