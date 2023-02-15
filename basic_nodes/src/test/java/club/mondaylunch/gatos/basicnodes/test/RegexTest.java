package club.mondaylunch.gatos.basicnodes.test;

import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.basicnodes.RegexNodeType;

public class RegexTest {
    private static final RegexNodeType TEST_REGEX_NODE_TYPE = new RegexNodeType();

    @Test   
    public void nodeAddsToGraph() {
        var graph = new Graph();
        var node = graph.addNode(TEST_REGEX_NODE_TYPE);
        Assertions.assertTrue(graph.containsNode(node));
    }

    @Test
    public void matchesSingleCharToSingleChar() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("a"),
            "word", DataType.STRING.create("a")
        );
        var output = TEST_REGEX_NODE_TYPE.compute(input, input);
        Assertions.assertEquals(true, output.get("isMatch").join().value());
    }

    @Test
    public void matchesWordToWord() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("https://"),
            "word", DataType.STRING.create("https://my.website.co.uk")
        );
        var output = TEST_REGEX_NODE_TYPE.compute(input, input);
        Assertions.assertEquals(true, output.get("isMatch").join().value());
    }

    @Test
    public void doesntMatchOtherToWord() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("^https?://"),
            "word", DataType.STRING.create("totally a real website")
        );
        var output = TEST_REGEX_NODE_TYPE.compute(input, input);
        Assertions.assertEquals(false, output.get("isMatch").join().value());
    }

    @Test
    public void matchesBoolAndString() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("Hello"),
            "word", DataType.STRING.create("Hello")
        );
        var output = TEST_REGEX_NODE_TYPE.compute(input, input);
        Assertions.assertEquals(true, output.get("isMatch").join().value());
        Assertions.assertEquals("Hello", output.get("match").join().value());
    }

    @Test
    public void matchesBoolStringList() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("H(e)(l)lo"),
            "word", DataType.STRING.create("Hello")
        );
        var output = TEST_REGEX_NODE_TYPE.compute(input, input);
        Assertions.assertEquals(true, output.get("isMatch").join().value());
        Assertions.assertEquals("Hello", output.get("match").join().value());
        ArrayList<String> expected = new ArrayList<String>();
        expected.add("e"); expected.add("l");
        Assertions.assertEquals(expected, output.get("group").join().value());
    }

    @Test
    public void correctlyGetGroups() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("(1)(2)"),
            "word", DataType.STRING.create("12")
        );

        // TODO: remove debug print statement
        var output = TEST_REGEX_NODE_TYPE.compute(input, input);

        System.out.println(output.keySet().toString());

        Assertions.assertTrue(output.containsKey("group 1"));
        Assertions.assertEquals("1", output.get("group 1").join().value());
        Assertions.assertTrue(output.containsKey("group 2"));
        Assertions.assertEquals("2", output.get("group 2").join().value());
    }

    @Test
    public void matchesHello() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("H(e)(l)lo"),
            "word", DataType.STRING.create("Hello")
        );
        var output = TEST_REGEX_NODE_TYPE.compute(input, input);
        
        Assertions.assertEquals(true, output.get("isMatch").join().value());
        Assertions.assertEquals("Hello", output.get("match").join().value());
        
        ArrayList<String> expected = new ArrayList<String>();
        expected.add("e"); expected.add("l");
        Assertions.assertEquals(expected, output.get("group").join().value());
        
        Assertions.assertTrue(output.containsKey("group 1"));
        Assertions.assertEquals("e", output.get("group 1").join().value());
        Assertions.assertTrue(output.containsKey("group 2"));
        Assertions.assertEquals("l", output.get("group 2").join().value());
    }
}
