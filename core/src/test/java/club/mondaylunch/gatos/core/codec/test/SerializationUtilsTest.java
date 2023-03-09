package club.mondaylunch.gatos.core.codec.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import club.mondaylunch.gatos.core.codec.SerializationUtils;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.Flow;
import club.mondaylunch.gatos.testshared.graph.type.test.TestNodeTypes;

public class SerializationUtilsTest {

    @Test
    public void canSerializeFlowToJson() throws Exception {
        var flow = createFlow();
        String expectedJson = readString("test-flow.json");
        String actualJson = flow.toJson();
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void canDeserializeFlowFromJson() throws Exception {
        String json = readString("test-flow.json");
        var expectedFlow = createFlow();
        var actualFlow = SerializationUtils.fromJson(json, Flow.class);
        Assertions.assertEquals(expectedFlow, actualFlow);
    }

    private static Flow createFlow() {
        UUID id = UUID.fromString("269e8442-05b9-4981-a9cb-c780a42cba30");
        UUID authorId = UUID.fromString("166d8840-322d-4d9e-9967-cb5be6d49af2");
        Flow flow = new Flow(id, "Test Flow", authorId);
        flow.setDescription("This is a test flow");

        UUID startId = UUID.fromString("9f60cd6b-b4c2-43a1-83b7-711aa90ce8fd");
        Node start = createNode(TestNodeTypes.START, startId);

        UUID processId = UUID.fromString("6f8de627-706d-4817-8921-73bff23006a8");
        Node process = createNode(TestNodeTypes.PROCESS, processId);

        UUID endId = UUID.fromString("b15f484f-4345-4f30-9162-5210b4ff1433");
        Node end = createNode(TestNodeTypes.END, endId);

        Graph graph = new Graph(List.of(start, process, end), Map.of(), List.of());
        flow.setGraph(graph);

        var startToProcess = NodeConnection.create(start, "start_output", process, "process_input");
        var processToEnd = NodeConnection.create(process, "process_output", end, "end_input");
        graph.addConnection(startToProcess);
        graph.addConnection(processToEnd);

        graph.modifyMetadata(start.id(), nodeMetadata -> nodeMetadata.withX(1));

        return flow;
    }

    private static Node createNode(NodeType type, UUID id) {
        try {
            Constructor<Node> nodeConstructor = Node.class.getDeclaredConstructor(
                UUID.class,
                NodeType.class,
                Map.class,
                Set.class,
                Set.class,
                Map.class
            );
            nodeConstructor.setAccessible(true);
            var defaultSettings = type.settings();
            return nodeConstructor.newInstance(
                id,
                type,
                defaultSettings,
                NodeType.inputsOrEmpty(type, id, defaultSettings, Map.of()),
                NodeType.outputsOrEmpty(type, id, defaultSettings, Map.of()),
                Map.of()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static String readString(String resourcePath) throws IOException {
        try (
            InputStream inputStream = getResource(resourcePath);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
    }

    private static InputStream getResource(String path) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found:\n" + path);
        } else {
            return inputStream;
        }
    }
}
