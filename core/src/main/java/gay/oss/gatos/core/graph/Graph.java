package gay.oss.gatos.core.graph;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * A flow graph.
 *
 * <p>The graph is made of {@link Node nodes} (addressable by UUID), and {@link NodeConnection edges} between them.</p>
 *
 * <p>In addition, {@link NodeMetadata extra metadata} is stored per-node.</p>
 */
public class Graph {
    /**
     * The nodes of this graph.
     */
    private final Map<UUID, Node> nodes = new HashMap<>();
    /**
     * The edges of this graph.
     */
    private final Set<NodeConnection<?>> connections = new HashSet<>();
    /**
     * The edges of this graph, easily retrievable by their associated nodes' UUIDs.
     */
    private final Map<UUID, Set<NodeConnection<?>>> connectionsByNode = new HashMap<>();

    /**
     * The metadata of each node in the graph.
     */
    private final Map<UUID, NodeMetadata> metadataByNode = new HashMap<>();

    /**
     * Adds a new Node to the graph of the given type. The new node is returned.
     *
     * @param type  the type of node to add
     * @return      the new node
     */
    public Node addNode(NodeType type) {
        var node = Node.create(type);
        this.nodes.put(node.id(), node);
        return node;
    }

    /**
     * Modifies, using the given unary operator, the node with a given UUID.
     * @param id    the UUID of the node to change
     * @param func  the unary operator giving the new node state from the old
     * @return      the modified node
     * @throws IllegalArgumentException if there is no node with the given UUID
     * @throws NullPointerException     if the unary operator returns null
     */
    public Node modifyNode(UUID id, UnaryOperator<Node> func) {
        var node = this.nodes.get(id);
        if (node == null) {
            throw new IllegalArgumentException("There is no node with that UUID.");
        }

        var result = func.apply(node);
        if (result == null) {
            throw new NullPointerException("The modify function returned null.");
        }

        this.nodes.put(id, result);
        var invalidConns = this.getConnectionsForNode(id);
        invalidConns.removeIf(c -> isConnectionValid(node, c));
        this.connections.removeAll(invalidConns);
        this.connectionsByNode.get(id).removeAll(invalidConns);

        return result;
    }

    /**
     * Removes the node with the given UUID, if it exists.
     * @param id    the UUID of the node to remove
     */
    public void removeNode(UUID id) {
        this.nodes.remove(id);
        var conns = this.connectionsByNode.remove(id);
        conns.forEach(this.connections::remove);
    }

    /**
     * Adds a new node connection between connectors of two existing nodes.
     * @param connection    the connection to add
     * @throws IllegalArgumentException if the node connection has a null node at either end
     * @throws IllegalArgumentException if the node connection has a connector already involved in a connection at either end
     */
    public void addConnection(NodeConnection<?> connection) {
        var nodeFrom = this.nodes.get(connection.from().nodeId());
        var nodeTo = this.nodes.get(connection.to().nodeId());
        if (nodeFrom == null) {
            throw new IllegalArgumentException("Node connection cannot be from a nonexistent node.");
        }
        if (nodeTo == null) {
            throw new IllegalArgumentException("Node connection cannot be to a nonexistent node.");
        }
        if (this.connections.stream().anyMatch(conn -> conn.to().equals(connection.to()) || conn.from().equals(connection.from()))) {
            throw new IllegalArgumentException("Node connection cannot be to or from connectors which are already part of a connection.");
        }

        this.connections.add(connection);
        this.getOrCreateConnectionsForNode(nodeFrom.id()).add(connection);
        this.getOrCreateConnectionsForNode(nodeTo.id()).add(connection);
    }

    /**
     * Removes a node connection, if it exists.
     * @param connection    the connection to remove
     */
    public void removeConnection(NodeConnection<?> connection) {
        this.connections.remove(connection);
        this.getOrCreateConnectionsForNode(connection.from().nodeId()).remove(connection);
        this.getOrCreateConnectionsForNode(connection.to().nodeId()).remove(connection);
    }

    /**
     * Retrieves all connections associated with a node UUID. Returns an empty set if there is no node with the given UUID.
     * @param nodeId    the node UUID
     * @return          all connections associated with the node with the UUID
     */
    public Set<NodeConnection<?>> getConnectionsForNode(UUID nodeId) {
        return new HashSet<>(this.getOrCreateConnectionsForNode(nodeId));
    }

    /**
     * Gets the <em>mutable</em> set of connections associated with a node UUID, or creates it if it does not exist.
     * <p><strong>Changing the contents of this set without also changing {@link #connections} can cause invalid state!</strong>
     * Use {@link #getConnectionsForNode(UUID)} where possible.</p>
     * @param nodeId    the node UUID
     * @return          the set of connections associated with the node with the UUID
     */
    private Set<NodeConnection<?>> getOrCreateConnectionsForNode(UUID nodeId) {
        return this.connectionsByNode.computeIfAbsent(nodeId, $ -> new HashSet<>());
    }

    /**
     * Gets the metadata for a node with the given UUID. Creates default metadata if there is none specified.
     * @param nodeId    the node UUID
     * @return          the metadata for the node with the UUID
     */
    public NodeMetadata getOrCreateMetadataForNode(UUID nodeId) {
        return this.metadataByNode.computeIfAbsent(nodeId, $ -> new NodeMetadata(0f, 0f));
    }

    /**
     * Modifies, using the given unary operator, the metadata for the node with a given UUID.
     * @param nodeId    the UUID of the node to change
     * @param func      the unary operator giving the new node metadata from the old
     * @return          the modified metadata
     * @throws NullPointerException     if the unary operator returns null
     */
    public NodeMetadata modifyMetadata(UUID nodeId, UnaryOperator<NodeMetadata> func) {
        var result = func.apply(this.getOrCreateMetadataForNode(nodeId));
        if (result == null) {
            throw new NullPointerException("The modify function returned null.");
        }

        this.metadataByNode.put(nodeId, result);
        return result;
    }

    /**
     * Determines whether either of a node connection's connectors is present on the given node.
     *
     * @param node          the node
     * @param connection    the connection to check
     * @return              whether the connection is valid for the given node
     */
    private static boolean isConnectionValid(Node node, NodeConnection<?> connection) {
        if (connection.from().nodeId().equals(node.id())) {
            return node.outputs().contains(connection.from());
        }

        if (connection.to().nodeId().equals(node.id())) {
            return node.inputs().contains(connection.to());
        }

        return false;
    }
}
