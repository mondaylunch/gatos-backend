package gay.oss.gatos.core.models;

import gay.oss.gatos.core.collections.FlowCollection;

/**
 * POJO for flows
 */
public class Flow extends BaseModel {
    public static FlowCollection objects = new FlowCollection();

    /**
     * Display name
     */
    private String name;

    /**
     * Set the display name
     * 
     * @param name display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the display name
     * 
     * @return display name
     */
    public String getName() {
        return this.name;
    }
}
