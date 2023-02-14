package club.mondaylunch.gatos.core.codec;

import club.mondaylunch.gatos.core.graph.Node;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public enum NodeCodecProvider implements CodecProvider {

    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (Node.class.isAssignableFrom(clazz)) {
            return (Codec<T>) new Node.NodeCodec(registry);
        } else {
            return null;
        }
    }
}
