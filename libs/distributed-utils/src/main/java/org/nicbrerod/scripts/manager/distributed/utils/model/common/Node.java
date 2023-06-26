package org.nicbrerod.scripts.manager.distributed.utils.model.common;

import java.io.Serializable;

public interface Node<I extends Serializable & Comparable<I>> {
    
    Role getRole();

    I getId();
}
