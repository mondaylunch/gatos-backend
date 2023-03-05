package club.mondaylunch.gatos.basicnodes;

import club.mondaylunch.gatos.core.graph.type.NodeType;

public final class BasicNodes {

    /**
     * Class load this class to register node types.
     */
    public static void init() {
    }

    public static final StringInterpolationNodeType STRING_INTERPOLATION = NodeType.REGISTRY
        .register("string_interpolation", new StringInterpolationNodeType());
    public static final VariableExtractionNodeType VARIABLE_EXTRACTION = NodeType.REGISTRY
        .register("variable_extraction", new VariableExtractionNodeType());
    public static final VariableRemappingNodeType VARIABLE_REMAPPING = NodeType.REGISTRY
        .register("variable_remapping", new VariableRemappingNodeType());
    public static final StringLengthNodeType STRING_LENGTH = NodeType.REGISTRY
        .register("string_length", new StringLengthNodeType());
    public static final StringContainsNodeType STRING_CONTAINS = NodeType.REGISTRY
        .register("string_contains", new StringContainsNodeType());
    public static final StringConcatNodeType STRING_CONCAT = NodeType.REGISTRY
        .register("string_concat", new StringConcatNodeType());
    public static final NumberComparisonNodeType NUMBER_COMPARISON = NodeType.REGISTRY
        .register("number_comparison", new NumberComparisonNodeType());
    public static final MathNodeType MATH = NodeType.REGISTRY
        .register("math", new MathNodeType());
    public static final IsFiniteNodeType IS_FINITE = NodeType.REGISTRY
        .register("is_finite", new IsFiniteNodeType());
    public static final IsNanNodeType IS_NAN = NodeType.REGISTRY
        .register("is_nan", new IsNanNodeType());
    public static final BooleanOperationNodeType BOOL_OP = NodeType.REGISTRY
        .register("boolean_operation", new BooleanOperationNodeType());
    public static final ListLengthNodeType LIST_LENGTH = NodeType.REGISTRY
        .register("list_length", new ListLengthNodeType());
    public static final ListHeadTailNodeType LIST_HEADTAIL = NodeType.REGISTRY
        .register("list_headtail", new ListHeadTailNodeType());
    public static final EqualsNodeType EQUALS = NodeType.REGISTRY
        .register("equals", new EqualsNodeType());
    public static final TruthinessNodeType TRUTHINESS = NodeType.REGISTRY
        .register("truthiness", new TruthinessNodeType());
    public static final HTTPRequestNodeType HTTP_REQUEST = NodeType.REGISTRY
        .register("http_request", new HTTPRequestNodeType());
}
