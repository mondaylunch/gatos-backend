package club.mondaylunch.gatos.core.codec;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import club.mondaylunch.gatos.core.graph.connector.NodeConnector;

public enum NodeConnectorCodecProvider implements CodecProvider {

    INSTANCE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (NodeConnector.class.isAssignableFrom(clazz)) {
            return (Codec<T>) new NodeConnectorCodec(registry, clazz);
        } else {
            return null;
        }
    }
}
