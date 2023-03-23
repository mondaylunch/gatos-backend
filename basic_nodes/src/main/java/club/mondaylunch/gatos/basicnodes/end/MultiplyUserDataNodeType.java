package club.mondaylunch.gatos.basicnodes.end;

import java.util.UUID;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.models.UserData;

public class MultiplyUserDataNodeType extends UpdateUserDataNodeType {

    @Override
    protected void updateValue(UUID userId, String key, double value, boolean setIfAbsent) {
        if (setIfAbsent) {
            UserData.objects.setIfAbsent(userId, key, DataType.NUMBER.create(1.0));
        }
        UserData.objects.multiply(userId, key, value);
    }
}
