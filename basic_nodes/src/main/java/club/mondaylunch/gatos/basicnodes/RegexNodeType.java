package club.mondaylunch.gatos.basicnodes;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class RegexNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of(
            "template", DataType.STRING.create("{}")
        );
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "regex",  DataType.STRING),
            new NodeConnector.Input<>(nodeId, "word",   DataType.STRING)
        );
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "isMatch",   DataType.BOOLEAN),
            new NodeConnector.Output<>(nodeId, "match",     DataType.STRING),
            new NodeConnector.Output<>(nodeId, "group",     DataType.STRING.listOf())
        );
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {        
        var regex = Pattern.compile(DataBox.get(inputs, "regex", DataType.STRING).orElseThrow());        
        var word  = DataBox.get(inputs, "word",  DataType.STRING).orElseThrow();
        var matcher = regex.matcher(word);

        return Map.of(
            "isMatch",  CompletableFuture.completedFuture(DataType.BOOLEAN.create(
                matcher.matches()
            )),
            "match",    CompletableFuture.completedFuture(DataType.STRING.create(
                matcher.group(0)
            )),
            "group",    CompletableFuture.completedFuture(DataType.STRING.listOf().create(
                getGroups(matcher)
            ))
        );
    }

    public ArrayList<String> getGroups(Matcher matcher) {
        int groups = matcher.groupCount();
        if(groups == 0) return null;
        ArrayList<String> lst = new ArrayList<String>();
        for(int i = 1; i <= groups; i++) { lst.add(matcher.group(i)); }
        return lst;
    }
}
