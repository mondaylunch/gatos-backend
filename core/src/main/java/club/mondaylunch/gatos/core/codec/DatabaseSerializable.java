package club.mondaylunch.gatos.core.codec;

public interface DatabaseSerializable {

    void beforeSerialization();

    void afterDeserialization();
}
