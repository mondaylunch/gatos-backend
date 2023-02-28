package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class NumberComparisonNodeType extends NodeType.Process {

    public static final DataType<Mode> NUMBER_ORDERING_MODE = DataType.register("number_ordering_type", Mode.class);

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of("mode", NUMBER_ORDERING_MODE.create(Mode.GREATERTHAN));
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "inputA", DataType.NUMBER),
            new NodeConnector.Input<>(nodeId, "inputB", DataType.NUMBER)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.BOOLEAN));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Map.of("output", CompletableFuture.completedFuture(DataType.BOOLEAN.create(
            DataBox.get(settings, "mode", NUMBER_ORDERING_MODE).orElseThrow().apply(
                DataBox.get(inputs, "inputA", DataType.NUMBER).orElseThrow(),
                DataBox.get(inputs, "inputB", DataType.NUMBER).orElseThrow()
            )
        )));
    }

    public enum Mode {
        GREATERTHAN     (">")   {@Override public boolean apply(double a, double b) { return a >  b; }},
        GREATERTHANEQ   (">=")  {@Override public boolean apply(double a, double b) { return a >= b; }},
        LESSTHAN        ("<")   {@Override public boolean apply(double a, double b) { return a <  b; }},
        LESSTHANEQ      ("<=")  {@Override public boolean apply(double a, double b) { return a <= b; }};

        private final String mode;

        private Mode(String mode) {
            this.mode = mode;
        }

        @Override
        public String toString() {
            return mode;
        }

        public abstract boolean apply(double a, double b);
    }

    public Map<String, CompletableFuture<DataBox<?>>> compute (Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        return compute(inputs, settings, Map.of());
    }
 
    /**
     * If no mode is given default to greaterthan
     * @param inputs    a map of the two given doubles
     * @return          a map of the computed result
     */
    public Map<String, CompletableFuture<DataBox<?>>> compute (Map<String, DataBox<?>> inputs) {
        return compute(inputs, settings(), Map.of());
    }
}
