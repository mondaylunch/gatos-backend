package club.mondaylunch.gatos.basicnodes;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Input;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector.Output;
import club.mondaylunch.gatos.core.graph.type.NodeType;

public class ToStringNodeType extends NodeType.Process {

    @Override
    public Map<String, DataBox<?>> settings() {
        return Map.of();
    }

    @Override
    public Set<Input<?>> inputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Input<>(nodeId, "data", DataType.ANY)
        );
    }

    @Override
    public Set<Output<?>> outputs(UUID nodeId, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        return Set.of(
            new NodeConnector.Output<>(nodeId, "output", DataType.STRING));
    }

    @Override
    public Map<String, CompletableFuture<DataBox<?>>> compute(Map<String, DataBox<?>> inputs, Map<String, DataBox<?>> settings, Map<String, DataType<?>> inputTypes) {
        var input = DataBox.get(inputs, "data", DataType.ANY).orElse("");
        String result = this.canNotAutoCast(input) ? this.castToString(input) : input.toString();
        return Map.of("output", CompletableFuture.completedFuture(DataType.STRING.create(result)));
    }

    /*
    * the main string we can get would be of this form
    * DataBox[value=[1, 2], type=DataType[name=list]]
    * or 
    * DataBox[value=Optional[d], type=DataType[name=any]]
    * so if we start from index 14 all the way to the first occurence of ] this will result in the string 
    * of a list but not of an optional so more work needs to be done in that case.  
    */
    private String castToString(Object input) {
        String temp = input.toString(); // this will result in one of the strings above
        temp = temp.substring(14, temp.length());
        if (temp.startsWith("Optional")) {
            // check for empty situation
            if (temp.startsWith("Optional.empty")) {
                return "";
            }
            // then get the first occurence of both [ and ] respectively
            int openBracket = temp.indexOf("[");
            int closeBracket = temp.indexOf("]");
            temp = temp.substring(openBracket+1, closeBracket);
        } else {
            int firstOccurence = temp.indexOf("]");
            temp = temp.substring(0, firstOccurence+1);
        }
        return temp;
    }

    private boolean canNotAutoCast(Object input) {
        return input instanceof DataBox;
    }
}
