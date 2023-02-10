package club.mondaylunch.gatos.core.codec;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import club.mondaylunch.gatos.core.graph.connector.NodeConnection;

public enum NodeConnectionCodecProvider implements CodecProvider {

    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (NodeConnection.class.isAssignableFrom(clazz)) {
            return (Codec<T>) new NodeConnectionCodec(registry);
        } else {
            return null;
        }
    }
}
