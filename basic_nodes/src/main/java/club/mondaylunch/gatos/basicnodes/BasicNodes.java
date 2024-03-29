package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.VisibleForTesting;

import club.mondaylunch.gatos.basicnodes.end.IncrementUserDataNodeType;
import club.mondaylunch.gatos.basicnodes.end.MultiplyUserDataNodeType;
import club.mondaylunch.gatos.basicnodes.end.RemoveUserDataNodeType;
import club.mondaylunch.gatos.basicnodes.end.SetUserDataNodeType;
import club.mondaylunch.gatos.basicnodes.end.WebhookEndNodeType;
import club.mondaylunch.gatos.basicnodes.process.AddElementToListNodeType;
import club.mondaylunch.gatos.basicnodes.process.BooleanOperationNodeType;
import club.mondaylunch.gatos.basicnodes.process.ContainsUserDataNodeType;
import club.mondaylunch.gatos.basicnodes.process.EmptyListNodeType;
import club.mondaylunch.gatos.basicnodes.process.EmptyOptionalNodeType;
import club.mondaylunch.gatos.basicnodes.process.EqualsNodeType;
import club.mondaylunch.gatos.basicnodes.process.GetAtIndexNodeType;
import club.mondaylunch.gatos.basicnodes.process.GetUserDataNodeType;
import club.mondaylunch.gatos.basicnodes.process.HTTPRequestNodeType;
import club.mondaylunch.gatos.basicnodes.process.IsFiniteNodeType;
import club.mondaylunch.gatos.basicnodes.process.IsNanNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListContainsNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListDistinctNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListHeadSeparationNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListLengthNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListMappingNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListReverseNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListSetOperationNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListSortNodeType;
import club.mondaylunch.gatos.basicnodes.process.ListTailSeparationNodeType;
import club.mondaylunch.gatos.basicnodes.process.MathNodeType;
import club.mondaylunch.gatos.basicnodes.process.NegationNodeType;
import club.mondaylunch.gatos.basicnodes.process.NumberComparisonNodeType;
import club.mondaylunch.gatos.basicnodes.process.ObjectSetValueNodeType;
import club.mondaylunch.gatos.basicnodes.process.OptionalCreationNodeType;
import club.mondaylunch.gatos.basicnodes.process.OptionalFilterNodeType;
import club.mondaylunch.gatos.basicnodes.process.OptionalOrElseNodeType;
import club.mondaylunch.gatos.basicnodes.process.ParseStringToNumberNodeType;
import club.mondaylunch.gatos.basicnodes.process.ParseStringToObjectNodeType;
import club.mondaylunch.gatos.basicnodes.process.RegexNodeType;
import club.mondaylunch.gatos.basicnodes.process.RemoveElementFromListNodeType;
import club.mondaylunch.gatos.basicnodes.process.StringCaseNodeType;
import club.mondaylunch.gatos.basicnodes.process.StringConcatNodeType;
import club.mondaylunch.gatos.basicnodes.process.StringContainsNodeType;
import club.mondaylunch.gatos.basicnodes.process.StringInterpolationNodeType;
import club.mondaylunch.gatos.basicnodes.process.StringLengthNodeType;
import club.mondaylunch.gatos.basicnodes.process.StringRegexReplacementNodeType;
import club.mondaylunch.gatos.basicnodes.process.StringRegexSplitNodeType;
import club.mondaylunch.gatos.basicnodes.process.TruthinessNodeType;
import club.mondaylunch.gatos.basicnodes.process.VariableExtractionNodeType;
import club.mondaylunch.gatos.basicnodes.process.VariableRemappingNodeType;
import club.mondaylunch.gatos.basicnodes.start.WebhookStartNodeType;
import club.mondaylunch.gatos.core.GatosPlugin;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.util.ValueProviderNodeType;
import club.mondaylunch.gatos.core.util.ValueReplacerNodeType;

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
    public static final StringCaseNodeType STRING_CASE = NodeType.REGISTRY
        .register("string_case", new StringCaseNodeType());
    public static final StringRegexReplacementNodeType STRING_REGEX_REPLACE = NodeType.REGISTRY
        .register("string_regex_replace", new StringRegexReplacementNodeType());
    public static final StringRegexSplitNodeType STRING_REGEX_SPLIT = NodeType.REGISTRY
        .register("string_regex_split", new StringRegexSplitNodeType());
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
    public static final ListReverseNodeType LIST_REVERSE = NodeType.REGISTRY
        .register("list_reverse", new ListReverseNodeType());
    public static final ListDistinctNodeType LIST_DISTINCT = NodeType.REGISTRY
        .register("list_distinct", new ListDistinctNodeType());
    public static final ListContainsNodeType LIST_CONTAINS = NodeType.REGISTRY
        .register("list_contains", new ListContainsNodeType());
    public static final ListSetOperationNodeType LIST_SET_OPERATION = NodeType.REGISTRY
        .register("list_set_operation", new ListSetOperationNodeType());
    public static final AddElementToListNodeType ADD_ELEM_TO_LIST = NodeType.REGISTRY
        .register("add_element_to_list", new AddElementToListNodeType());
    public static final RemoveElementFromListNodeType REMOVE_ELEM_FROM_LIST = NodeType.REGISTRY
        .register("remove_element_from_list", new RemoveElementFromListNodeType());
    public static final EqualsNodeType EQUALS = NodeType.REGISTRY
        .register("equals", new EqualsNodeType());
    public static final TruthinessNodeType TRUTHINESS = NodeType.REGISTRY
        .register("truthiness", new TruthinessNodeType());
    public static final NegationNodeType NEGATION = NodeType.REGISTRY
        .register("negation", new NegationNodeType());
    public static final ParseStringToNumberNodeType PARSE_STRING_TO_NUMBER = NodeType.REGISTRY
        .register("parse_string_to_number", new ParseStringToNumberNodeType());
    public static final ParseStringToObjectNodeType PARSE_STRING_TO_OBJECT = NodeType.REGISTRY
        .register("parse_string_to_object", new ParseStringToObjectNodeType());
    public static final HTTPRequestNodeType HTTP_REQUEST = NodeType.REGISTRY
        .register("http_request", new HTTPRequestNodeType());
    public static final WebhookStartNodeType WEBHOOK_START = NodeType.REGISTRY
        .register("webhook_start", new WebhookStartNodeType());
    public static final WebhookEndNodeType WEBHOOK_END = NodeType.REGISTRY
        .register("webhook_end", new WebhookEndNodeType());
    public static final EmptyListNodeType EMPTY_LIST = NodeType.REGISTRY
        .register("empty_list", new EmptyListNodeType());
    public static final EmptyOptionalNodeType EMPTY_OPTIONAL = NodeType.REGISTRY
        .register("empty_optional", new EmptyOptionalNodeType());
    public static final OptionalCreationNodeType OPTIONAL_CREATION = NodeType.REGISTRY
        .register("optional_creation", new OptionalCreationNodeType());
    public static final OptionalFilterNodeType OPTIONAL_FILTER = NodeType.REGISTRY
        .register("optional_filter", new OptionalFilterNodeType());
    public static final ObjectSetValueNodeType OBJECT_SET_VALUE = NodeType.REGISTRY
        .register("object_set_value", new ObjectSetValueNodeType());
    public static final RegexNodeType REGEX = NodeType.REGISTRY
        .register("regex", new RegexNodeType());
    public static final GetUserDataNodeType GET_USER_DATA = NodeType.REGISTRY
        .register("get_user_data", new GetUserDataNodeType());
    public static final SetUserDataNodeType SET_USER_DATA = NodeType.REGISTRY
        .register("set_user_data", new SetUserDataNodeType());
    public static final ContainsUserDataNodeType CONTAINS_USER_DATA = NodeType.REGISTRY
        .register("contains_user_data", new ContainsUserDataNodeType());
    public static final RemoveUserDataNodeType REMOVE_USER_DATA = NodeType.REGISTRY
        .register("remove_user_data", new RemoveUserDataNodeType());
    public static final IncrementUserDataNodeType INCREMENT_USER_DATA = NodeType.REGISTRY
        .register("increment_user_data", new IncrementUserDataNodeType());
    public static final MultiplyUserDataNodeType MULTIPLY_USER_DATA = NodeType.REGISTRY
        .register("multiply_user_data", new MultiplyUserDataNodeType());

    @VisibleForTesting
    public static final Set<DataBox<?>> VALUE_PROVIDER_TYPES_WITH_DEFAULTS = Set.of(
        DataType.STRING.create(""),
        DataType.NUMBER.create(0.0),
        DataType.BOOLEAN.create(false),
        DataType.JSON_OBJECT.create(new JsonObject())
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
