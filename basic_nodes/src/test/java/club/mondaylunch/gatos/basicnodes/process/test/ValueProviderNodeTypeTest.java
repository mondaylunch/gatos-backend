package club.mondaylunch.gatos.basicnodes.process.test;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.basicnodes.BasicNodes;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Node;

public class ValueProviderNodeTypeTest {
    @Test
    public void isSettingCorrect() {
        BasicNodes.VALUE_PROVIDERS.forEach((dataType, nodeType) -> {
            var node = Node.create(nodeType);
            Assertions.assertEquals(dataType, node.settings().get("value").type());
        });
    }

    @Test
    public void isOutputConnectorCorrect() {
        BasicNodes.VALUE_PROVIDERS.forEach((dataType, nodeType) -> {
            var node = Node.create(nodeType);
            Assertions.assertEquals(dataType, node.getOutputWithName("output").orElseThrow().type());
        });
    }

    @Test
    public void isDefaultOutputValueCorrect() {
        BasicNodes.VALUE_PROVIDER_TYPES_WITH_DEFAULTS.forEach(databox -> {
            var nodeType = BasicNodes.VALUE_PROVIDERS.get(databox.type());
            var node = Node.create(nodeType);
            Assertions.assertEquals(databox.value(), nodeType.compute(UUID.randomUUID(), Map.of(), node.settings(), Map.of()).get("output").join().value());
        });
    }

    private static final Map<DataType<?>, ?> TEST_VALUES = Map.of(
        DataType.BOOLEAN, true,
        DataType.NUMBER, 3.14,
        DataType.STRING, "Hello World!"
    );

    @SuppressWarnings("unchecked")
    private static <T> DataBox<T> createDataBox(DataType<?> type, Object value) {
        return ((DataType<T>) type).create((T) value);
    }

    @Test
    public void isOutputValueCorrect() {
        BasicNodes.VALUE_PROVIDER_TYPES_WITH_DEFAULTS.forEach(databox -> {
            var nodeType = BasicNodes.VALUE_PROVIDERS.get(databox.type());
            var node = Node.create(nodeType).modifySetting("value", createDataBox(databox.type(), TEST_VALUES.get(databox.type())));
            Assertions.assertEquals(TEST_VALUES.get(databox.type()), nodeType.compute(UUID.randomUUID(), Map.of(), node.settings(), Map.of()).get("output").join().value());
        });
    }
}
