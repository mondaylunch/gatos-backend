package club.mondaylunch.gatos.basicnodes;

import club.mondaylunch.gatos.core.graph.type.NodeTypeRegistry;

public final class BasicNodes {
    public static final StringInterpolationNodeType STRING_INTERPOLATION = NodeTypeRegistry
            .register(new StringInterpolationNodeType());
    public static final VariableExtractionNodeType VARIABLE_EXTRACTION = NodeTypeRegistry
            .register(new VariableExtractionNodeType());
    public static final VariableRemappingNodeType VARIABLE_REMAPPING = NodeTypeRegistry
        .register(new VariableRemappingNodeType());
}
