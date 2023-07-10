package org.nicbrerod.scripts.manager.node.ws;

import java.util.Set;
import java.util.stream.Collectors;

import org.nicbrerod.scripts.manager.node.ScriptManagerClusterNode;
import org.nicbrerod.scripts.manager.node.model.BaseNodeInfo;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Service used to retrieve information about node and their neighbours
 */
@Path("/node")
public class NodeApi {
    
    /**
     * Node created in the system
     */
    @Inject
    ScriptManagerClusterNode node;

    /**
     * Used to get basic information about server's node
     * @return Information about current node, like their id, their term,...
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public BaseNodeInfo getNodeInfo() {
        return new BaseNodeInfo(node.getId(), node.getTerm(), node.imLeader());
    }

    /**
     * Used to get basic information about current node's neighbours nodes, this is, the other nodes 
     * inside its cluster
     * @return Information of each node registered in same cluster of current node
     */
    @GET
    @Path("/neighbours")
    public Set<BaseNodeInfo> getRegisteredNodes() {
        return node.getClusterNodesInfo().values().stream().map(info -> new BaseNodeInfo(info.getSender(), info.getTerm(), info.isLeader())).collect(Collectors.toSet());
    }
}
