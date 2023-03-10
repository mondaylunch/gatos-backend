package club.mondaylunch.gatos.core.collection.test;

import java.util.UUID;

import com.mongodb.MongoWriteException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.models.Flow;

public class FlowCollectionTest {

    private long initialFlowCount;

    @BeforeEach
    void setUp() {
        this.reset();
        this.initialFlowCount = getFlowCount();
    }

    @AfterEach
    void tearDown() {
        this.reset();
    }

    private void reset() {
        Flow.objects.clear();
    }

    @Test
    public void canInsertFlow() {
        Flow flow = createFlow();
        String flowName = flow.getName();
        UUID authorId = flow.getAuthorId();
        Flow.objects.insert(flow);
        this.assertFlowCountChange(1);
        Flow retrievedFlow = Flow.objects.get(flow.getId());
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
        Flow.objects.insert(flow);
        this.assertFlowCountChange(1);
        Flow retrievedFlow = Flow.objects.get(flow.getId());
        Assertions.assertEquals(flowName, retrievedFlow.getName());
        Assertions.assertEquals(authorId, retrievedFlow.getAuthorId());
        Assertions.assertEquals(id, flow.getId());
    }

    @Test
    public void cannotGetNonExistentFlow() {
        Assertions.assertNull(Flow.objects.get(UUID.randomUUID()));
    }

    @Test
    public void cannotHaveDuplicateId() {
        String flowName = "Test Flow";
        UUID authorId = UUID.randomUUID();
        Flow flow = new Flow(UUID.randomUUID(), flowName, authorId);
        Flow.objects.insert(flow);
        this.assertFlowCountChange(1);
        Assertions.assertThrows(MongoWriteException.class, () -> Flow.objects.insert(flow));
    }

    @Test
    public void canHaveDuplicateName() {
        String flowName = "Test Flow";
        Flow flow = new Flow(UUID.randomUUID(), flowName, UUID.randomUUID());
        Flow.objects.insert(flow);
        Flow flow2 = new Flow(UUID.randomUUID(), flowName, UUID.randomUUID());
        Assertions.assertDoesNotThrow(() -> Flow.objects.insert(flow2));
        this.assertFlowCountChange(2);
    }

    @Test
    public void canHaveDuplicateAuthorId() {
        UUID authorId = UUID.randomUUID();
        Flow flow = new Flow(UUID.randomUUID(), "Test Flow", authorId);
        Flow.objects.insert(flow);
        Flow flow2 = new Flow(UUID.randomUUID(), "Test Flow 2", authorId);
        Assertions.assertDoesNotThrow(() -> Flow.objects.insert(flow2));
        this.assertFlowCountChange(2);
    }

    @Test
    public void cannotUpdateFlowId() {
        Flow flow = new Flow();
        UUID id = UUID.randomUUID();
        flow.setId(id);
        Flow.objects.insert(flow);
        Flow flowUpdate = new Flow();
        UUID newId = UUID.randomUUID();
        flowUpdate.setId(newId);
        Flow.objects.update(flow.getId(), flowUpdate);
        Assertions.assertNotNull(Flow.objects.get(id));
        Assertions.assertNull(Flow.objects.get(newId));
    }

    @Test
    public void canUpdateFlowName() {
        Flow flow = createFlow();
        Flow.objects.insert(flow);
        Flow flowUpdate = new Flow();
        String newName = "New Name";
        flowUpdate.setName(newName);
        Flow.objects.update(flow.getId(), flowUpdate);
        Flow updated = Flow.objects.get(flow.getId());
        Assertions.assertEquals(newName, updated.getName());
    }

    @Test
    public void canUpdateFlowAuthorId() {
        Flow flow = createFlow();
        Flow.objects.insert(flow);
        Flow flowUpdate = new Flow();
        UUID newAuthorId = UUID.randomUUID();
        flowUpdate.setAuthorId(newAuthorId);
        Flow.objects.update(flow.getId(), flowUpdate);
        Flow updated = Flow.objects.get(flow.getId());
        Assertions.assertEquals(newAuthorId, updated.getAuthorId());
    }

    @Test
    public void canUpdateNonExistentFlow() {
        Flow flowUpdate = new Flow();
        String newName = "New Name";
        flowUpdate.setName(newName);
        UUID id = UUID.randomUUID();
        Flow.objects.update(id, flowUpdate);
        Flow updated = Flow.objects.get(id);
        Assertions.assertNull(updated);
    }

    @Test
    public void canDeleteFlow() {
        Flow flow = createFlow();
        Flow.objects.insert(flow);
        this.assertFlowCountChange(1);
        Flow.objects.delete(flow.getId());
        this.assertFlowCountChange(0);
        Assertions.assertNull(Flow.objects.get(flow.getId()));
    }

    @Test
    public void canDeleteNonExistentFlow() {
        Flow.objects.delete(UUID.randomUUID());
        this.assertFlowCountChange(0);
    }

    @Test
    public void containsFlow() {
        var flow = createFlow();
        Flow.objects.insert(flow);
        this.assertFlowCountChange(1);
        var flowId = flow.getId();
        Assertions.assertTrue(Flow.objects.contains(flowId));
    }

    @Test
    public void doesNotContainFlow() {
        var flowId = UUID.randomUUID();
        Assertions.assertFalse(Flow.objects.contains(flowId));
    }

    private void assertFlowCountChange(long change) {
        Assertions.assertEquals(this.initialFlowCount + change, getFlowCount());
    }

    private static long getFlowCount() {
        return Flow.objects.size();
    }

    private static Flow createFlow() {
        return new Flow(UUID.randomUUID(), "Test Flow", UUID.randomUUID());
    }
}
