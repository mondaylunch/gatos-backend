package gay.oss.gatos.core.collection;

import com.mongodb.MongoWriteException;
import gay.oss.gatos.core.models.Flow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class FlowCollectionTest {

    private static final FlowCollection objects = new FlowCollection("testFlows");

    @BeforeEach
    void setUp() {
        reset();
    }

    @AfterEach
    void tearDown() {
        reset();
    }

    private void reset() {
        objects.getCollection().drop();
    }

    @Test
    public void canInsertFlow() {
        Flow flow = createFlow();
        String flowName = flow.getName();
        UUID authorId = flow.getAuthorId();
        long countBefore = getFlowCount();
        objects.insert(flow);
        assertFlowCount(countBefore + 1);
        Flow retrievedFlow = objects.get(flow.getId());
        Assertions.assertEquals(flowName, retrievedFlow.getName());
        Assertions.assertEquals(authorId, retrievedFlow.getAuthorId());
    }

    @Test
    public void canInsertFlowWithId() {
        Flow flow = createFlow();
        String flowName = flow.getName();
        UUID authorId = flow.getAuthorId();
        UUID id = UUID.randomUUID();
        flow.setId(id);
        long countBefore = getFlowCount();
        objects.insert(flow);
        assertFlowCount(countBefore + 1);
        Flow retrievedFlow = objects.get(flow.getId());
        Assertions.assertEquals(flowName, retrievedFlow.getName());
        Assertions.assertEquals(authorId, retrievedFlow.getAuthorId());
        Assertions.assertEquals(id, flow.getId());
    }

    @Test
    public void cannotGetNonExistentFlow() {
        Assertions.assertNull(objects.get(UUID.randomUUID()));
    }

    @Test
    public void cannotHaveDuplicateId() {
        String flowName = "Test Flow";
        UUID authorId = UUID.randomUUID();
        Flow flow = new Flow(flowName, authorId);
        long countBefore = getFlowCount();
        objects.insert(flow);
        assertFlowCount(countBefore + 1);
        Assertions.assertThrows(MongoWriteException.class, () -> objects.insert(flow));
    }

    @Test
    public void canHaveDuplicateName() {
        String flowName = "Test Flow";
        Flow flow = new Flow(flowName, UUID.randomUUID());
        long countBefore = getFlowCount();
        objects.insert(flow);
        Flow flow2 = new Flow(flowName, UUID.randomUUID());
        Assertions.assertDoesNotThrow(() -> objects.insert(flow2));
        assertFlowCount(countBefore + 2);
    }

    @Test
    public void canHaveDuplicateAuthorId() {
        UUID authorId = UUID.randomUUID();
        Flow flow = new Flow("Test Flow", authorId);
        long countBefore = getFlowCount();
        objects.insert(flow);
        Flow flow2 = new Flow("Test Flow 2", authorId);
        Assertions.assertDoesNotThrow(() -> objects.insert(flow2));
        assertFlowCount(countBefore + 2);
    }

    @Test
    public void cannotUpdateFlowId() {
        Flow flow = new Flow();
        UUID id = UUID.randomUUID();
        flow.setId(id);
        objects.insert(flow);
        Flow flowUpdate = new Flow();
        UUID newId = UUID.randomUUID();
        flowUpdate.setId(newId);
        objects.update(flow.getId(), flowUpdate);
        Assertions.assertNotNull(objects.get(id));
        Assertions.assertNull(objects.get(newId));
    }

    @Test
    public void canUpdateFlowName() {
        Flow flow = createFlow();
        objects.insert(flow);
        Flow flowUpdate = new Flow();
        String newName = "New Name";
        flowUpdate.setName(newName);
        Flow updated = objects.update(flow.getId(), flowUpdate);
        Assertions.assertEquals(newName, updated.getName());
    }

    @Test
    public void canUpdateFlowAuthorId() {
        Flow flow = createFlow();
        objects.insert(flow);
        Flow flowUpdate = new Flow();
        UUID newAuthorId = UUID.randomUUID();
        flowUpdate.setAuthorId(newAuthorId);
        Flow updated = objects.update(flow.getId(), flowUpdate);
        Assertions.assertEquals(newAuthorId, updated.getAuthorId());
    }

    @Test
    public void canUpdateNonExistentFlow() {
        Flow flowUpdate = new Flow();
        String newName = "New Name";
        flowUpdate.setName(newName);
        Assertions.assertNull(objects.update(UUID.randomUUID(), flowUpdate));
    }

    @Test
    public void canDeleteFlow() {
        Flow flow = createFlow();
        long countBefore = getFlowCount();
        objects.insert(flow);
        assertFlowCount(countBefore + 1);
        objects.delete(flow.getId());
        assertFlowCount(countBefore);
    }

    @Test
    public void canDeleteNonExistentFlow() {
        long countBefore = getFlowCount();
        objects.delete(UUID.randomUUID());
        assertFlowCount(countBefore);
    }

    private long getFlowCount() {
        return objects.getCollection().countDocuments();
    }

    private void assertFlowCount(long count) {
        Assertions.assertEquals(count, getFlowCount());
    }

    private Flow createFlow() {
        return new Flow("Test Flow", UUID.randomUUID());
    }
}
