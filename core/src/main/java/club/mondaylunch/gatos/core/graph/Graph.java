package club.mondaylunch.gatos.core.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.jetbrains.annotations.Nullable;

import club.mondaylunch.gatos.core.Either;
import club.mondaylunch.gatos.core.codec.SerializationUtils;
import club.mondaylunch.gatos.core.data.Conversions;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
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

    private final GraphObserver observer = new GraphObserver();

    public Graph() {
    }

    public Graph(Collection<Node> nodes, Map<UUID, NodeMetadata> metas, Collection<NodeConnection<?>> connections) {
        this();
        for (var node : nodes) {
            this.nodes.put(node.id(), node);
        }
        this.metadataByNode.putAll(metas);
        connections.forEach(this::addConnection);

        this.observer.reset();
    }

    /**
     * Returns an immutable set of all nodes in the graph.
     * @return the graph's nodes
     */
    public Set<Node> nodes() {
        return Set.copyOf(this.nodes.values());
    }

    /**
     * Adds a new Node to the graph of the given type. The new node is returned.
     *
     * @param type the type of node to add
     * @return the new node
     */
    public Node addNode(NodeType type) {
        var node = Node.create(type);
        this.nodes.put(node.id(), node);

        this.observer.nodeAdded(node);

        return node;
    }

    /**
     * Modifies, using the given unary operator, the node with a given UUID.
     *
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
        this.updateNodeConnections(id);

        this.observer.nodeModified(result);

        return result;
    }

    /**
     * Removes the node with the given UUID, if it exists.
     *
     * @param id the UUID of the node to remove
     */
    public void removeNode(UUID id) {
        @Nullable var conns = this.connectionsByNode.remove(id);
        if (conns != null) {
            conns.forEach(this::removeConnection);
        }

        @Nullable var oldNode = this.nodes.remove(id);
        @Nullable var oldMetaData = this.metadataByNode.remove(id);

        if (oldNode != null) {
            this.observer.nodeRemoved(oldNode);
        }
        if (oldMetaData != null) {
            this.observer.metadataRemoved(id, oldMetaData);
        }
    }

    /**
     * Gets the node with a given UUID from the graph, if it exists.
     *
     * @param id the UUID of the graph
     * @return the node with the UUID, or empty
     */
    public Optional<Node> getNode(UUID id) {
        return Optional.ofNullable(this.nodes.get(id));
    }

    /**
     * Whether this <em>exact</em> node exists in the graph.
     *
     * @param node the node
     * @return whether this node exists in the graph
     */
    public boolean containsNode(Node node) {
        return this.nodes.containsValue(node);
    }

    /**
     * Returns whether this graph has a node with the given UUID.
     *
     * @param id the UUID
     * @return whether there is a node with the given UUID
     */
    public boolean containsNode(UUID id) {
        return this.nodes.containsKey(id);
    }

    /**
     * Adds a new node connection between connectors of two existing nodes.
     *
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
        var destinationNodeConnections = this.getOrCreateConnectionsForNode(nodeTo.id());
        destinationNodeConnections.add(connection);
        this.observer.connectionAdded(connection);

        this.modifyNode(nodeTo.id(), n -> n.updateInputTypes(destinationNodeConnections.stream()
            .filter(c -> c.to().nodeId().equals(nodeTo.id()))
            .collect(Collectors.toMap(c -> c.to().name(), c -> this.getCanonicalConnector(c.from()).type()))));
    }

    /**
     * Removes invalid connections to and from this node,
     * and updates the types of any which are of the wrong type
     * but are compatible.
     * @param nodeId the node to update
     */
    private void updateNodeConnections(UUID nodeId) {
        var node = this.getNode(nodeId).orElseThrow();
        var connections = this.getConnectionsForNode(nodeId);
        Set<NodeConnection<?>> newConns = new HashSet<>();
        for (var conn : connections) {
            if (conn.from().nodeId().equals(nodeId)) {
                var connector = node.getOutputWithName(conn.from().name());
                if (connector.filter(newConnector -> newConnector.isCompatible(conn.from())).isPresent()) {
                    newConns.add(conn);
                }
            } else if (conn.to().nodeId().equals(nodeId)) {
                node.getInputWithName(conn.to().name()).ifPresent(connector -> {
                    if (!(connector.type().equals(conn.to().type()))) {
                        var canonicalFrom = this.getCanonicalConnector(conn.from());
                        if (Conversions.canConvert(canonicalFrom.type(), connector.type())) { // the 'from' here is on purpose
                            newConns.add(NodeConnection.create(this.getNode(conn.from().nodeId()).orElseThrow(), conn.from().name(), node, conn.to().name()));
                        }
                    } else {
                        newConns.add(conn);
                    }
                });
            }
        }

        for (var conn : connections) {
            if (!newConns.contains(conn)) {
                this.connections.remove(conn);
                this.getOrCreateConnectionsForNode(conn.from().nodeId()).remove(conn);
                this.getOrCreateConnectionsForNode(conn.to().nodeId()).remove(conn);
                this.observer.connectionRemoved(conn);
            }
        }

        for (var conn : newConns) {
            if (!this.connections.contains(conn)) {
                this.connections.add(conn);
                this.getOrCreateConnectionsForNode(conn.from().nodeId()).add(conn);
                this.getOrCreateConnectionsForNode(conn.to().nodeId()).add(conn);
                this.observer.connectionAdded(conn);
            }
        }
    }

    /**
     * Removes a node connection, if it exists. Beware that a connection you added
     * may not still be in the graph, even if nothing else changed the graph: best
     * to get the connection with {@link #getConnection(UUID, String, UUID, String)}
     * first.
     *
     * @param connection the connection to remove
     */
    public void removeConnection(NodeConnection<?> connection) {
        var removed = this.connections.remove(connection);
        this.getOrCreateConnectionsForNode(connection.from().nodeId()).remove(connection);
        var destinationNodeConnections = this.getOrCreateConnectionsForNode(connection.to().nodeId());
        destinationNodeConnections.remove(connection);
        if (removed) {
            this.observer.connectionRemoved(connection);
        }

        if (this.containsNode(connection.to().nodeId())) {
            this.modifyNode(connection.to().nodeId(), n -> n.updateInputTypes(destinationNodeConnections.stream()
                .filter(c -> c.to().nodeId().equals(connection.to().nodeId()))
                .collect(Collectors.toMap(c -> c.to().name(), c -> this.getCanonicalConnector(c.from()).type()))));
        }
    }

    /**
     * Returns a copy of the set of all connections in this graph.
     *
     * @return the connections in this graph
     */
    public Set<NodeConnection<?>> getConnections() {
        return new HashSet<>(this.connections);
    }

    /**
     * Retrieves all connections associated with a node UUID. Returns an empty set
     * if there is no node with the given UUID.
     *
     * @param nodeId the node UUID
     * @return all connections associated with the node with the UUID
     */
    public Set<NodeConnection<?>> getConnectionsForNode(UUID nodeId) {
        return new HashSet<>(this.getOrCreateConnectionsForNode(nodeId));
    }

    /**
     * Gets a connection from one node connector to another node connector.
     * @param fromId    the ID of the node the connection is from
     * @param fromName  the name of the connector the connection is from
     * @param toId      the ID of the node the connection is to
     * @param toName    the name of the connector the connection is to
     * @return          the connection, if it exists
     */
    public <T> Optional<NodeConnection<T>> getConnection(UUID fromId, String fromName, UUID toId, String toName) {
        return this.getConnectionsForNode(fromId).stream()
            .filter(conn -> conn.from().name().equals(fromName) && conn.to().nodeId().equals(toId) && conn.to().name().equals(toName))
            .map(conn -> (NodeConnection<T>) conn)
            .findFirst();
    }

    /**
     * Gets the <em>mutable</em> set of connections associated with a node UUID, or
     * creates it if it does not exist.
     * <p>
     * <strong>Changing the contents of this set without also changing
     * {@link #connections} can cause invalid state!</strong>
     * Use {@link #getConnectionsForNode(UUID)} where possible.
     * </p>
     *
     * @param nodeId the node UUID
     * @return the set of connections associated with the node with the UUID
     */
    private Set<NodeConnection<?>> getOrCreateConnectionsForNode(UUID nodeId) {
        return this.connectionsByNode.computeIfAbsent(nodeId, $ -> new HashSet<>());
    }

    /**
     * For an output connector which may have a modified type, gets the connector with the 'true' type.
     * If the connector does not exist, this method throws an exception.
     *
     * @param derivedConnector the connector to find the canonical representation of
     * @return the canonical representation of the connector
     */
    private NodeConnector.Output<?> getCanonicalConnector(NodeConnector.Output<?> derivedConnector) {
        return this.nodes.get(derivedConnector.nodeId()).getOutputWithName(derivedConnector.name()).orElseThrow();
    }

    /**
     * Gets the metadata for a node with the given UUID. Creates default metadata if
     * there is none specified.
     *
     * @param nodeId the node UUID
     * @return the metadata for the node with the UUID
     */
    public NodeMetadata getOrCreateMetadataForNode(UUID nodeId) {
        return this.metadataByNode.computeIfAbsent(nodeId, $ -> new NodeMetadata(0f, 0f));
    }

    /**
     * Modifies, using the given unary operator, the metadata for the node with a
     * given UUID.
     *
     * @param nodeId the UUID of the node to change
     * @param func   the unary operator giving the new node metadata from the old
     * @return the modified metadata
     * @throws NullPointerException if the unary operator returns null
     */
    public NodeMetadata modifyMetadata(UUID nodeId, UnaryOperator<NodeMetadata> func) {
        var hadMetadata = this.metadataByNode.containsKey(nodeId);
        var result = func.apply(this.getOrCreateMetadataForNode(nodeId));
        if (result == null) {
            throw new NullPointerException("The modify function returned null.");
        }

        this.metadataByNode.put(nodeId, result);
        if (hadMetadata) {
            this.observer.metadataModified(nodeId, result);
        } else {
            this.observer.metadataAdded(nodeId, result);
        }

        return result;
    }

    /**
     * Sets the metadata for a node with a given UUID.
     *
     * @param nodeId   the UUID of the node to change
     * @param metadata the new metadata
     * @throws NullPointerException if the metadata is null
     */
    public void setMetadata(UUID nodeId, NodeMetadata metadata) {
        this.modifyMetadata(nodeId, $ -> metadata);
    }

    /**
     * Determines whether this graph is <i>valid</i>.
     * A graph is valid if there is a path from a {@link NodeCategory#START input}
     * node to
     * an {@link NodeCategory#END output} node, and there are no cycles.
     *
     * @return a list of any errors in this graph
     */
    public List<GraphValidityError> validate() {
        List<GraphValidityError> errors = this.getExecutionOrder().map($ -> new ArrayList<>(), ArrayList::new);
        errors.addAll(this.nodes.values().stream()
            .flatMap(n -> n.type().isValid(n, this).stream())
            .toList());

        return errors;
    }

    /**
     * Performs a topological sort on this graph. Ignores nodes with no connections.
     * If the graph is not
     * {@link #validate() valid}, this will return an empty Optional.
     *
     * @return a topological sort of this graph, or a list of errors
     */
    public Either<List<Node>, List<GraphValidityError>> getExecutionOrder() {
        Set<UUID> relevantNodes = new HashSet<>(this.nodes.keySet());
        relevantNodes.removeIf(n -> this.getConnectionsForNode(n).isEmpty());
        Set<NodeConnection<?>> deduplicatedConnections = new HashSet<>();
        for (var conn : this.connections) {
            if (deduplicatedConnections.stream().noneMatch(conn2 -> conn2.to().nodeId().equals(conn.to().nodeId()) && conn2.from().nodeId().equals(conn.from().nodeId()))) {
                deduplicatedConnections.add(conn);
            }
        }

        boolean hasSeenInput = false;
        boolean hasSeenOutput = false;

        Set<NodeConnection<?>> visitedConnections = new HashSet<>();

        Deque<UUID> nodesWithoutIncoming = new ArrayDeque<>();
        for (var uuid : relevantNodes) {
            if (this.getConnectionsForNode(uuid).stream().noneMatch(c -> c.to().nodeId().equals(uuid))) {
                nodesWithoutIncoming.add(uuid);
            }
        }

        List<Node> res = new ArrayList<>();

        while (!nodesWithoutIncoming.isEmpty()) {
            UUID nodeId = nodesWithoutIncoming.pop();
            var node = this.nodes.get(nodeId);
            if (!res.contains(node)) {
                res.add(node);
            }

            if (!hasSeenInput && node.type().category() == NodeCategory.START) {
                hasSeenInput = true;
            }

            if (!hasSeenOutput && node.type().category() == NodeCategory.END) {
                hasSeenOutput = true;
            }

            for (NodeConnection<?> conn : this.getConnectionsForNode(nodeId)) {
                if (!visitedConnections.contains(conn)
                    && deduplicatedConnections.contains(conn)
                    && conn.from().nodeId().equals(nodeId)
                ) {
                    visitedConnections.add(conn);
                    UUID to = conn.to().nodeId();
                    if (!res.contains(this.getNode(to).orElseThrow())
                        && this.getConnectionsForNode(to).stream()
                        .filter(deduplicatedConnections::contains)
                        .filter(c -> c.to().nodeId().equals(to))
                        .allMatch(visitedConnections::contains)) {
                        nodesWithoutIncoming.add(to);
                    }
                }
            }
        }

        List<GraphValidityError> errors = new ArrayList<>();

        if (!hasSeenInput) {
            errors.add(GraphValidityError.noStart());
        }

        if (!hasSeenOutput) {
            errors.add(GraphValidityError.noEnd());
        }

        if (!visitedConnections.containsAll(deduplicatedConnections)) {
            errors.add(GraphValidityError.cycle());
        }

        if (!errors.isEmpty()) {
            return Either.right(errors);
        }

        return Either.left(res);
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
            return node.getOutputWithName(connection.from().name()).map(it ->
                it.isCompatible(connection.from())
            ).orElse(false);
        }

        if (connection.to().nodeId().equals(node.id())) {
            return node.getInputWithName(connection.to().name()).map(connection.to()::equals).orElse(false);
        }

        return false;
    }

    /**
     * Gets the number of nodes in this graph.
     *
     * @return the number of nodes
     */
    public int nodeCount() {
        return this.nodes.size();
    }

    /**
     * Gets the number of connections in this graph.
     *
     * @return the number of connections
     */
    public int connectionCount() {
        return this.connections.size();
    }

    public GraphObserver observer() {
        return this.observer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.nodes,
            this.connections,
            this.metadataByNode
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        } else {
            Graph graph = (Graph) obj;
            return Objects.equals(this.nodes, graph.nodes)
                && Objects.equals(this.connections, graph.connections)
                && Objects.equals(this.metadataByNode, graph.metadataByNode);
        }
    }

    @Override
    public String toString() {
        return "Graph{"
            + "nodes=" + this.nodes
            + ", connections=" + this.connections
            + ", metadataByNode=" + this.metadataByNode
            + '}';
    }

    public static final class GraphCodec implements Codec<Graph> {
        private final CodecRegistry registry;

        public GraphCodec(CodecRegistry registry) {
            this.registry = registry;
        }

        @Override
        public Graph decode(BsonReader reader, DecoderContext decoderContext) {
            return SerializationUtils.readDocument(reader, () -> {
                reader.readName("nodes");
                Set<Node> nodes = SerializationUtils.readSet(reader, decoderContext, Node.class, this.registry);
                reader.readName("connections");
                Set<NodeConnection<?>> connections = SerializationUtils.readSet(reader, decoderContext, NodeConnection.class, this.registry);
                reader.readName("metadata");
                Map<UUID, NodeMetadata> metadata = SerializationUtils.readMap(reader, decoderContext, NodeMetadata.class, UUID::fromString, this.registry);

                // adding connections in certain ways makes the graph sad, lets filter out connections that would do that
                var nodeIds = nodes.stream().map(Node::id).collect(Collectors.toSet());
                // list for faster iteration b/c we're gonna be doing a lot of streams
                List<NodeConnection<?>> filteredConnections = new ArrayList<>();
                for (var conn : connections) {
                    if (nodeIds.contains(conn.from().nodeId())
                        && nodeIds.contains(conn.to().nodeId())
                        && filteredConnections.stream().noneMatch(c -> c.to().equals(conn.to()))
                    ) {
                        filteredConnections.add(conn);
                    }
                }

                return new Graph(nodes, metadata, filteredConnections);
            });
        }

        @Override
        public void encode(BsonWriter writer, Graph value, EncoderContext encoderContext) {
            SerializationUtils.writeDocument(writer, () -> {
                writer.writeName("nodes");
                SerializationUtils.writeSet(writer, encoderContext, Node.class, this.registry, Set.copyOf(value.nodes.values()));
                writer.writeName("connections");
                SerializationUtils.writeSet(writer, encoderContext, NodeConnection.class, this.registry, value.connections);
                writer.writeName("metadata");
                SerializationUtils.writeMap(writer, encoderContext, NodeMetadata.class, UUID::toString, this.registry, value.metadataByNode);
            });
        }

        @Override
        public Class<Graph> getEncoderClass() {
            return Graph.class;
        }
    }
}
