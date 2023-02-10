package club.mondaylunch.gatos.core.codec;

/**
 * Provides hooks for running code before serialization and after deserialization.
 */
public interface DatabaseSerializable {

    /**
     * Called before serialization.
     * Use this to prepare the object for serialization.
     */
    void beforeSerialization();

    /**
     * Called after deserialization.
     * Use this to finalize deserialization.
     */
    void afterDeserialization();
}
