package gay.oss.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import gay.oss.gatos.core.data.DataBox;
import gay.oss.gatos.core.graph.connector.NodeConnector;
import gay.oss.gatos.core.graph.type.NodeType;

public class VariableRemappingNodeType extends NodeType.Process {
    @Override
    public Map<String, DataBox<?>> settings() {
        return null;
    }

    @Override
    public Set<NodeConnector.Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return null;
    }

    @Override
    public Set<NodeConnector.Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> state) {
        return null;
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings) {
        return null;
    }
}
