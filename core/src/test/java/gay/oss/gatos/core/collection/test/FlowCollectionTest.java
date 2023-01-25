package gay.oss.gatos.core.collection.test;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.mongodb.MongoWriteException;

import gay.oss.gatos.core.collection.FlowCollection;
import gay.oss.gatos.core.models.Flow;

public class FlowCollectionTest {

    @BeforeEach
    void setUp() {
        this.reset();
    }

    @AfterEach
    void tearDown() {
        this.reset();
    }

    private void reset() {
        Flow.objects.getCollection().drop();
    }

    @Test
    public void canInsertFlow() {
        Flow flow = this.createFlow();
        String flowName = flow.getName();
        UUID authorId = flow.getAuthorId();
        long countBefore = this.getFlowCount();
        Flow.objects.insert(flow);
        this.assertFlowCount(countBefore + 1);
        Flow retrievedFlow = Flow.objects.get(flow.getId());
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
        Flow.objects.insert(flow);
        this.assertFlowCount(countBefore + 1);
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
        Flow flow = new Flow(flowName, authorId);
        long countBefore = this.getFlowCount();
        Flow.objects.insert(flow);
        this.assertFlowCount(countBefore + 1);
        Assertions.assertThrows(MongoWriteException.class, () -> Flow.objects.insert(flow));
    }

    @Test
    public void canHaveDuplicateName() {
        String flowName = "Test Flow";
        Flow flow = new Flow(flowName, UUID.randomUUID());
        long countBefore = this.getFlowCount();
        Flow.objects.insert(flow);
        Flow flow2 = new Flow(flowName, UUID.randomUUID());
        Assertions.assertDoesNotThrow(() -> Flow.objects.insert(flow2));
        this.assertFlowCount(countBefore + 2);
    }

    @Test
    public void canHaveDuplicateAuthorId() {
        UUID authorId = UUID.randomUUID();
        Flow flow = new Flow("Test Flow", authorId);
        long countBefore = this.getFlowCount();
        Flow.objects.insert(flow);
        Flow flow2 = new Flow("Test Flow 2", authorId);
        Assertions.assertDoesNotThrow(() -> Flow.objects.insert(flow2));
        this.assertFlowCount(countBefore + 2);
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
        Flow flow = this.createFlow();
        Flow.objects.insert(flow);
        Flow flowUpdate = new Flow();
        String newName = "New Name";
        flowUpdate.setName(newName);
        Flow updated = Flow.objects.update(flow.getId(), flowUpdate);
        Assertions.assertEquals(newName, updated.getName());
    }

    @Test
    public void canUpdateFlowAuthorId() {
        Flow flow = this.createFlow();
        Flow.objects.insert(flow);
        Flow flowUpdate = new Flow();
        UUID newAuthorId = UUID.randomUUID();
        flowUpdate.setAuthorId(newAuthorId);
        Flow updated = Flow.objects.update(flow.getId(), flowUpdate);
        Assertions.assertEquals(newAuthorId, updated.getAuthorId());
    }

    @Test
    public void canUpdateNonExistentFlow() {
        Flow flowUpdate = new Flow();
        String newName = "New Name";
        flowUpdate.setName(newName);
        Assertions.assertNull(Flow.objects.update(UUID.randomUUID(), flowUpdate));
    }

    @Test
    public void canDeleteFlow() {
        Flow flow = this.createFlow();
        long countBefore = this.getFlowCount();
        Flow.objects.insert(flow);
        this.assertFlowCount(countBefore + 1);
        Flow.objects.delete(flow.getId());
        this.assertFlowCount(countBefore);
    }

    @Test
    public void canDeleteNonExistentFlow() {
        long countBefore = this.getFlowCount();
        Flow.objects.delete(UUID.randomUUID());
        this.assertFlowCount(countBefore);
    }

    private long getFlowCount() {
        return Flow.objects.getCollection().countDocuments();
    }

    private void assertFlowCount(long count) {
        Assertions.assertEquals(count, this.getFlowCount());
    }

    private Flow createFlow() {
        return new Flow("Test Flow", UUID.randomUUID());
    }
}
