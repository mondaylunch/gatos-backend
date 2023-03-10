package club.mondaylunch.gatos.core.codec;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import club.mondaylunch.gatos.core.graph.Graph;

public enum GraphCodecProvider implements CodecProvider {

    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (Graph.class.isAssignableFrom(clazz)) {
            return (Codec<T>) new Graph.GraphCodec(registry);
        } else {
            return null;
        }
    }
}
