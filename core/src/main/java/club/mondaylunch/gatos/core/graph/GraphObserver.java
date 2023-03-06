package club.mondaylunch.gatos.core.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.conversions.Bson;

import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.models.Flow;

public class GraphObserver {

    private final Map<UUID, Node> addedNodes = new HashMap<>();
    private final Map<UUID, Node> modifiedNodes = new HashMap<>();
    private final Map<UUID, Node> removedNodes = new HashMap<>();
    private final Map<ConnectionId, NodeConnection<?>> addedConnections = new HashMap<>();
    private final Map<ConnectionId, NodeConnection<?>> modifiedConnections = new HashMap<>();
    private final Map<ConnectionId, NodeConnection<?>> removedConnections = new HashMap<>();
    private final Map<UUID, NodeMetadata> addedMetadata = new HashMap<>();
    private final Map<UUID, NodeMetadata> modifiedMetadata = new HashMap<>();
    private final Map<UUID, NodeMetadata> removedMetadata = new HashMap<>();

    public void nodeAdded(Node node) {
        added(node.id(), node, this.addedNodes, this.modifiedNodes, this.removedNodes);
    }

    public void nodeModified(Node node) {
        modified(node.id(), node, this.addedNodes, this.modifiedNodes, this.removedNodes);
    }

    public void nodeRemoved(Node node) {
        removed(node.id(), node, this.addedNodes, this.modifiedNodes, this.removedNodes);
    }

    public void connectionAdded(NodeConnection<?> connection) {
        ConnectionId id = new ConnectionId(connection);
        added(id, connection, this.addedConnections, this.modifiedConnections, this.removedConnections);
    }

    public void connectionRemoved(NodeConnection<?> connection) {
        ConnectionId id = new ConnectionId(connection);
        removed(id, connection, this.addedConnections, this.modifiedConnections, this.removedConnections);
    }

    public void metadataAdded(UUID nodeId, NodeMetadata metadata) {
        added(nodeId, metadata, this.addedMetadata, this.modifiedMetadata, this.removedMetadata);
    }

    public void metadataModified(UUID nodeId, NodeMetadata metadata) {
        modified(nodeId, metadata, this.addedMetadata, this.modifiedMetadata, this.removedMetadata);
    }

    public void metadataRemoved(UUID nodeId, NodeMetadata metadata) {
        removed(nodeId, metadata, this.addedMetadata, this.modifiedMetadata, this.removedMetadata);
    }

    private static <K, V> void added(K key, V value, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        if (modified.containsKey(key)) {
            /*
            If a value with a key was modified when
            a value with the same key is added, set
            the modified value to the added value.
             */
            modified.put(key, value);
        } else if (removed.containsKey(key)) {
            /*
            If a value with a key was removed when a
            value with the same key is added, it is
            not removed anymore.
             */
            removed.remove(key);
            if (!value.equals(removed.get(key))) {
                /*
                If the contents of the removed value
                and the added value are not the same,
                the value becomes a modified value.
                 */
                modified.put(key, value);
            }
        } else {
            // Otherwise, the value is added.
            added.put(key, value);
        }
    }

    private static <K, V> void modified(K key, V value, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        if (added.containsKey(key)) {
            /*
            If a value with a key was added when a
            value with the same key is modified,
            the added value is replaced with the
            modified value.
             */
            added.put(key, value);
        } else if (removed.containsKey(key)) {
            /*
            If a value that was removed cannot at
            be modified.
             */
            throw new IllegalStateException("Cannot modify a " + value.getClass().getSimpleName() + " that has been removed");
        } else {
            // Otherwise, the value is modified.
            modified.put(key, value);
        }
    }

    private static <K, V> void removed(K key, V value, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        removed.put(key, value);
        added.remove(key);
        modified.remove(key);
    }

    /**
     * Creates a Bson update for the graph
     * from the changes seen by this observer
     * since the last time {@link #reset()}
     * was called, and applies it to the collection.
     *
     * @param flowId     The ID of the flow to update.
     * @param collection The collection to update.
     */
    public void updateFlow(UUID flowId, MongoCollection<Flow> collection) {
        this.validate();
        List<Bson> updates = new ArrayList<>();

        this.updateAddNode(updates);
        this.updateModifyNode(flowId, collection);
        this.updateRemoveNode(updates);

        this.updateAddConnection(updates);
        this.updateModifyConnection(flowId, collection);
        this.updateRemoveConnection(updates);

        this.updateAddMetadata(updates);
        this.updateModifyMetadata(updates);
        this.updateRemoveMetadata(flowId, collection);

        if (!updates.isEmpty()) {
            var update = Updates.combine(updates);
            collection.updateOne(Filters.eq(flowId), update);
        }
    }

    private void validate() {
        var nodeShared = sharedValues(
            this.addedNodes.keySet(),
            this.modifiedNodes.keySet(),
            this.removedNodes.keySet()
        );
        if (nodeShared) {
            throw new IllegalStateException("Node updates are in multiple states");
        }
        var connectionShared = sharedValues(
            this.addedConnections.keySet(),
            this.modifiedConnections.keySet(),
            this.removedConnections.keySet()
        );
        if (connectionShared) {
            throw new IllegalStateException("Connection updates are in multiple states");
        }
        var metadataShared = sharedValues(
            this.addedMetadata.keySet(),
            this.modifiedMetadata.keySet(),
            this.removedMetadata.keySet()
        );
        if (metadataShared) {
            throw new IllegalStateException("Metadata updates are in multiple states");
        }
    }

    @SafeVarargs
    private static <T> boolean sharedValues(Set<T> set1, Set<T> set2, Set<T>... sets) {
        return !intersection(set1, set2, sets).isEmpty();
    }

    @SafeVarargs
    private static <T> Set<T> intersection(Set<T> set1, Set<T> set2, Set<T>... sets) {
        Set<T> result = new HashSet<>(set1);
        result.retainAll(set2);
        for (Set<T> set : sets) {
            result.retainAll(set);
        }
        return result;
    }

    private void updateAddNode(Collection<Bson> updates) {
        if (!this.addedNodes.isEmpty()) {
            var addedNodes = Updates.pushEach("graph.nodes", new ArrayList<>(this.addedNodes.values()));
            updates.add(addedNodes);
        }
    }

    private void updateModifyNode(UUID flowId, MongoCollection<Flow> collection) {
        for (var modified : this.modifiedNodes.values()) {
            var filter = Filters.and(Filters.eq(flowId), Filters.eq("graph.nodes.id", modified.id()));
            var update = Updates.set("graph.nodes.$", modified);
            collection.updateOne(filter, update);
        }
    }

    private void updateRemoveNode(Collection<Bson> updates) {
        if (!this.removedNodes.isEmpty()) {
            var filter = Filters.in("id", this.removedNodes.keySet());
            var removedNodes = Updates.pullByFilter(new BasicDBObject("graph.nodes", filter));
            updates.add(removedNodes);
        }
    }

    private void updateAddConnection(Collection<Bson> updates) {
        if (!this.addedConnections.isEmpty()) {
            var addedNodes = Updates.pushEach("graph.connections", new ArrayList<>(this.addedConnections.values()));
            updates.add(addedNodes);
        }
    }

    private void updateModifyConnection(UUID flowId, MongoCollection<Flow> collection) {
        for (var modified : this.modifiedConnections.values()) {
            var filter = Filters.and(
                Filters.eq(flowId),
                Filters.eq("graph.connections.output.nodeId", modified.from().nodeId()),
                Filters.eq("graph.connections.input.nodeId", modified.to().nodeId())
            );
            var update = Updates.set("graph.connections.$", modified);
            collection.updateOne(filter, update);
        }
    }

    private void updateRemoveConnection(Collection<Bson> updates) {
        for (var removed : this.removedConnections.values()) {
            var filter = Filters.and(
                Filters.eq("output.nodeId", removed.from().nodeId()),
                Filters.eq("input.nodeId", removed.to().nodeId())
            );
            var removedConnections = Updates.pullByFilter(new BasicDBObject("graph.connections", filter));
            updates.add(removedConnections);
        }
    }

    private void updateAddMetadata(Collection<Bson> updates) {
        for (var entry : this.addedMetadata.entrySet()) {
            setMetadata(entry.getKey(), entry.getValue(), updates);
        }
    }

    private void updateModifyMetadata(Collection<Bson> updates) {
        for (var entry : this.modifiedMetadata.entrySet()) {
            setMetadata(entry.getKey(), entry.getValue(), updates);
        }
    }

    private static void setMetadata(UUID nodeId, NodeMetadata metadata, Collection<Bson> updates) {
        var update = Updates.set("graph.metadata." + nodeId, metadata);
        updates.add(update);
    }

    private void updateRemoveMetadata(UUID flowId, MongoCollection<Flow> collection) {
        for (var nodeId : this.removedMetadata.keySet()) {
            var filter = Filters.and(Filters.eq(flowId));
            var update = Updates.unset("graph.metadata." + nodeId);
            collection.updateOne(filter, update);
        }
    }

    /**
     * Resets the observer, clearing all changes.
     */
    public void reset() {
        this.addedNodes.clear();
        this.modifiedNodes.clear();
        this.removedNodes.clear();

        this.addedConnections.clear();
        this.modifiedConnections.clear();
        this.removedConnections.clear();

        this.addedMetadata.clear();
        this.modifiedMetadata.clear();
        this.removedMetadata.clear();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            GraphObserver that = (GraphObserver) obj;
            return Objects.equals(this.addedNodes, that.addedNodes)
                && Objects.equals(this.modifiedNodes, that.modifiedNodes)
                && Objects.equals(this.removedNodes, that.removedNodes)
                && Objects.equals(this.addedConnections, that.addedConnections)
                && Objects.equals(this.modifiedConnections, that.modifiedConnections)
                && Objects.equals(this.removedConnections, that.removedConnections)
                && Objects.equals(this.addedMetadata, that.addedMetadata)
                && Objects.equals(this.modifiedMetadata, that.modifiedMetadata)
                && Objects.equals(this.removedMetadata, that.removedMetadata);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.addedNodes,
            this.modifiedNodes,
            this.removedNodes,
            this.addedConnections,
            this.modifiedConnections,
            this.removedConnections,
            this.addedMetadata,
            this.modifiedMetadata,
            this.removedMetadata
        );
    }

    @Override
    public String toString() {
        return "GraphObserver{"
            + "addedNodes=" + this.addedNodes
            + ", modifiedNodes=" + this.modifiedNodes
            + ", removedNodes=" + this.removedNodes
            + ", addedConnections=" + this.addedConnections
            + ", modifiedConnections=" + this.modifiedConnections
            + ", removedConnections=" + this.removedConnections
            + ", addedMetadata=" + this.addedMetadata
            + ", modifiedMetadata=" + this.modifiedMetadata
            + ", removedMetadata=" + this.removedMetadata
            + '}';
    }

    private record ConnectionId(UUID fromId, UUID toId) {
        ConnectionId(NodeConnection<?> connection) {
            this(connection.from().nodeId(), connection.to().nodeId());
        }
    }
}
