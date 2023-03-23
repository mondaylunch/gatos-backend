package club.mondaylunch.gatos.core.codec;

import com.google.gson.JsonElement;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public enum JsonElementCodecProvider implements CodecProvider {

    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (JsonElement.class.isAssignableFrom(clazz)) {
            return (Codec<T>) JsonElementCodec.INSTANCE;
        } else {
            return null;
        }
    }
}
