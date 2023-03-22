package club.mondaylunch.gatos.basicnodes.end;

import java.util.UUID;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.models.FlowData;

public class MultiplyFlowDataNodeType extends UpdateFlowDataNodeType {

    @Override
    protected void updateValue(UUID flowId, String key, double value, boolean setIfAbsent) {
        if (setIfAbsent) {
            FlowData.objects.setIfAbsent(flowId, key, DataType.NUMBER.create(1.0));
        }
        FlowData.objects.multiply(flowId, key, value);
    }
}
