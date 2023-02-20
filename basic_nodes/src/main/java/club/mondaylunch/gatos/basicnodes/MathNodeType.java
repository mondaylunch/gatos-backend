package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class MathNodeType extends NodeType.Process {

    public static final DataType<Operator> MATHEMATICAL_OPERATOR = DataType.register("mathematicaloperator");

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of("operator", MATHEMATICAL_OPERATOR.create(Operator.ADDITION));
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "inputA", DataType.INTEGER),
            new NodeConnector.Input<>(nodeId, "inputB", DataType.INTEGER)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.INTEGER));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        double a = DataBox.get(inputs, "inputA", DataType.INTEGER).orElseThrow();
        double b = DataBox.get(inputs, "inputB", DataType.INTEGER).orElseThrow();

        Operator op = DataBox.get(settings, "operator", MATHEMATICAL_OPERATOR).orElseThrow();

        double result = op.apply(a, b);

        return Map.of("output", CompletableFuture.completedFuture(DataType.INTEGER.create((int) result)));
    }

    public enum Operator {
        ADDITION("+") {         @Override public double apply(double a, double b) { return a + b; }},
        SUBTRACTION("-") {      @Override public double apply(double a, double b) { return a - b; }},
        MULTIPLICATION("*") {   @Override public double apply(double a, double b) { return a * b; }},
        DIVISION("/") {         @Override public double apply(double a, double b) { return a / b; }};

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
}
