package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class BooleanOperationNodeType extends NodeType.Process {

    public static final DataType<Mode> BOOL_OPERATION_MODE = DataType.register("booloperationmode");

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
                "mode", BOOL_OPERATION_MODE.create(Mode.NOT)
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings) {
        if(DataBox.get(settings, "mode", BOOL_OPERATION_MODE).orElseThrow().equals(Mode.NOT))
            return Set.of(new NodeConnector.Input<>(nodeId, "input", DataType.BOOLEAN));

        return Set.of(
                new NodeConnector.Input<>(nodeId, "inputA", DataType.BOOLEAN),
                new NodeConnector.Input<>(nodeId, "inputB", DataType.BOOLEAN)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings) {
        return Set.of(
                new NodeConnector.Output<>(nodeId, "output", DataType.BOOLEAN));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        Mode mode = DataBox.get(settings, "mode", BOOL_OPERATION_MODE).orElseThrow();
        if(mode.equals(Mode.NOT))
            return Map.of("output", CompletableFuture.completedFuture(DataType.BOOLEAN.create(
                !DataBox.get(inputs, "input", DataType.BOOLEAN).orElseThrow()
            )));

        boolean a = DataBox.get(inputs, "inputA", DataType.BOOLEAN).orElseThrow();
        boolean b = DataBox.get(inputs, "inputB", DataType.BOOLEAN).orElseThrow();

        boolean result = mode.apply(a, b);

        return Map.of("output", CompletableFuture.completedFuture(DataType.BOOLEAN.create(result)));
    }

    public enum Mode {
        OR("or") {
            @Override public boolean apply(boolean a, boolean b) { return a | b; }
        },
        AND("and") {
            @Override public boolean apply(boolean a, boolean b) { return a & b; }
        },
        XOR("xor") {
            @Override public boolean apply(boolean a, boolean b) { return a ^ b; }
        },
        NOT("not") {
            public boolean apply(boolean a) { return apply(a, false); }
            @Override public boolean apply(boolean a, boolean b) { return !a; }
        };

        private final String op;

        Mode(String op) {
            this.op = op;
        }

        @Override
        public String toString() {
            return this.op;
        }
        public abstract boolean apply(boolean a, boolean b);
    }

}
