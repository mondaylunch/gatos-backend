package club.mondaylunch.gatos.basicnodes.process;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import club.mondaylunch.gatos.core.Either;
import club.mondaylunch.gatos.core.GatosUtils;
import club.mondaylunch.gatos.core.data.Conversions;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.GraphValidityError;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.models.Flow;
import club.mondaylunch.gatos.core.models.UserData;

public class GetUserDataNodeType extends NodeType.Process {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "key", DataType.STRING.create(""),
            "type", DataType.DATA_TYPE.create(DataType.ANY)
        );
    }

    @Override
    public Collection<GraphValidityError> isValid(Node node, Either<Flow, Graph> flowOrGraph) {
        return GatosUtils.union(
            GraphValidityError.ensureSetting(node, "key", DataType.STRING, key -> key.isBlank() ? "Key cannot be blank" : null),
            super.isValid(node, flowOrGraph));
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var key = DataBox.get(settings, "key", DataType.STRING).orElse("");
        if (key.isBlank()) {
            return Set.of(new NodeConnector.Input<>(nodeId, "key", DataType.STRING));
        } else {
            return Set.of();
        }
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var type = DataBox.get(settings, "type", DataType.DATA_TYPE)
            .orElse(DataType.ANY)
            .optionalOf();
        return Set.of(new NodeConnector.Output<>(nodeId, "value", type));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var key = DataBox.get(settings, inputs, "key", DataType.STRING, Predicate.not(String::isBlank)).orElseThrow();
        var type = DataBox.get(settings, "type", DataType.DATA_TYPE).orElse(DataType.ANY);
        @SuppressWarnings("unchecked")
        var optionalType = (DataType<Optional<?>>) (DataType<?>) type.optionalOf();
        return Map.of(
            "value", CompletableFuture.supplyAsync(() -> {
                var valueOptional = UserData.objects.get(userId, key)
                    .filter(dataBox -> Conversions.canConvert(dataBox.type(), type))
                    .map(dataBox -> Conversions.convert(dataBox, type).value());
                return optionalType.create(valueOptional);
            })
        );
    }
}
