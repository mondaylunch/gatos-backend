package club.mondaylunch.gatos.basicnodes.end;

import java.util.UUID;

import club.mondaylunch.gatos.core.models.FlowData;

public class IncrementFlowDataNodeType extends UpdateFlowDataNodeType {

    @Override
    protected void updateValue(UUID flowId, String key, double value, boolean setIfAbsent) {
        if (setIfAbsent) {
            FlowData.objects.incrementOrSet(flowId, key, value);
        } else {
            FlowData.objects.increment(flowId, key, value);
        }
    }
}
