package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToIntFunction;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class StringInterpolationNodeType extends NodeType.Process {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern
            .compile("(?<!\\\\)(?:\\\\\\\\)*\\{(?<name>(?:[\\h\\w]*)?)}");

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
                "template", DataType.STRING.create("{}"));
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings) {
        String template = DataBox.get(settings, "template", DataType.STRING).orElse("");
        var matcher = PLACEHOLDER_PATTERN.matcher(template);
        var matches = matcher.results().toList();
        return matches.stream()
                .map(match -> new NodeConnector.Input<>(nodeId,
                        getNameForPlaceholder(m -> matches.indexOf(m) + 1, match),
                        DataType.STRING))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
                new NodeConnector.Output<>(nodeId, "result", DataType.STRING));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs,
            Map<String, DataBox<?>> settings) {
        String template = DataBox.get(settings, "template", DataType.STRING).orElse("");
        var matcher = PLACEHOLDER_PATTERN.matcher(template);
        AtomicInteger i = new AtomicInteger(0);
        String result = matcher.replaceAll(match -> {
            i.getAndIncrement();
            return Matcher.quoteReplacement(
                    DataBox.get(inputs, getNameForPlaceholder($ -> i.get(), match), DataType.STRING)
                            .orElse(""));
        });
        return Map.of(
                "result", CompletableFuture.completedFuture(DataType.STRING.create(result)));
    }

    private static String getNameForPlaceholder(ToIntFunction<MatchResult> placeholderIndexer,
            MatchResult placeholder) {
        String name = placeholder.group(1);
        return name.isBlank() ? "Placeholder " + (placeholderIndexer.applyAsInt(placeholder)) : name;
    }
}
