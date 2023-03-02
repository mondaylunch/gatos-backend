package club.mondaylunch.gatos.core.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.mongodb.client.model.Updates;
import org.bson.conversions.Bson;

import club.mondaylunch.gatos.core.graph.connector.NodeConnection;

public class GraphObserver {

    private final Map<UUID, Node> addedNodes = new HashMap<>();
    private final Map<UUID, Node> modifiedNodes = new HashMap<>();
    private final Set<UUID> removedNodesIds = new HashSet<>();
    private final Set<NodeConnection<?>> addedConnections = new HashSet<>();
    private final Set<NodeConnection<?>> removedConnections = new HashSet<>();
    private final Map<UUID, NodeMetadata> addedMetadata = new HashMap<>();
    private final Map<UUID, NodeMetadata> modifiedMetadata = new HashMap<>();
    private final Set<UUID> removedMetadataIds = new HashSet<>();

    public void nodeAdded(Node node) {
        this.addedNodes.put(node.id(), node);
    }

    public void nodeModified(Node node) {
        this.modifiedNodes.put(node.id(), node);
    }

    public void nodeRemoved(UUID nodeId) {
        this.removedNodesIds.add(nodeId);
        this.addedNodes.remove(nodeId);
        this.modifiedNodes.remove(nodeId);
    }

    public void connectionAdded(NodeConnection<?> connection) {
        this.addedConnections.add(connection);
    }

    public void connectionRemoved(NodeConnection<?> connection) {
        this.removedConnections.add(connection);
        this.addedConnections.remove(connection);
    }

    public void connectionsRemoved(Collection<NodeConnection<?>> connections) {
        this.removedConnections.addAll(connections);
        this.addedConnections.removeAll(connections);
    }

    public void metadataAdded(UUID nodeId, NodeMetadata metadata) {
        this.addedMetadata.put(nodeId, metadata);
    }

    public void metadataModified(UUID nodeId, NodeMetadata metadata) {
        this.modifiedMetadata.put(nodeId, metadata);
    }

    public void metadataRemoved(UUID nodeId) {
        this.removedMetadataIds.add(nodeId);
        this.addedMetadata.remove(nodeId);
        this.modifiedMetadata.remove(nodeId);
    }

    public Optional<Bson> createFlowUpdate() {
        List<Bson> updates = new ArrayList<>();

        if (!this.addedNodes.isEmpty()) {
            var addedNodes = Updates.pushEach("graph.nodes", new ArrayList<>(this.addedNodes.values()));
            updates.add(addedNodes);
        }

        if (updates.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(Updates.combine(updates));
        }
    }

    public void reset() {
        this.addedNodes.clear();
        this.modifiedNodes.clear();
        this.removedNodesIds.clear();
        this.addedConnections.clear();
        this.removedConnections.clear();
        this.addedMetadata.clear();
        this.removedMetadataIds.clear();
    }
}
