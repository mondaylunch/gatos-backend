package club.mondaylunch.gatos.core.codec.test;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.collection.BaseCollection;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.BaseModel;
import club.mondaylunch.gatos.testshared.graph.type.test.TestNodeTypes;

public class NodeTypeCodecTest {

    private static final BaseCollection<NodeTypeContainer> CONTAINER_COLLECTION = new BaseCollection<>("nodeTypeContainers", NodeTypeContainer.class);
    private static final BaseCollection<NodeTypeContainerContainer> CONTAINER_CONTAINER_COLLECTION = new BaseCollection<>("nodeTypeContainerContainers", NodeTypeContainerContainer.class);

    @BeforeEach
    void setUp() {
        this.reset();
        assertContainerCount(0);
        assertContainerContainerCount(0);
    }

    @AfterEach
    void tearDown() {
        this.reset();
    }

    private void reset() {
        CONTAINER_COLLECTION.clear();
        CONTAINER_CONTAINER_COLLECTION.clear();
    }

    @Test
    public void canInsertNodeTypeContainer() {
        assertInsertNodeTypeContainers(TestNodeTypes.NO_INPUTS);
    }

    @Test
    public void canInsertMultipleNodeTypeContainers() {
        assertInsertNodeTypeContainers(
            TestNodeTypes.NO_INPUTS,
            TestNodeTypes.PROCESS,
            TestNodeTypes.END
        );
    }

    @Test
    public void canInsertNodeTypeContainersWithDuplicates() {
        assertInsertNodeTypeContainers(
            TestNodeTypes.NO_INPUTS,
            TestNodeTypes.PROCESS,
            TestNodeTypes.END,
            TestNodeTypes.NO_INPUTS,
            TestNodeTypes.PROCESS,
            TestNodeTypes.END
        );
    }

    @Test
    public void canInsertNestedContainer() {
        UUID id = UUID.randomUUID();
        NodeType nodeType = TestNodeTypes.NO_INPUTS;
        NodeTypeContainer container = new NodeTypeContainer();
        container.nodeType = nodeType;
        NodeTypeContainerContainer containerContainer = new NodeTypeContainerContainer(id, container);
        CONTAINER_CONTAINER_COLLECTION.insert(containerContainer);
        assertContainerContainerCount(1);
        NodeTypeContainerContainer retrievedContainerContainer = CONTAINER_CONTAINER_COLLECTION.get(id);
        Assertions.assertNotNull(retrievedContainerContainer);
        NodeTypeContainer retrievedContainer = retrievedContainerContainer.nodeTypeContainer;
        Assertions.assertNotNull(retrievedContainer);
        Assertions.assertSame(nodeType, retrievedContainer.nodeType);
    }

    private static UUID insertNodeTypeContainer(NodeType nodeType) {
        UUID id = UUID.randomUUID();
        NodeTypeContainer container = new NodeTypeContainer(id, nodeType);
        long countBefore = CONTAINER_COLLECTION.size();
        CONTAINER_COLLECTION.insert(container);
        assertContainerCount(countBefore + 1);
        return id;
    }

    private static void assertInsertNodeTypeContainers(NodeType... nodeTypes) {
        int inserted = 0;
        for (NodeType nodeType : nodeTypes) {
            UUID id = insertNodeTypeContainer(nodeType);
            inserted++;
            assertContainerCount(inserted);
            NodeTypeContainer retrievedContainer = CONTAINER_COLLECTION.get(id);
            Assertions.assertNotNull(retrievedContainer);
            Assertions.assertSame(nodeType, retrievedContainer.nodeType);
        }
    }

    private static void assertContainerCount(long expected) {
        Assertions.assertEquals(expected, CONTAINER_COLLECTION.size());
    }

    private static void assertContainerContainerCount(long expected) {
        Assertions.assertEquals(expected, CONTAINER_CONTAINER_COLLECTION.size());
    }

    public static class NodeTypeContainer extends BaseModel {

        public NodeType nodeType;

        public NodeTypeContainer(UUID id, NodeType nodeType) {
            super(id);
            this.nodeType = nodeType;
        }

        public NodeTypeContainer() {
        }
    }

    public static class NodeTypeContainerContainer extends BaseModel {

        public NodeTypeContainer nodeTypeContainer;

        public NodeTypeContainerContainer(UUID id, NodeTypeContainer nodeTypeContainer) {
            super(id);
            this.nodeTypeContainer = nodeTypeContainer;
        }

        @SuppressWarnings("unused")
        public NodeTypeContainerContainer() {
        }
    }
}
