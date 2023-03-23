package club.mondaylunch.gatos.basicnodes.end;

import java.util.UUID;

import club.mondaylunch.gatos.core.models.UserData;

public class IncrementUserDataNodeType extends UpdateUserDataNodeType {

    @Override
    protected void updateValue(UUID userId, String key, double value, boolean setIfAbsent) {
        if (setIfAbsent) {
            UserData.objects.incrementOrSet(userId, key, value);
        } else {
            UserData.objects.increment(userId, key, value);
        }
    }
}
