package club.mondaylunch.gatos.core.graph.test;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.GraphObserver;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.NodeMetadata;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.testshared.graph.type.test.TestNodeTypes;

public class GraphObserverTest {

    private final GraphObserver observer = new GraphObserver();

    @BeforeEach
    public void setUp() {
        this.observer.reset();
    }

    @AfterEach
    public void tearDown() {
        this.validate();
    }

    @Test
    public void canAddNode() {
        var node = Node.create(TestNodeTypes.NO_INPUTS);
        canAdd(
            node.id(),
            node,
            (id, node1) -> this.observer.nodeAdded(node1),
            this.addedNodes(),
            this.modifiedNodes(),
            this.removedNodes()
        );
    }

    @Test
    public void canAddModifiedNode() {
        var node = Node.create(TestNodeTypes.NO_INPUTS);
        var modifiedNode = node.modifySetting("setting", DataType.NUMBER.create(1.0));
        canAddModified(
            node.id(),
            node,
            modifiedNode,
            (id, node1) -> this.observer.nodeAdded(node1),
            (id, node1) -> this.observer.nodeModified(node1),
            this.addedNodes(),
            this.modifiedNodes(),
            this.removedNodes()
        );
    }

    @Test
    public void canAddRemovedSameNode() {
        var node = Node.create(TestNodeTypes.NO_INPUTS);
        canAddRemovedSame(
            node.id(),
            node,
            (id, node1) -> this.observer.nodeAdded(node1),
            (id, node1) -> this.observer.nodeRemoved(node1),
            this.addedNodes(),
            this.modifiedNodes(),
            this.removedNodes()
        );
    }

    @Test
    public void canAddRemovedDifferentNode() {
        var node = Node.create(TestNodeTypes.NO_INPUTS);
        var removedNode = node.modifySetting("setting", DataType.NUMBER.create(1.0));
        canAddRemovedDifferent(
            node.id(),
            node,
            removedNode,
            (id, node1) -> this.observer.nodeAdded(node1),
            (id, node1) -> this.observer.nodeRemoved(node1),
            this.addedNodes(),
            this.modifiedNodes(),
            this.removedNodes()
        );
    }

    @Test
    public void canModifyNode() {
        var node = Node.create(TestNodeTypes.NO_INPUTS);
        canModify(
            node.id(),
            node,
            (id, node1) -> this.observer.nodeModified(node1),
            this.addedNodes(),
            this.modifiedNodes(),
            this.removedNodes()
        );
    }

    @Test
    public void canModifyAddedNode() {
        var node = Node.create(TestNodeTypes.NO_INPUTS);
        var modifiedNode = node.modifySetting("setting", DataType.NUMBER.create(1.0));
        canModifyAdded(
            node.id(),
            node,
            modifiedNode,
            (id, node1) -> this.observer.nodeAdded(node1),
            (id, node1) -> this.observer.nodeModified(node1),
            this.addedNodes(),
            this.modifiedNodes(),
            this.removedNodes()
        );
    }

    @Test
    public void cannotModifyRemovedNode() {
        var node = Node.create(TestNodeTypes.NO_INPUTS);
        cannotModifyRemoved(
            node.id(),
            node,
            (id, node1) -> this.observer.nodeModified(node1),
            (id, node1) -> this.observer.nodeRemoved(node1),
            this.addedNodes(),
            this.modifiedNodes(),
            this.removedNodes()
        );
    }

    @Test
    public void canRemoveNode() {
        var node = Node.create(TestNodeTypes.NO_INPUTS);
        canRemove(
            node.id(),
            node,
            (id, node1) -> this.observer.nodeRemoved(node1),
            this.addedNodes(),
            this.modifiedNodes(),
            this.removedNodes()
        );
    }

    @Test
    public void canRemoveAddedNode() {
        var node = Node.create(TestNodeTypes.NO_INPUTS);
        canRemoveAdded(
            node.id(),
            node,
            (id, node1) -> this.observer.nodeAdded(node1),
            (id, node1) -> this.observer.nodeRemoved(node1),
            this.addedNodes(),
            this.modifiedNodes(),
            this.removedNodes()
        );
    }

    @Test
    public void canRemoveModifiedNode() {
        var node = Node.create(TestNodeTypes.NO_INPUTS);
        canRemoveModified(
            node.id(),
            node,
            (id, node1) -> this.observer.nodeModified(node1),
            (id, node1) -> this.observer.nodeRemoved(node1),
            this.addedNodes(),
            this.modifiedNodes(),
            this.removedNodes()
        );
    }

    @Test
    public void canAddConnection() {
        var node1 = Node.create(TestNodeTypes.NO_INPUTS);
        var node2 = Node.create(TestNodeTypes.PROCESS);
        var connection = NodeConnection.create(node1, "start_output", node2, "process_input");
        var connectionId = createConnectionId(connection);
        canAdd(
            connectionId,
            connection,
            (id, connection1) -> this.observer.connectionAdded(connection1),
            this.addedConnections(),
            this.modifiedConnections(),
            this.removedConnections()
        );
    }

    @Test
    public void canAddRemovedConnection() {
        var node1 = Node.create(TestNodeTypes.NO_INPUTS);
        var node2 = Node.create(TestNodeTypes.PROCESS);
        var connection = NodeConnection.create(node1, "start_output", node2, "process_input");
        var connectionId = createConnectionId(connection);
        canAddRemovedSame(
            connectionId,
            connection,
            (id, connection1) -> this.observer.connectionAdded(connection1),
            (id, connection1) -> this.observer.connectionRemoved(connection1),
            this.addedConnections(),
            this.modifiedConnections(),
            this.removedConnections()
        );
    }

    @Test
    public void canRemoveConnection() {
        var node1 = Node.create(TestNodeTypes.NO_INPUTS);
        var node2 = Node.create(TestNodeTypes.PROCESS);
        var connection = NodeConnection.create(node1, "start_output", node2, "process_input");
        var connectionId = createConnectionId(connection);
        canRemove(
            connectionId,
            connection,
            (id, connection1) -> this.observer.connectionRemoved(connection1),
            this.addedConnections(),
            this.modifiedConnections(),
            this.removedConnections()
        );
    }

    @Test
    public void canRemoveAddedConnection() {
        var node1 = Node.create(TestNodeTypes.NO_INPUTS);
        var node2 = Node.create(TestNodeTypes.PROCESS);
        var connection = NodeConnection.create(node1, "start_output", node2, "process_input");
        var connectionId = createConnectionId(connection);
        canRemoveAdded(
            connectionId,
            connection,
            (id, connection1) -> this.observer.connectionAdded(connection1),
            (id, connection1) -> this.observer.connectionRemoved(connection1),
            this.addedConnections(),
            this.modifiedConnections(),
            this.removedConnections()
        );
    }

    @Test
    public void canAddMetadata() {
        var nodeId = UUID.randomUUID();
        var metadata = new NodeMetadata(0, 0);
        canAdd(
            nodeId,
            metadata,
            this.observer::metadataAdded,
            this.addedMetadata(),
            this.modifiedMetadata(),
            this.removedMetadata()
        );
    }

    @Test
    public void canAddModifiedMetadata() {
        var nodeId = UUID.randomUUID();
        var metadata = new NodeMetadata(0, 0);
        var modifiedMetadata = metadata.withX(1);
        canAddModified(
            nodeId,
            metadata,
            modifiedMetadata,
            this.observer::metadataAdded,
            this.observer::metadataModified,
            this.addedMetadata(),
            this.modifiedMetadata(),
            this.removedMetadata()
        );
    }

    @Test
    public void canAddRemovedSameMetadata() {
        var nodeId = UUID.randomUUID();
        var metadata = new NodeMetadata(0, 0);
        canAddRemovedSame(
            nodeId,
            metadata,
            this.observer::metadataAdded,
            this.observer::metadataRemoved,
            this.addedMetadata(),
            this.modifiedMetadata(),
            this.removedMetadata()
        );
    }

    @Test
    public void canAddRemovedDifferentMetadata() {
        var nodeId = UUID.randomUUID();
        var metadata = new NodeMetadata(0, 0);
        var removedMetadata = metadata.withX(1);
        canAddRemovedDifferent(
            nodeId,
            metadata,
            removedMetadata,
            this.observer::metadataAdded,
            this.observer::metadataRemoved,
            this.addedMetadata(),
            this.modifiedMetadata(),
            this.removedMetadata()
        );
    }

    @Test
    public void canModifyMetadata() {
        var nodeId = UUID.randomUUID();
        var metadata = new NodeMetadata(0, 0);
        canModify(
            nodeId,
            metadata,
            this.observer::metadataModified,
            this.addedMetadata(),
            this.modifiedMetadata(),
            this.removedMetadata()
        );
    }

    @Test
    public void canModifyAddedMetadata() {
        var nodeId = UUID.randomUUID();
        var metadata = new NodeMetadata(0, 0);
        var modifiedMetadata = metadata.withX(1);
        canModifyAdded(
            nodeId,
            metadata,
            modifiedMetadata,
            this.observer::metadataAdded,
            this.observer::metadataModified,
            this.addedMetadata(),
            this.modifiedMetadata(),
            this.removedMetadata()
        );
    }

    @Test
    public void cannotModifyRemovedMetadata() {
        var nodeId = UUID.randomUUID();
        var metadata = new NodeMetadata(0, 0);
        cannotModifyRemoved(
            nodeId,
            metadata,
            this.observer::metadataModified,
            this.observer::metadataRemoved,
            this.addedMetadata(),
            this.modifiedMetadata(),
            this.removedMetadata()
        );
    }

    @Test
    public void canRemoveMetadata() {
        var nodeId = UUID.randomUUID();
        var metadata = new NodeMetadata(0, 0);
        canRemove(
            nodeId,
            metadata,
            this.observer::metadataRemoved,
            this.addedMetadata(),
            this.modifiedMetadata(),
            this.removedMetadata()
        );
    }

    @Test
    public void canRemoveAddedMetadata() {
        var nodeId = UUID.randomUUID();
        var metadata = new NodeMetadata(0, 0);
        canRemoveAdded(
            nodeId,
            metadata,
            this.observer::metadataAdded,
            this.observer::metadataRemoved,
            this.addedMetadata(),
            this.modifiedMetadata(),
            this.removedMetadata()
        );
    }

    @Test
    public void canRemoveModifiedMetadata() {
        var nodeId = UUID.randomUUID();
        var metadata = new NodeMetadata(0, 0);
        canRemoveModified(
            nodeId,
            metadata,
            this.observer::metadataModified,
            this.observer::metadataRemoved,
            this.addedMetadata(),
            this.modifiedMetadata(),
            this.removedMetadata()
        );
    }

    private static <K, V> void canAdd(K id, V value, BiConsumer<K, V> observerAction, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        observerAction.accept(id, value);
        Assertions.assertEquals(1, added.size());
        Assertions.assertEquals(value, added.get(id));
        Assertions.assertEquals(0, modified.size());
        Assertions.assertEquals(0, removed.size());
    }

    private static <K, V> void canAddModified(K id, V addedValue, V modifiedValue, BiConsumer<K, V> addAction, BiConsumer<K, V> modifyAction, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        modifyAction.accept(id, modifiedValue);
        addAction.accept(id, addedValue);
        Assertions.assertEquals(0, added.size());
        Assertions.assertEquals(1, modified.size());
        Assertions.assertEquals(addedValue, modified.get(id));
        Assertions.assertEquals(0, removed.size());
    }

    private static <K, V> void canAddRemovedSame(K id, V value, BiConsumer<K, V> addAction, BiConsumer<K, V> removeAction, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        removeAction.accept(id, value);
        addAction.accept(id, value);
        Assertions.assertEquals(0, added.size());
        Assertions.assertEquals(0, modified.size());
        Assertions.assertEquals(0, removed.size());
    }

    private static <K, V> void canAddRemovedDifferent(K id, V addedValue, V removedValue, BiConsumer<K, V> addAction, BiConsumer<K, V> removeAction, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        removeAction.accept(id, removedValue);
        addAction.accept(id, addedValue);
        Assertions.assertEquals(0, added.size());
        Assertions.assertEquals(1, modified.size());
        Assertions.assertEquals(addedValue, modified.get(id));
        Assertions.assertEquals(0, removed.size());
    }

    private static <K, V> void canModify(K id, V value, BiConsumer<K, V> observerAction, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        observerAction.accept(id, value);
        Assertions.assertEquals(0, added.size());
        Assertions.assertEquals(1, modified.size());
        Assertions.assertEquals(value, modified.get(id));
        Assertions.assertEquals(0, removed.size());
    }

    private static <K, V> void canModifyAdded(K id, V addedValue, V modifiedValue, BiConsumer<K, V> addAction, BiConsumer<K, V> modifyAction, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        addAction.accept(id, addedValue);
        modifyAction.accept(id, modifiedValue);
        Assertions.assertEquals(1, added.size());
        Assertions.assertEquals(modifiedValue, added.get(id));
        Assertions.assertEquals(0, modified.size());
        Assertions.assertEquals(0, removed.size());
    }

    private static <K, V> void cannotModifyRemoved(K id, V value, BiConsumer<K, V> modifyAction, BiConsumer<K, V> removeAction, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        removeAction.accept(id, value);
        Assertions.assertThrows(IllegalStateException.class, () -> modifyAction.accept(id, value));
        Assertions.assertEquals(0, added.size());
        Assertions.assertEquals(0, modified.size());
        Assertions.assertEquals(1, removed.size());
        Assertions.assertEquals(value, removed.get(id));
    }

    private static <K, V> void canRemove(K id, V value, BiConsumer<K, V> observerAction, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        observerAction.accept(id, value);
        Assertions.assertEquals(0, added.size());
        Assertions.assertEquals(0, modified.size());
        Assertions.assertEquals(1, removed.size());
        Assertions.assertEquals(value, removed.get(id));
    }

    private static <K, V> void canRemoveAdded(K id, V value, BiConsumer<K, V> addAction, BiConsumer<K, V> removeAction, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        addAction.accept(id, value);
        removeAction.accept(id, value);
        Assertions.assertEquals(0, added.size());
        Assertions.assertEquals(0, modified.size());
        Assertions.assertEquals(0, removed.size());
    }

    private static <K, V> void canRemoveModified(K id, V value, BiConsumer<K, V> modifyAction, BiConsumer<K, V> removeAction, Map<K, V> added, Map<K, V> modified, Map<K, V> removed) {
        modifyAction.accept(id, value);
        removeAction.accept(id, value);
        Assertions.assertEquals(0, added.size());
        Assertions.assertEquals(0, modified.size());
        Assertions.assertEquals(1, removed.size());
        Assertions.assertEquals(value, removed.get(id));
    }

    private Map<UUID, Node> addedNodes() {
        return getFieldValue("addedNodes");
    }

    private Map<UUID, Node> modifiedNodes() {
        return getFieldValue("modifiedNodes");
    }

    private Map<UUID, Node> removedNodes() {
        return getFieldValue("removedNodes");
    }

    private Map<Object, NodeConnection<?>> addedConnections() {
        return getFieldValue("addedConnections");
    }

    private Map<Object, NodeConnection<?>> modifiedConnections() {
        return getFieldValue("modifiedConnections");
    }

    private Map<Object, NodeConnection<?>> removedConnections() {
        return getFieldValue("removedConnections");
    }

    private Map<UUID, NodeMetadata> addedMetadata() {
        return getFieldValue("addedMetadata");
    }

    private Map<UUID, NodeMetadata> modifiedMetadata() {
        return getFieldValue("modifiedMetadata");
    }

    private Map<UUID, NodeMetadata> removedMetadata() {
        return getFieldValue("removedMetadata");
    }

    private void validate() {
        try {
            var method = GraphObserver.class.getDeclaredMethod("validate");
            method.setAccessible(true);
            method.invoke(this.observer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object createConnectionId(NodeConnection<?> connection) {
        var connectionIdClass = GraphObserver.class.getDeclaredClasses()[0];
        try {
            var constructor = connectionIdClass.getDeclaredConstructor(NodeConnection.class);
            constructor.setAccessible(true);
            return constructor.newInstance(connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T getFieldValue(String fieldName) {
        try {
            var field = GraphObserver.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            var value = (T) field.get(this.observer);
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
