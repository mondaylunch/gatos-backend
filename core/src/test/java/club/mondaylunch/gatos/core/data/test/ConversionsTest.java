package club.mondaylunch.gatos.core.data.test;

import java.util.List;
import java.util.Optional;

import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.gson.JsonArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.data.Conversions;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;

public class ConversionsTest {
    private static final DataType<Foo> FOO_TYPE = DataType.register("foo", Foo.class);
    private static final DataType<Bar> BAR_TYPE = DataType.register("bar", Bar.class);
    private static final DataType<Baz> BAZ_TYPE = DataType.register("baz", Baz.class);

    @Test
    public void canRegisterConversion() {
        Assertions.assertDoesNotThrow(() ->
            Conversions.register(FOO_TYPE, BAR_TYPE, foo -> new Bar(foo.name()))
        );
    }

    @Test
    public void canConvertIsCorrect() {
        Conversions.register(FOO_TYPE, BAR_TYPE, foo -> new Bar(foo.name()));
        Assertions.assertTrue(Conversions.canConvert(FOO_TYPE, BAR_TYPE));
        Assertions.assertFalse(Conversions.canConvert(BAR_TYPE, FOO_TYPE));
    }

    @Test
    public void canConvertIsCorrectTransitive() {
        Conversions.register(FOO_TYPE, BAR_TYPE, foo -> new Bar(foo.name()));
        Conversions.register(BAR_TYPE, BAZ_TYPE, bar -> new Baz(bar.name()));
        Assertions.assertTrue(Conversions.canConvert(FOO_TYPE, BAZ_TYPE));
    }

    @Test
    public void canConvertToSelf() {
        Assertions.assertTrue(Conversions.canConvert(FOO_TYPE, FOO_TYPE));
        var foo = FOO_TYPE.create(new Foo("hello!"));
        Assertions.assertEquals(foo, Conversions.convert(foo, FOO_TYPE));
    }

    @Test
    public void testConversion() {
        Conversions.register(FOO_TYPE, BAR_TYPE, foo -> new Bar(foo.name()));
        var foo = FOO_TYPE.create(new Foo("hello!"));
        var bar = BAR_TYPE.create(new Bar("hello!"));
        Assertions.assertEquals(bar, Conversions.convert(foo, BAR_TYPE));
    }

    @Test
    public void incorrectConversionFails() {
        Conversions.register(FOO_TYPE, BAR_TYPE, foo -> new Bar(foo.name()));
        var bar = BAR_TYPE.create(new Bar("hello!"));
        Assertions.assertThrows(
            Conversions.ConversionException.class,
            () -> Conversions.convert(bar, FOO_TYPE)
        );
    }

    @Test
    public void nullConversionFails() {
        Conversions.register(FOO_TYPE, BAZ_TYPE, foo -> null);
        var foo = FOO_TYPE.create(new Foo("hello!"));
        Assertions.assertThrows(
            Conversions.ConversionException.class,
            () -> Conversions.convert(foo, BAZ_TYPE)
        );
    }

    @Test
    public void canConvertListType() {
        var list = List.of(new Foo("hello!"));
        var fooList = FOO_TYPE.listOf().create(list);
        Assertions.assertDoesNotThrow(() -> {
            Conversions.convert(fooList, ListDataType.GENERIC_LIST);
        });
    }

    @Test
    public void canConvertOptionalType() {
        var opt = Optional.of(new Foo("hello!"));
        var fooOpt = FOO_TYPE.optionalOf().create(opt);
        Assertions.assertDoesNotThrow(() -> {
            Conversions.convert(fooOpt, OptionalDataType.GENERIC_OPTIONAL);
        });
    }

    @Test
    public void canConvertToAny() {
        var foo = FOO_TYPE.create(new Foo("Hello!"));
        Assertions.assertDoesNotThrow(() -> {
            Conversions.convert(foo, DataType.ANY);
        });
    }

    @Test
    public void canConvertToString() {
        var foo = FOO_TYPE.create(new Foo("Hello!"));
        Assertions.assertEquals(foo.value().toString(), Conversions.convert(foo, DataType.STRING).value());
    }

    @Test
    public void canConvertToStringList() {
        var foo = new Foo("Hello!");
        var fooListBox = FOO_TYPE.listOf().create(List.of(foo));
        Assertions.assertEquals(List.of(foo.toString()), Conversions.convert(fooListBox, DataType.STRING.listOf()).value());
    }

    @Test
    public void canConvertTransitive() {
        Conversions.register(FOO_TYPE, BAR_TYPE, foo -> new Bar(foo.name()));
        Conversions.register(BAR_TYPE, BAZ_TYPE, bar -> new Baz(bar.name()));
        var foo = FOO_TYPE.create(new Foo("hello!"));
        var baz = BAZ_TYPE.create(new Baz("hello!"));
        Assertions.assertEquals(baz, Conversions.convert(foo, BAZ_TYPE));
    }

    @Test
    public void preferDirectConversion() {
        Conversions.register(FOO_TYPE, BAR_TYPE, foo -> new Bar(foo.name()));
        Conversions.register(BAR_TYPE, BAZ_TYPE, bar -> new Baz(bar.name()));
        Conversions.register(FOO_TYPE, BAZ_TYPE, foo -> new Baz("direct " + foo.name()));
        var foo = FOO_TYPE.create(new Foo("hello!"));
        var baz = BAZ_TYPE.create(new Baz("direct hello!"));
        Assertions.assertEquals(baz, Conversions.convert(foo, BAZ_TYPE));
    }

    @Test
    public void canConvertToJsonArray() {
        var numbers = List.of(1.0, 2.0, 3.0);
        var numbersBox = DataType.NUMBER.listOf().create(numbers);
        var jsonBox = DataType.JSON_ELEMENT.create(numbers.stream().collect(
            JsonArray::new,
            JsonArray::add,
            JsonArray::addAll
        ));
        Assertions.assertEquals(jsonBox, Conversions.convert(numbersBox, DataType.JSON_ELEMENT));
    }

    @Test
    public void canGetPath() {
        var graph = createGraph();
        var path = getPath(graph, "node5", "node7").orElseThrow();
        Assertions.assertIterableEquals(List.of("edge3", "edge4"), path);
    }

    @Test
    public void cannotGetPath() {
        var graph = createGraph();
        var pathOptional = getPath(graph, "node1", "node3");
        Assertions.assertTrue(pathOptional.isEmpty());
    }

    @Test
    public void cannotGetReversePath() {
        var graph = createGraph();
        var pathOptional = getPath(graph, "node7", "node5");
        Assertions.assertTrue(pathOptional.isEmpty());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    public void canGetCyclePath() {
        var graph = ValueGraphBuilder.directed()
            .<String, String>immutable()
            .putEdgeValue("node1", "node2", "edge1")
            .putEdgeValue("node2", "node3", "edge2")
            .putEdgeValue("node3", "node1", "edge3")
            .build();
        var path = getPath(graph, "node1", "node3").orElseThrow();
        Assertions.assertIterableEquals(List.of("edge1", "edge2"), path);
    }

    @Test
    public void canGetSelfPath() {
        var graph = createGraph();
        var path = getPath(graph, "node1", "node1").orElseThrow();
        Assertions.assertTrue(path.isEmpty());
    }

    @Test
    public void cannotGetNonExistentNodePath() {
        var graph = createGraph();
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> getPath(graph, "start", "end")
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private static ValueGraph<String, String> createGraph() {
        return ValueGraphBuilder.directed()
            .<String, String>immutable()
            .putEdgeValue("node1", "node2", "edge1")
            .putEdgeValue("node3", "node4", "edge2")
            .putEdgeValue("node5", "node6", "edge3")
            .putEdgeValue("node6", "node7", "edge4")
            .build();
    }

    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    private static <N, V> Optional<List<V>> getPath(ValueGraph<N, V> graph, N start, N end) {
        try {
            var method = Conversions.class.getDeclaredMethod("getPath", ValueGraph.class, Object.class, Object.class);
            method.setAccessible(true);
            return (Optional<List<V>>) method.invoke(null, graph, start, end);
        } catch (Exception e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw new IllegalArgumentException(e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private record Foo(String name) {
    }

    private record Bar(String name) {
    }

    private record Baz(String name) {
    }
}
