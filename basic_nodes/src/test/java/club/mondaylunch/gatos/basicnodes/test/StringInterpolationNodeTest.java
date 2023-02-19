package gay.oss.gatos.basicnodes.test;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gay.oss.gatos.basicnodes.BasicNodes;
import gay.oss.gatos.core.data.DataBox;
import gay.oss.gatos.core.data.DataType;
import gay.oss.gatos.core.graph.Node;

public class StringInterpolationNodeTest {
    private static final String TEST_TEMPLATE_STRING = "{} {template_param_1} - {a thing} {} {} {} \\{not one}";

    @Test
    public void hasCorrectInputs() {
        var node = Node.create(BasicNodes.STRING_INTERPOLATION)
            .modifySetting("template", DataType.STRING.create(TEST_TEMPLATE_STRING));
        Assertions.assertEquals(6, node.inputs().size());
    }

    @Test
    public void paramNamesAreCorrect() {
        var node = Node.create(BasicNodes.STRING_INTERPOLATION)
            .modifySetting("template", DataType.STRING.create(TEST_TEMPLATE_STRING));
        Assertions.assertTrue(node.inputs().containsKey("Placeholder 1"));
        Assertions.assertTrue(node.inputs().containsKey("template_param_1"));
        Assertions.assertTrue(node.inputs().containsKey("a thing"));
        Assertions.assertTrue(node.inputs().containsKey("Placeholder 4"));
        Assertions.assertTrue(node.inputs().containsKey("Placeholder 5"));
        Assertions.assertTrue(node.inputs().containsKey("Placeholder 6"));
    }

    @Test
    public void hasOutput() {
        var node = Node.create(BasicNodes.STRING_INTERPOLATION);
        Assertions.assertEquals(1, node.outputs().size());
        Assertions.assertTrue(node.outputs().containsKey("result"));
    }

    @Test
    public void computesCorrectly() {
        var node = Node.create(BasicNodes.STRING_INTERPOLATION)
            .modifySetting("template", DataType.STRING.create(TEST_TEMPLATE_STRING));
        Map<String, DataBox<?>> inputs = Map.of(
            "Placeholder 1", DataType.STRING.create("This is a"),
            "template_param_1", DataType.STRING.create("test node"),
            "a thing", DataType.STRING.create("did the"),
            "Placeholder 4", DataType.STRING.create("template substitution"),
            "Placeholder 5", DataType.STRING.create("work correctly?"),
            "Placeholder 6", DataType.STRING.create("This one shouldn't:")
        );
        var result = (String) BasicNodes.STRING_INTERPOLATION.compute(inputs, node.settings()).get("result").join().value();
        Assertions.assertEquals("This is a test node - did the template substitution work correctly? This one shouldn't: \\{not one}", result);
    }
}
