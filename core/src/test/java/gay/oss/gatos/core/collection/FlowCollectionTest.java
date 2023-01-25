package gay.oss.gatos.core.collection;

import java.util.UUID;

import com.mongodb.MongoWriteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gay.oss.gatos.core.models.Flow;

public class FlowCollectionTest {

    private static final FlowCollection OBJECTS = new FlowCollection("testFlows");

    @BeforeEach
    void setUp() {
        this.reset();
    }

    @AfterEach
    void tearDown() {
        this.reset();
    }

    private void reset() {
        OBJECTS.getCollection().drop();
    }

    @Test
    public void canInsertFlow() {
        Flow flow = this.createFlow();
        String flowName = flow.getName();
        UUID authorId = flow.getAuthorId();
        long countBefore = this.getFlowCount();
        OBJECTS.insert(flow);
        this.assertFlowCount(countBefore + 1);
        Flow retrievedFlow = OBJECTS.get(flow.getId());
        Assertions.assertEquals(flowName, retrievedFlow.getName());
        Assertions.assertEquals(authorId, retrievedFlow.getAuthorId());
    }

    @Test
    public void canInsertFlowWithId() {
        Flow flow = this.createFlow();
        String flowName = flow.getName();
        UUID authorId = flow.getAuthorId();
        UUID id = UUID.randomUUID();
        flow.setId(id);
        long countBefore = this.getFlowCount();
        OBJECTS.insert(flow);
        this.assertFlowCount(countBefore + 1);
        Flow retrievedFlow = OBJECTS.get(flow.getId());
        Assertions.assertEquals(flowName, retrievedFlow.getName());
        Assertions.assertEquals(authorId, retrievedFlow.getAuthorId());
        Assertions.assertEquals(id, flow.getId());
    }

    @Test
    public void cannotGetNonExistentFlow() {
        Assertions.assertNull(OBJECTS.get(UUID.randomUUID()));
    }

    @Test
    public void cannotHaveDuplicateId() {
        String flowName = "Test Flow";
        UUID authorId = UUID.randomUUID();
        Flow flow = new Flow(flowName, authorId);
        long countBefore = this.getFlowCount();
        OBJECTS.insert(flow);
        this.assertFlowCount(countBefore + 1);
        Assertions.assertThrows(MongoWriteException.class, () -> OBJECTS.insert(flow));
    }

    @Test
    public void canHaveDuplicateName() {
        String flowName = "Test Flow";
        Flow flow = new Flow(flowName, UUID.randomUUID());
        long countBefore = this.getFlowCount();
        OBJECTS.insert(flow);
        Flow flow2 = new Flow(flowName, UUID.randomUUID());
        Assertions.assertDoesNotThrow(() -> OBJECTS.insert(flow2));
        this.assertFlowCount(countBefore + 2);
    }

    @Test
    public void canHaveDuplicateAuthorId() {
        UUID authorId = UUID.randomUUID();
        Flow flow = new Flow("Test Flow", authorId);
        long countBefore = this.getFlowCount();
        OBJECTS.insert(flow);
        Flow flow2 = new Flow("Test Flow 2", authorId);
        Assertions.assertDoesNotThrow(() -> OBJECTS.insert(flow2));
        this.assertFlowCount(countBefore + 2);
    }

    @Test
    public void cannotUpdateFlowId() {
        Flow flow = new Flow();
        UUID id = UUID.randomUUID();
        flow.setId(id);
        OBJECTS.insert(flow);
        Flow flowUpdate = new Flow();
        UUID newId = UUID.randomUUID();
        flowUpdate.setId(newId);
        OBJECTS.update(flow.getId(), flowUpdate);
        Assertions.assertNotNull(OBJECTS.get(id));
        Assertions.assertNull(OBJECTS.get(newId));
    }

    @Test
    public void canUpdateFlowName() {
        Flow flow = this.createFlow();
        OBJECTS.insert(flow);
        Flow flowUpdate = new Flow();
        String newName = "New Name";
        flowUpdate.setName(newName);
        Flow updated = OBJECTS.update(flow.getId(), flowUpdate);
        Assertions.assertEquals(newName, updated.getName());
    }

    @Test
    public void canUpdateFlowAuthorId() {
        Flow flow = this.createFlow();
        OBJECTS.insert(flow);
        Flow flowUpdate = new Flow();
        UUID newAuthorId = UUID.randomUUID();
        flowUpdate.setAuthorId(newAuthorId);
        Flow updated = OBJECTS.update(flow.getId(), flowUpdate);
        Assertions.assertEquals(newAuthorId, updated.getAuthorId());
    }

    @Test
    public void canUpdateNonExistentFlow() {
        Flow flowUpdate = new Flow();
        String newName = "New Name";
        flowUpdate.setName(newName);
        Assertions.assertNull(OBJECTS.update(UUID.randomUUID(), flowUpdate));
    }

    @Test
    public void canDeleteFlow() {
        Flow flow = this.createFlow();
        long countBefore = this.getFlowCount();
        OBJECTS.insert(flow);
        this.assertFlowCount(countBefore + 1);
        OBJECTS.delete(flow.getId());
        this.assertFlowCount(countBefore);
    }

    @Test
    public void canDeleteNonExistentFlow() {
        long countBefore = this.getFlowCount();
        OBJECTS.delete(UUID.randomUUID());
        this.assertFlowCount(countBefore);
    }

    private long getFlowCount() {
        return OBJECTS.getCollection().countDocuments();
    }

    private void assertFlowCount(long count) {
        Assertions.assertEquals(count, this.getFlowCount());
    }

    private Flow createFlow() {
        return new Flow("Test Flow", UUID.randomUUID());
    }
}
