package club.mondaylunch.gatos.core.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.type.NodeCategory;
import club.mondaylunch.gatos.core.graph.type.NodeType;

/**
 * A flow graph.
 *
 * <p>
 * The graph is made of {@link Node nodes} (addressable by UUID), and
 * {@link NodeConnection edges} between them.
 * </p>
 *
 * <p>
 * In addition, {@link NodeMetadata extra metadata} is stored per-node.
 * </p>
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
     * @param type the type of node to add
     * @return the new node
     */
    public Node addNode(NodeType type) {
        var node = Node.create(type);
        this.nodes.put(node.id(), node);
        return node;
    }

    /**
     * Modifies, using the given unary operator, the node with a given UUID.
     * @param id   the UUID of the node to change
     * @param func the unary operator giving the new node state from the old
     * @return the modified node
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
     * @param id the UUID of the node to remove
     */
    public void removeNode(UUID id) {
        this.nodes.remove(id);
        this.metadataByNode.remove(id);
        @Nullable
        var conns = this.connectionsByNode.remove(id);
        if (conns == null) {
            return;
        }
        conns.forEach(this::removeConnection);
    }

    /**
     * Whether this <em>exact</em> node exists in the graph.
     * @param node the node
     * @return whether this node exists in the graph
     */
    public boolean containsNode(Node node) {
        return this.nodes.containsValue(node);
    }

    /**
     * Returns whether this graph has a node with the given UUID.
     * @param id the UUID
     * @return whether there is a node with the given UUID
     */
    public boolean containsNode(UUID id) {
        return this.nodes.containsKey(id);
    }

    /**
     * Adds a new node connection between connectors of two existing nodes.
     * @param connection the connection to add
     * @throws IllegalArgumentException if the node connection has a null node at
     *                                  either end
     * @throws IllegalArgumentException if the node connection is to a connector
     *                                  already involved in a connection
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
        if (this.connections.stream().anyMatch(conn -> conn.to().equals(connection.to()))) {
            throw new IllegalArgumentException(
                    "Node connection cannot be to a connector which is already part of a connection.");
        }

        this.connections.add(connection);
        this.getOrCreateConnectionsForNode(nodeFrom.id()).add(connection);
        this.getOrCreateConnectionsForNode(nodeTo.id()).add(connection);
    }

    /**
     * Removes a node connection, if it exists.
     * @param connection the connection to remove
     */
    public void removeConnection(NodeConnection<?> connection) {
        this.connections.remove(connection);
        this.getOrCreateConnectionsForNode(connection.from().nodeId()).remove(connection);
        this.getOrCreateConnectionsForNode(connection.to().nodeId()).remove(connection);
    }

    /**
     * Returns a copy of the set of all connections in this graph.
     * @return the connections in this graph
     */
    public Set<NodeConnection<?>> getConnections() {
        return new HashSet<>(this.connections);
    }

    /**
     * Retrieves all connections associated with a node UUID. Returns an empty set
     * if there is no node with the given UUID.
     * @param nodeId the node UUID
     * @return all connections associated with the node with the UUID
     */
    public Set<NodeConnection<?>> getConnectionsForNode(UUID nodeId) {
        return new HashSet<>(this.getOrCreateConnectionsForNode(nodeId));
    }

    /**
     * Gets the <em>mutable</em> set of connections associated with a node UUID, or
     * creates it if it does not exist.
     * <p>
     * <strong>Changing the contents of this set without also changing
     * {@link #connections} can cause invalid state!</strong>
     * Use {@link #getConnectionsForNode(UUID)} where possible.
     * </p>
     * @param nodeId the node UUID
     * @return the set of connections associated with the node with the UUID
     */
    private Set<NodeConnection<?>> getOrCreateConnectionsForNode(UUID nodeId) {
        return this.connectionsByNode.computeIfAbsent(nodeId, $ -> new HashSet<>());
    }

    /**
     * Gets the metadata for a node with the given UUID. Creates default metadata if
     * there is none specified.
     * @param nodeId the node UUID
     * @return the metadata for the node with the UUID
     */
    public NodeMetadata getOrCreateMetadataForNode(UUID nodeId) {
        return this.metadataByNode.computeIfAbsent(nodeId, $ -> new NodeMetadata(0f, 0f));
    }

    /**
     * Modifies, using the given unary operator, the metadata for the node with a
     * given UUID.
     * @param nodeId the UUID of the node to change
     * @param func   the unary operator giving the new node metadata from the old
     * @return the modified metadata
     * @throws NullPointerException if the unary operator returns null
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
     * Determines whether this graph is <i>valid</i>.
     * A graph is valid if there is a path from a {@link NodeCategory#START input}
     * node to
     * an {@link NodeCategory#END output} node, and there are no cycles.
     *
     * @return whether this graph is valid
     */
    public boolean validate() {
        return this.getExecutionOrder().isPresent();
    }

    /**
     * Performs a topological sort on this graph. Ignores nodes with no connections.
     * If the graph is not
     * {@link #validate() valid}, this will return an empty Optional.
     * @return a topological sort of this graph
     */
    public Optional<List<Node>> getExecutionOrder() {
        Set<UUID> relevantNodes = new HashSet<>(this.nodes.keySet());
        relevantNodes.removeIf(n -> this.getConnectionsForNode(n).isEmpty());

        boolean hasSeenInput = false;
        boolean hasSeenOutput = false;

        Set<NodeConnection<?>> visitedConnections = new HashSet<>();

        Deque<UUID> nodesWithoutIncoming = new ArrayDeque<>();
        for (var uuid : relevantNodes) {
            if (this.getConnectionsForNode(uuid).stream().noneMatch(c -> c.to().nodeId() == uuid)) {
                nodesWithoutIncoming.add(uuid);
            }
        }

        List<Node> res = new ArrayList<>();

        while (!nodesWithoutIncoming.isEmpty()) {
            UUID nodeId = nodesWithoutIncoming.pop();
            var node = this.nodes.get(nodeId);
            res.add(node);

            if (!hasSeenInput && node.type().category() == NodeCategory.START) {
                hasSeenInput = true;
            }

            if (!hasSeenOutput && node.type().category() == NodeCategory.END) {
                hasSeenOutput = true;
            }

            for (NodeConnection<?> conn : this.getConnectionsForNode(nodeId)) {
                if (!visitedConnections.contains(conn) && conn.from().nodeId().equals(nodeId)) {
                    visitedConnections.add(conn);
                    UUID to = conn.to().nodeId();
                    if (this.getConnectionsForNode(to).stream()
                            .noneMatch(c -> !visitedConnections.contains(c) && c.to().nodeId() == to)) {
                        nodesWithoutIncoming.add(to);
                    }
                }
            }
        }

        if (!hasSeenInput || !hasSeenOutput || !visitedConnections.containsAll(this.connections)) {
            return Optional.empty();
        }

        return Optional.of(res);
    }

    /**
     * Determines whether either of a node connection's connectors is present on the
     * given node.
     *
     * @param node       the node
     * @param connection the connection to check
     * @return whether the connection is valid for the given node
     */
    private static boolean isConnectionValid(Node node, NodeConnection<?> connection) {
        if (connection.from().nodeId().equals(node.id())) {
            return node.outputs().containsValue(connection.from());
        }

        if (connection.to().nodeId().equals(node.id())) {
            return node.inputs().containsValue(connection.to());
        }

        return false;
    }
}