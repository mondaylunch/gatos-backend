package gay.oss.gatos.basicnodes;

import gay.oss.gatos.core.graph.type.NodeTypeRegistry;

public final class BasicNodes {
    public static final StringInterpolationNodeType STRING_INTERPOLATION = NodeTypeRegistry.register("string_interpolation", new StringInterpolationNodeType());
    public static final VariableExtractionNodeType VARIABLE_EXTRACTION = NodeTypeRegistry.register("variable_extraction", new VariableExtractionNodeType());
    public static final VariableRemappingNodeType VARIABLE_REMAPPING = NodeTypeRegistry.register("variable_remapping", new VariableRemappingNodeType());
}
