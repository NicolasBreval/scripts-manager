package org.nicbrerod.scripts.manager.distributed.utils.model.custom;

import java.util.Random;

import org.nicbrerod.scripts.manager.distributed.utils.model.common.Node;
import org.nicbrerod.scripts.manager.distributed.utils.model.common.Role;

import lombok.Setter;

/**
 * {@link Node} implementation to be used with {@linkplain org.nicbrerod.scripts.manager.distributed.utils.algorithm.ConsensusAlgorithm}.
 */
public class ConsensusNode implements Node<Long> {

    /**
     * Node identifier. {@linkplain org.nicbrerod.scripts.manager.distributed.utils.algorithm.ConsensusAlgorithm} uses long ids.
     */
    private final Long id;

    @Setter
    private Role role;

    /**
     * When new Node is created, id is auto-generated with a {@link Random} instance, and its role is always {@link Role#FOLLOWER}
     */
    public ConsensusNode() {
        id = new Random().nextLong();
        role = Role.FOLLOWER;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Role getRole() {
        return role;
    }
    
}
