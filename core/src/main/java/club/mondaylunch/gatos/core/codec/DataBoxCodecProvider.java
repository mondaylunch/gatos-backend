package club.mondaylunch.gatos.core.codec;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import club.mondaylunch.gatos.core.data.DataBox;

public enum DataBoxCodecProvider implements CodecProvider {

    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (DataBox.class.isAssignableFrom(clazz)) {
            return (Codec<T>) new DataBoxCodec(registry);
        } else {
            return null;
        }
    }
}
