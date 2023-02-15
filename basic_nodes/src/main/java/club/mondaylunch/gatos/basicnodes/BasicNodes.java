package club.mondaylunch.gatos.basicnodes;

import club.mondaylunch.gatos.core.graph.type.NodeTypeRegistry;

public final class BasicNodes {
    public static final StringInterpolationNodeType STRING_INTERPOLATION = NodeTypeRegistry
            .register(new StringInterpolationNodeType());
    public static final VariableExtractionNodeType VARIABLE_EXTRACTION = NodeTypeRegistry
            .register(new VariableExtractionNodeType());
    public static final VariableRemappingNodeType VARIABLE_REMAPPING = NodeTypeRegistry
        .register(new VariableRemappingNodeType());
    public static final StringLengthNodeType STRING_LENGTH = NodeTypeRegistry
        .register(new StringLengthNodeType());
    public static final StringContainsNodeType STRING_CONTAINS = NodeTypeRegistry
        .register(new StringContainsNodeType());
    public static final StringConcatNodeType STRING_CONCAT = NodeTypeRegistry
        .register(new StringConcatNodeType());
    public static final ListLengthNodeType LIST_LENGTH = NodeTypeRegistry
        .register(new ListLengthNodeType());
}
