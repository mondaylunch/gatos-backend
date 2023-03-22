package club.mondaylunch.gatos.basicnodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;

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
            new NodeConnector.Input<>(nodeId, "regex", DataType.STRING),
            new NodeConnector.Input<>(nodeId, "word", DataType.STRING)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "isMatch", DataType.BOOLEAN),
            new NodeConnector.Output<>(nodeId, "match", DataType.STRING),
            new NodeConnector.Output<>(nodeId, "group", DataType.STRING.listOf())
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {        
        var regex = Pattern.compile(DataBox.get(inputs, "regex", DataType.STRING).orElseThrow());        
        var word = DataBox.get(inputs, "word", DataType.STRING).orElseThrow();
        var matcher = regex.matcher(word);

        return Map.of(
            "isMatch", CompletableFuture.completedFuture(DataType.BOOLEAN.create(matcher.find())),
            "match", CompletableFuture.completedFuture(DataType.STRING.optionalOf().create(this.getMatch(matcher))),
            "groups", CompletableFuture.completedFuture(DataType.STRING.listOf().optionalOf().create(this.getGroups(matcher)))
        );
    }

    /**
     * A function method to acquire the matching text and return it as an optional
     * @param matcher a Matcher given a word set to a regex Pattern
     * @return an Optional of a String or an empty Optional if no match was found
     */
    public Optional<String> getMatch(Matcher matcher) {
        matcher.reset();
        if(!matcher.find()) return Optional.empty();
        return Optional.of(matcher.group());
    }

    /**
     * A function method to gather all the groups a regular expression finds in an input word as an optional list. 
     * @param matcher a Matcher given a word set to a regex Pattern
     * @return an Optional of a List of Strings or an empty Optional if no groups are found
     */
    public Optional<List<String>> getGroups(Matcher matcher) {
        int groups = matcher.groupCount();
        if (groups == 0) return Optional.empty();

        ArrayList<String> lst = new ArrayList<String>();
        for (int i = 1; i <= groups; i++) lst.add(matcher.group(i));
        
        return Optional.of(lst);
    }

    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        return this.compute(inputs, settings, Map.of());
    }

    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs) {
        return this.compute(inputs, Map.of(), Map.of());
    }
}
