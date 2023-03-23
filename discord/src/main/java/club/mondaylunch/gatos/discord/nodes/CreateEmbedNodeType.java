package club.mondaylunch.gatos.discord.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.dv8tion.jda.api.EmbedBuilder;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.discord.DiscordDataTypes;
import club.mondaylunch.gatos.discord.GatosDiscord;

public class CreateEmbedNodeType extends NodeType.Process {
    public CreateEmbedNodeType(@SuppressWarnings("unused") GatosDiscord gatosDiscord) {
    }

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "fields", DataType.STRING.listOf().create(List.of())
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var regularInputs = Set.of(
            new NodeConnector.Input<>(nodeId, "title", DataType.STRING.optionalOf()),
            new NodeConnector.Input<>(nodeId, "description", DataType.STRING.optionalOf()),
            new NodeConnector.Input<>(nodeId, "image_url", DataType.STRING.optionalOf())
        );

        var fieldInputs = this.getFieldNames(DataBox.get(settings, "fields", DataType.STRING.listOf()).orElseThrow()).stream()
            .map(field -> new NodeConnector.Input<>(nodeId, field, DataType.STRING.optionalOf())).toList();

        Set<NodeConnector.Input<?>> inputs = new HashSet<>(regularInputs);
        inputs.addAll(regularInputs);
        inputs.addAll(fieldInputs);
        return inputs;
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "embed", DiscordDataTypes.MESSAGE_EMBED)
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(UUID userId, Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var embed = new EmbedBuilder();
        DataBox.get(inputs, "title", DataType.STRING.optionalOf()).flatMap(Function.identity())
            .ifPresent(embed::setTitle);
        DataBox.get(inputs, "description", DataType.STRING.optionalOf()).flatMap(Function.identity())
            .ifPresent(embed::setDescription);
        DataBox.get(inputs, "image_url", DataType.STRING.optionalOf()).flatMap(Function.identity())
            .ifPresent(embed::setImage);

        var fields = this.getFieldNames(DataBox.get(settings, "fields", DataType.STRING.listOf()).orElseThrow());
        for (String field : fields) {
            DataBox.get(inputs, field, DataType.STRING.optionalOf()).flatMap(Function.identity())
                .ifPresent(value -> embed.addField(field, value, false));
        }

        return Map.of(
            "embed", CompletableFuture.completedFuture(DiscordDataTypes.MESSAGE_EMBED.create(embed))
        );
    }

    private List<String> getFieldNames(List<String> fields) {
        List<String> filteredFields = new ArrayList<>();
        Map<String, Integer> fieldNumbers = new HashMap<>();
        for (String field : fields) {
            if (field.isBlank()) {
                continue;
            }

            field = field.trim();

            fieldNumbers.compute(field, (k, v) -> v == null ? 1 : v + 1);
            if (fieldNumbers.get(field) > 1) {
                field += " " + fieldNumbers.get(field);
            }

            filteredFields.add(field);
        }

        return filteredFields;
    }
}
