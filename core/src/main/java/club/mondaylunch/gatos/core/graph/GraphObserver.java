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
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;
import org.bson.conversions.Bson;

import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.models.Flow;

/**
 * Tracks changes to a {@link Graph} so that when
 * it is updated in the database, only the parts
 * that changed are written to the database,
 * instead of the entire graph being overwritten.
 */
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

    /**
     * Records changes for when a graph component
     * is added.
     *
     * @param key      The key of the component.
     * @param value    The component.
     * @param added    The currently added components.
     * @param modified The currently modified components.
     * @param removed  The currently removed components.
     * @param <K>      The type of the key.
     * @param <V>      The type of the component.
     */
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
            var removedValue = removed.remove(key);
            if (!value.equals(removedValue)) {
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

    /**
     * Records changes for when a graph component
     * is modified.
     *
     * @param key      The key of the component.
     * @param value    The component.
     * @param added    The currently added components.
     * @param modified The currently modified components.
     * @param removed  The currently removed components.
     * @param <K>      The type of the key.
     * @param <V>      The type of the component.
     */
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

    /**
     * Records changes for when a graph component
     * is removed.
     *
     * @param key      The key of the component.
     * @param value    The component.
     * @param added    The currently added components.
     * @param modified The currently modified components.
     * @param removed  The currently removed components.
     * @param <K>      The type of the key.
     * @param <V>      The type of the component.
     */
    private static <K, V> void removed(K key, V value, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        if (added.containsKey(key)) {
            /*
            If a value with a key was added when a
            value with the same key is removed, it
            is not added anymore.
             */
            added.remove(key);
        } else if (modified.containsKey(key)) {
            /*
            If a value with a key was modified when
            a value with the same key is removed, it
            is not modified anymore, and the value
            is removed.
             */
            modified.remove(key);
            removed.put(key, value);
        } else {
            // Otherwise, the value is removed.
            removed.put(key, value);
        }
    }

    /**
     * Creates {@link Bson} updates for the graph
     * from the changes seen by this observer
     * since the last time the observer was
     * {@link #reset() reset}, and applies it to
     * the collection.
     *
     * @param flowId     The ID of the flow to update.
     * @param collection The collection to update.
     */
    public void updateFlow(UUID flowId, MongoCollection<Flow> collection) {
        this.validate();
        List<Bson> bsonUpdates = new ArrayList<>();
        List<WriteModel<Flow>> writeModelUpdates = new ArrayList<>();
        var flowIdFilter = Filters.eq(flowId);

        this.updateAddNode(bsonUpdates);
        this.updateModifyNode(flowIdFilter, writeModelUpdates);
        this.updateRemoveNode(bsonUpdates);

        this.updateAddConnection(bsonUpdates);
        this.updateModifyConnection(flowIdFilter, writeModelUpdates);
        this.updateRemoveConnection(bsonUpdates);

        this.updateAddMetadata(bsonUpdates);
        this.updateModifyMetadata(bsonUpdates);
        this.updateRemoveMetadata(bsonUpdates);

        for (var update : bsonUpdates) {
            writeModelUpdates.add(new UpdateOneModel<>(flowIdFilter, update));
        }
        if (!writeModelUpdates.isEmpty()) {
            collection.bulkWrite(writeModelUpdates);
        }
    }

    /**
     * Checks that this observer is in a valid
     * state before updating the flow in the
     * database.
     */
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

    /**
     * Checks if any of the given sets
     * share any values.
     *
     * @param set1 The first set.
     * @param set2 The second set.
     * @param sets The remaining sets.
     * @param <T>  The type of the set values.
     * @return {@code true} if any of the sets
     * share any values, {@code false} otherwise.
     */
    @SafeVarargs
    private static <T> boolean sharedValues(Set<T> set1, Set<T> set2, Set<T>... sets) {
        return !intersection(set1, set2, sets).isEmpty();
    }

    /**
     * Returns the intersection of the given sets.
     *
     * @param set1 The first set.
     * @param set2 The second set.
     * @param sets The remaining sets.
     * @param <T>  The type of the set values.
     * @return The intersection of the given sets.
     */
    @SafeVarargs
    private static <T> Set<T> intersection(Set<T> set1, Set<T> set2, Set<T>... sets) {
        Set<T> result = new HashSet<>(set1);
        result.retainAll(set2);
        for (var set : sets) {
            result.retainAll(set);
        }
        return result;
    }

    // Database updates

    private void updateAddNode(Collection<Bson> updates) {
        if (!this.addedNodes.isEmpty()) {
            var addedNodes = Updates.pushEach("graph.nodes", new ArrayList<>(this.addedNodes.values()));
            updates.add(addedNodes);
        }
    }

    private void updateModifyNode(Bson flowIdFilter, Collection<WriteModel<Flow>> updates) {
        for (var modified : this.modifiedNodes.values()) {
            var filter = Filters.and(flowIdFilter, Filters.eq("graph.nodes.id", modified.id()));
            var update = Updates.set("graph.nodes.$", modified);
            updates.add(new UpdateOneModel<>(filter, update));
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

    private void updateModifyConnection(Bson flowIdFilter, Collection<WriteModel<Flow>> updates) {
        for (var modified : this.modifiedConnections.values()) {
            var filter = Filters.and(
                flowIdFilter,
                Filters.eq("graph.connections.output.node_id", modified.from().nodeId()),
                Filters.eq("graph.connections.output.name", modified.from().name()),
                Filters.eq("graph.connections.input.node_id", modified.to().nodeId()),
                Filters.eq("graph.connections.input.name", modified.to().name())
            );
            var update = Updates.set("graph.connections.$", modified);
            updates.add(new UpdateOneModel<>(filter, update));
        }
    }

    private void updateRemoveConnection(Collection<Bson> updates) {
        var filters = this.removedConnections.values()
            .stream()
            .map(removed -> Filters.and(
                Filters.eq("output.node_id", removed.from().nodeId()),
                Filters.eq("output.name", removed.from().name()),
                Filters.eq("input.node_id", removed.to().nodeId()),
                Filters.eq("input.name", removed.to().name())
            ))
            .toList();
        if (!filters.isEmpty()) {
            var filter = Filters.or(filters);
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

    private void updateRemoveMetadata(List<Bson> updates) {
        for (var nodeId : this.removedMetadata.keySet()) {
            var update = Updates.unset("graph.metadata." + nodeId);
            updates.add(update);
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

    private record ConnectionId(UUID fromId, String fromName, UUID toId, String toName) {
        ConnectionId(NodeConnection<?> connection) {
            this(connection.from().nodeId(), connection.from().name(), connection.to().nodeId(), connection.to().name());
        }
    }
}
