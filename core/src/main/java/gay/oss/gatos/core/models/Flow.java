package gay.oss.gatos.core.models;

import gay.oss.gatos.core.collections.FlowCollection;

public class Flow extends BaseModel {
    public static FlowCollection objects = new FlowCollection();

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
