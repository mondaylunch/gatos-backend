package club.mondaylunch.gatos.basicnodes;

import club.mondaylunch.gatos.core.graph.type.NodeTypeRegistry;

public final class BasicNodes {
    public static final StringInterpolationNodeType STRING_INTERPOLATION = NodeTypeRegistry
            .register("string_interpolation", new StringInterpolationNodeType());
    public static final VariableExtractionNodeType VARIABLE_EXTRACTION = NodeTypeRegistry
            .register("variable_extraction", new VariableExtractionNodeType());
    public static final VariableRemappingNodeType VARIABLE_REMAPPING = NodeTypeRegistry
        .register("variable_remapping", new VariableRemappingNodeType());
    public static final StringLengthNodeType STRING_LENGTH = NodeTypeRegistry
        .register("string_length", new StringLengthNodeType());
    public static final StringContainsNodeType STRING_CONTAINS = NodeTypeRegistry
        .register("string_contains", new StringContainsNodeType());
    public static final StringConcatNodeType STRING_CONCAT = NodeTypeRegistry
        .register("string_concat", new StringConcatNodeType());
    public static final BooleanOperationNodeType BOOL_OP = NodeTypeRegistry
        .register("boolean_operation", new BooleanOperationNodeType());
}
