package org.nicbrerod.scripts.manager.distributed.utils.model.custom;

import java.util.Random;

import org.nicbrerod.scripts.manager.distributed.utils.model.common.Node;
import org.nicbrerod.scripts.manager.distributed.utils.model.common.Role;

import lombok.Setter;

public class ConsensusNode implements Node<Long> {

    private final Long id;

    @Setter
    private Role role;

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
