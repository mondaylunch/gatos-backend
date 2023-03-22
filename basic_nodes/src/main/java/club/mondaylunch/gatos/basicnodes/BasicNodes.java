package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.VisibleForTesting;

import club.mondaylunch.gatos.basicnodes.end.IncrementFlowDataNodeType;
import club.mondaylunch.gatos.basicnodes.end.MultiplyFlowDataNodeType;
import club.mondaylunch.gatos.basicnodes.end.RemoveFlowDataNodeType;
import club.mondaylunch.gatos.basicnodes.end.SetFlowDataNodeType;
import club.mondaylunch.gatos.basicnodes.end.WebhookEndNodeType;
import club.mondaylunch.gatos.basicnodes.process.BooleanOperationNodeType;
import club.mondaylunch.gatos.basicnodes.process.ContainsFlowDataNodeType;
import club.mondaylunch.gatos.basicnodes.process.EmptyListNodeType;
import club.mondaylunch.gatos.basicnodes.process.EqualsNodeType;
import club.mondaylunch.gatos.basicnodes.process.GetAtIndexNodeType;
import club.mondaylunch.gatos.basicnodes.process.GetFlowDataNodeType;
import club.mondaylunch.gatos.basicnodes.process.HTTPRequestNodeType;
import club.mondaylunch.gatos.basicnodes.process.IsFiniteNodeType;
import club.mondaylunch.gatos.basicnodes.process.IsNanNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListHeadSeparationNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListLengthNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListMappingNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListSetOperationNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListSortNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListTailSeparationNodeType;
import club.mondaylunch.gatos.basicnodes.process.MathNodeType;
import club.mondaylunch.gatos.basicnodes.process.NegationNodeType;
import club.mondaylunch.gatos.basicnodes.process.NumberComparisonNodeType;
import club.mondaylunch.gatos.basicnodes.process.ObjectSetValueNodeType;
import club.mondaylunch.gatos.basicnodes.process.OptionalOrElseNodeType;
import club.mondaylunch.gatos.basicnodes.process.ParseStringToNumberNodeType;
import club.mondaylunch.gatos.basicnodes.process.StringConcatNodeType;
import club.mondaylunch.gatos.basicnodes.process.StringContainsNodeType;
import club.mondaylunch.gatos.basicnodes.process.StringInterpolationNodeType;
import club.mondaylunch.gatos.basicnodes.process.StringLengthNodeType;
import club.mondaylunch.gatos.basicnodes.process.TruthinessNodeType;
import club.mondaylunch.gatos.basicnodes.process.ValueProviderNodeType;
import club.mondaylunch.gatos.basicnodes.process.ValueReplacerNodeType;
import club.mondaylunch.gatos.basicnodes.process.VariableExtractionNodeType;
import club.mondaylunch.gatos.basicnodes.process.VariableRemappingNodeType;
import club.mondaylunch.gatos.basicnodes.start.WebhookStartNodeType;
import club.mondaylunch.gatos.core.GatosPlugin;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public final class BasicNodes implements GatosPlugin {
    @Override
    public void init() {
    }

    @Override
    public String name() {
        return "basic_nodes";
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
    public static final GetAtIndexNodeType GET_AT_INDEX = NodeType.REGISTRY
        .register("get_at_index", new GetAtIndexNodeType());
    public static final ListMappingNodeType LIST_MAPPING = NodeType.REGISTRY
        .register("list_mapping", new ListMappingNodeType());
    public static final OptionalOrElseNodeType OPTIONAL_OR_ELSE = NodeType.REGISTRY
        .register("optional_or_else", new OptionalOrElseNodeType());
    public static final ListHeadSeparationNodeType LIST_HEAD_SEPARATION = NodeType.REGISTRY
        .register("list_head_separation", new ListHeadSeparationNodeType());
    public static final ListTailSeparationNodeType LIST_TAIL_SEPARATION = NodeType.REGISTRY
        .register("list_tail_separation", new ListTailSeparationNodeType());
    public static final ListSortNodeType LIST_SORT = NodeType.REGISTRY
        .register("list_sort", new ListSortNodeType());
    public static final ListSetOperationNodeType LIST_SET_OPERATION = NodeType.REGISTRY
        .register("list_set_operation", new ListSetOperationNodeType());
    public static final EqualsNodeType EQUALS = NodeType.REGISTRY
        .register("equals", new EqualsNodeType());
    public static final TruthinessNodeType TRUTHINESS = NodeType.REGISTRY
        .register("truthiness", new TruthinessNodeType());
    public static final NegationNodeType NEGATION = NodeType.REGISTRY
        .register("negation", new NegationNodeType());
    public static final ParseStringToNumberNodeType PARSE_STRING_TO_NUMBER = NodeType.REGISTRY
        .register("parse_string_to_number", new ParseStringToNumberNodeType());
    public static final HTTPRequestNodeType HTTP_REQUEST = NodeType.REGISTRY
        .register("http_request", new HTTPRequestNodeType());
    public static final WebhookStartNodeType WEBHOOK_START = NodeType.REGISTRY
        .register("webhook_start", new WebhookStartNodeType());
    public static final WebhookEndNodeType WEBHOOK_END = NodeType.REGISTRY
        .register("webhook_end", new WebhookEndNodeType());
    public static final EmptyListNodeType EMPTY_LIST = NodeType.REGISTRY
        .register("empty_list", new EmptyListNodeType());
    public static final ObjectSetValueNodeType OBJECT_SET_VALUE = NodeType.REGISTRY
        .register("object_set_value", new ObjectSetValueNodeType());
    public static final GetFlowDataNodeType GET_FLOW_DATA = NodeType.REGISTRY
        .register("get_flow_data", new GetFlowDataNodeType());
    public static final SetFlowDataNodeType SET_FLOW_DATA = NodeType.REGISTRY
        .register("set_flow_data", new SetFlowDataNodeType());
    public static final ContainsFlowDataNodeType CONTAINS_FLOW_DATA = NodeType.REGISTRY
        .register("contains_flow_data", new ContainsFlowDataNodeType());
    public static final RemoveFlowDataNodeType REMOVE_FLOW_DATA = NodeType.REGISTRY
        .register("remove_flow_data", new RemoveFlowDataNodeType());
    public static final IncrementFlowDataNodeType INCREMENT_FLOW_DATA = NodeType.REGISTRY
        .register("increment_flow_data", new IncrementFlowDataNodeType());
    public static final MultiplyFlowDataNodeType MULTIPLY_FLOW_DATA = NodeType.REGISTRY
        .register("multiply_flow_data", new MultiplyFlowDataNodeType());

    @VisibleForTesting
    public static final Set<DataBox<?>> VALUE_PROVIDER_TYPES_WITH_DEFAULTS = Set.of(
        DataType.STRING.create(""),
        DataType.NUMBER.create(0.0),
        DataType.BOOLEAN.create(false)
    );

    public static final Map<DataType<?>, ValueProviderNodeType<?>> VALUE_PROVIDERS = VALUE_PROVIDER_TYPES_WITH_DEFAULTS.stream()
        .collect(Collectors.toMap(
            DataBox::type,
            box -> NodeType.REGISTRY.register("value_provider_" + DataType.REGISTRY.getName(box.type()).orElseThrow(), new ValueProviderNodeType<>(box))
        ));

    public static final Map<DataType<?>, ValueReplacerNodeType<?>> VALUE_REPLACERS = VALUE_PROVIDER_TYPES_WITH_DEFAULTS.stream()
        .collect(Collectors.toMap(
            DataBox::type,
            box -> NodeType.REGISTRY.register("value_replacer_" + DataType.REGISTRY.getName(box.type()).orElseThrow(), new ValueReplacerNodeType<>(box))
        ));
}
