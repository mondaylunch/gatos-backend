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
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of("config", DataType.STRING.create(""));
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
        Number a = DataBox.get(inputs, "inputA", DataType.INTEGER).orElseThrow();
        Number b = DataBox.get(inputs, "inputB", DataType.INTEGER).orElseThrow();

        Number result = switch (DataBox.get(settings, "config", DataType.STRING).orElseThrow()) {
            case "+" -> operation(a, b, Operator.ADDITION);
            case "-" -> operation(a, b, Operator.SUBTRACTION);
            case "*" -> operation(a, b, Operator.MULTIPLICATION);
            case "/" -> operation(a, b, Operator.DIVISION);
            default -> 0;
        };

        return Map.of("output", CompletableFuture.completedFuture(DataType.INTEGER.create((int) result)));
    }

    public enum Operator {
        ADDITION("+") {
            @Override public int apply(int a, int b)          {   return a + b;   }
            @Override public long apply(long a, long b)       {   return a + b;   }
            @Override public float apply(float a, float b)    {   return a + b;   }
            @Override public double apply(double a, double b) {   return a + b;   }
        },
        SUBTRACTION("-") {
            @Override public int apply(int a, int b)          {   return a - b;   }
            @Override public long apply(long a, long b)       {   return a - b;   }
            @Override public float apply(float a, float b)    {   return a - b;   }
            @Override public double apply(double a, double b) {   return a - b;   }
        },
        MULTIPLICATION("*") {
            @Override public int apply(int a, int b)          {   return a * b;   }
            @Override public long apply(long a, long b)       {   return a * b;   }
            @Override public float apply(float a, float b)    {   return a * b;   }
            @Override public double apply(double a, double b) {   return a * b;   }
        },
        DIVISION("/") {
            @Override public int apply(int a, int b)          {   return a / b;   }
            @Override public long apply(long a, long b)       {   return a / b;   }
            @Override public float apply(float a, float b)    {   return a / b;   }
            @Override public double apply(double a, double b) {   return a / b;   }
        };

        private final String op;

        private Operator(String op) {
            this.op = op;
        }

        @Override
        public String toString() {
            return op;
        }

        public abstract int apply(int x1, int x2);
        public abstract long apply(long x1, long x2);
        public abstract float apply(float x1, float x2);
        public abstract double apply(double x1, double x2);
    }

    public Number operation(Number a, Number b, Operator op) {
        if(a instanceof Double || b instanceof Double) {
            return op.apply(a.doubleValue(), b.doubleValue());
        } else
        if(a instanceof Float || b instanceof Float) {
            return op.apply(a.floatValue(), b.floatValue());
        } else
        if(a instanceof Long || b instanceof Long) {
            return op.apply(a.longValue(), b.longValue());
        } else {    // must be int
            return op.apply(a.intValue(), b.intValue());
        }
    }
}
