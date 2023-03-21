package club.mondaylunch.gatos.basicnodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.graph.type.NodeType;

/**
 * A node that operates on regular expressions.
 *
 * <p>
 * This is a {@link Node node} (addressable by UUID).
 * </p>
 * <p>
 * It takes a regular expression: "regex" and input word: "word".
 * </p>
 * <p>
 * It outputs:
 * "isMatch" a boolean to confirm if a match was found, if false all other output values are null.
 * "match" the first matching String.
 * "group" a List of all groups, if the regex has no groups this returns null.
 * </p>
 */
public class RegexNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "regex",  DataType.STRING),
            new NodeConnector.Input<>(nodeId, "word",   DataType.STRING)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "isMatch", DataType.BOOLEAN),
            new NodeConnector.Output<>(nodeId, "match", DataType.STRING),
            new NodeConnector.Output<>(nodeId, "group", DataType.STRING.listOf())
        );

        /*Matcher matcher = Pattern
            .compile(DataBox.get(settings, "regex", DataType.STRING).orElseThrow())
            .matcher(DataBox.get(settings, "word", DataType.STRING).orElseThrow());
        var matches = matcher.results().toList();
        
        Set<NodeConnector.Output<?>> dynamicGroupOut = matches.stream()
            .map(match -> new NodeConnector.Output<>(nodeId, getNameForPlaceholder(m -> matches.indexOf(m) + 1, match), DataType.STRING))
            .collect(Collectors.toSet());
        
        return new HashSet<NodeConnector.Output<?>>() {{
            new NodeConnector.Output<>(nodeId, "isMatch",   DataType.BOOLEAN);
            new NodeConnector.Output<>(nodeId, "match",     DataType.STRING);
            new NodeConnector.Output<>(nodeId, "group",     DataType.STRING.listOf());
            
            addAll(
                Pattern
                .compile(DataBox.get(settings, "regex", DataType.STRING).orElseThrow())
                .matcher(DataBox.get(settings, "word", DataType.STRING).orElseThrow()).results().toList().stream()
                .map(match -> new NodeConnector.Output<>(nodeId, getNameForPlaceholder(m -> matches.indexOf(m) + 1, match), DataType.STRING))
                .collect(Collectors.toSet())
            );
        }};*/
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {        
        var regex = Pattern.compile(DataBox.get(inputs, "regex", DataType.STRING).orElseThrow());        
        var word  = DataBox.get(inputs, "word",  DataType.STRING).orElseThrow();
        var matcher = regex.matcher(word);

        if(!matcher.find()) {
            return Map.of(
                "isMatch",  CompletableFuture.completedFuture(DataType.BOOLEAN.create(false)),
                "match",    CompletableFuture.completedFuture(DataType.STRING.create(null)),
                "groups",    CompletableFuture.completedFuture(DataType.STRING.listOf().create(null))
            );
        }

        return Map.of(
            "isMatch",  CompletableFuture.completedFuture(DataType.BOOLEAN.create(true)),
            "match",    CompletableFuture.completedFuture(DataType.STRING.create(matcher.group())),
            "groups",    CompletableFuture.completedFuture(DataType.STRING.listOf().create(getGroups(matcher)))
        );
    }

    /**
     * A function method to gather all the groups a regular expression
     * finds in an input word as a list 
     * @param matcher a Matcher given a word set to a regex Pattern
     * @return a List<String> of groups
     */
    public List<String> getGroups(Matcher matcher) {
        int groups = matcher.groupCount();
        if(groups == 0) return null;
        ArrayList<String> lst = new ArrayList<String>();
        for(int i = 1; i <= groups; i++) { lst.add(matcher.group(i)); }
        return lst;
    }

    /*private static String getNameForPlaceholder(ToIntFunction<MatchResult> placeholderIndexer, MatchResult placeholder) {
        try {
            String name = placeholder.group(1);
            return name.isBlank() ? "group " + (placeholderIndexer.applyAsInt(placeholder)) : name;
        } catch (Exception e) {
            return "";
        }
    }*/

    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        return this.compute(inputs, settings, Map.of());
    }

    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs) {
        return this.compute(inputs, Map.of(), Map.of());
    }
}
