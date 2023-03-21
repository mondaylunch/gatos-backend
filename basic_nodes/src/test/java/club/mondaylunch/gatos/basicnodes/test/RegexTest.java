package club.mondaylunch.gatos.basicnodes.test;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.basicnodes.BasicNodes;

public class RegexTest {

    @Test   
    public void nodeAddsToGraph() {
        var graph = new Graph();
        var node = graph.addNode(BasicNodes.REGEX);
        Assertions.assertTrue(graph.containsNode(node));
    }

    @Test
    public void matchesSingleCharToSingleChar() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("a"),
            "word", DataType.STRING.create("a")
        );
        var output = BasicNodes.REGEX.compute(input);
        Assertions.assertTrue((boolean) output.get("isMatch").join().value());
    }

    @Test
    public void matchesWordToWord() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("https://"),
            "word", DataType.STRING.create("https://my.website.co.uk")
        );
        var output = BasicNodes.REGEX.compute(input);
        Assertions.assertTrue((boolean) output.get("isMatch").join().value());
    }

    @Test
    public void doesntMatchOtherToWord() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("^https?://"),
            "word", DataType.STRING.create("totally a real website")
        );
        var output = BasicNodes.REGEX.compute(input);
        Assertions.assertFalse((boolean) output.get("isMatch").join().value());
    }

    @Test
    public void matchesBoolAndString() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("Hello"),
            "word", DataType.STRING.create("Hello")
        );
        var output = BasicNodes.REGEX.compute(input);
        Assertions.assertTrue((boolean) output.get("isMatch").join().value());
        Assertions.assertEquals("Hello", output.get("match").join().value());
    }

    @Test
    public void matchesGroupList() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("(1)(2)"),
            "word", DataType.STRING.create("12")
        );

        var output = BasicNodes.REGEX.compute(input);

        Assertions.assertTrue((boolean) output.get("isMatch").join().value());
        Assertions.assertEquals(List.of("1", "2"), output.get("groups").join().value());
    }

    @Test
    public void matchesHello() {
        Map<String, DataBox<?>> input = Map.of(
            "regex", DataType.STRING.create("H(e)(l)lo"),
            "word", DataType.STRING.create("Hello")
        );
        var output = BasicNodes.REGEX.compute(input);
        
        Assertions.assertEquals(true, output.get("isMatch").join().value());
        Assertions.assertEquals("Hello", output.get("match").join().value());
        Assertions.assertEquals(List.of("e", "l"), output.get("groups").join().value());
    }
}
