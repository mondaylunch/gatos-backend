package gay.oss.gatos.core.graph;

import java.util.*;
import java.util.function.UnaryOperator;

public class Graph {
    private final Map<UUID, Node> nodes = new HashMap<>();
    private final Set<NodeConnection<?>> connections = new HashSet<>();
    private final Map<UUID, Set<NodeConnection<?>>> connectionsByNode = new HashMap<>();

    public Node addNode(NodeType type) {
        var node = Node.create(type);
        this.nodes.put(node.id(), node);
        return node;
    }

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

    public void removeNode(UUID id) {
        this.nodes.remove(id);
        var conns = this.connectionsByNode.remove(id);
        conns.forEach(this.connections::remove);
    }

    public void addConnection(NodeConnection<?> connection) {
        var nodeFrom = this.nodes.get(connection.from().nodeId());
        var nodeTo = this.nodes.get(connection.to().nodeId());
        if (nodeFrom == null) {
            throw new IllegalArgumentException("Node connection cannot be from a nonexistent node.");
        }
        if (nodeTo == null) {
            throw new IllegalArgumentException("Node connection cannot be to a nonexistent node.");
        }

        this.connections.add(connection);
        this.getOrCreateConnectionsForNode(nodeFrom.id()).add(connection);
        this.getOrCreateConnectionsForNode(nodeTo.id()).add(connection);
    }

    public void removeConnection(NodeConnection<?> connection) {
        this.connections.remove(connection);
        this.getOrCreateConnectionsForNode(connection.from().nodeId()).remove(connection);
        this.getOrCreateConnectionsForNode(connection.to().nodeId()).remove(connection);
    }

    public Set<NodeConnection<?>> getConnectionsForNode(UUID nodeId) {
        return new HashSet<>(this.getOrCreateConnectionsForNode(nodeId));
    }

    private Set<NodeConnection<?>> getOrCreateConnectionsForNode(UUID nodeId) {
        return this.connectionsByNode.computeIfAbsent(nodeId, $ -> new HashSet<>());
    }

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
