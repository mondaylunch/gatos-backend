package club.mondaylunch.gatos.basicnodes.process;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class MathNodeType extends NodeType.Process {

    public static final DataType<Operator> MATHEMATICAL_OPERATOR = DataType.register("mathematicaloperator", Operator.class);

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of("operator", MATHEMATICAL_OPERATOR.create(Operator.ADDITION));
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
            new NodeConnector.Output<>(nodeId, "output", DataType.NUMBER));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        double a = DataBox.get(inputs, "inputA", DataType.NUMBER).orElseThrow();
        double b = DataBox.get(inputs, "inputB", DataType.NUMBER).orElseThrow();

        Operator op = DataBox.get(settings, "operator", MATHEMATICAL_OPERATOR).orElseThrow();

        double result = op.apply(a, b);

        return Map.of("output", CompletableFuture.completedFuture(DataType.NUMBER.create(result)));
    }

    public enum Operator {
        ADDITION("+") {
            @Override
            public double apply(double a, double b) {
                return a + b;
            }
        },
        SUBTRACTION("-") {
            @Override
            public double apply(double a, double b) {
                return a - b;
            }
        },
        MULTIPLICATION("*") {
            @Override
            public double apply(double a, double b) {
                return a * b;
            }
        },
        DIVISION("/") {
            @Override
            public double apply(double a, double b) {
                return a / b;
            }
        };

        private final String op;

        Operator(String op) {
            this.op = op;
        }

        @Override
        public String toString() {
            return this.op;
        }

        public abstract double apply(double a, double b);
    }

    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        return this.compute(UUID.randomUUID(), inputs, settings, Map.of());
    }

    /**
     * if no mode is given default to addition.
     *
     * @param inputs a map of the two inputted doubles
     * @return a map of the computed result
     */
    @SuppressWarnings("unused")
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs) {
        return this.compute(UUID.randomUUID(), inputs, this.settings(), Map.of());
    }
}
