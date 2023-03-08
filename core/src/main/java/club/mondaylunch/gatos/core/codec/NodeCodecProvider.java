package club.mondaylunch.gatos.core.codec;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import club.mondaylunch.gatos.core.graph.Node;

public class NodeCodecProvider implements CodecProvider {
    /**
     * For the database, we want to store the node's inputtypes (we can't just work it out from other nodes when deserializing
     * as we don't have the graph context), and we don't want to store connectors (they can be created by the nodetype on node
     * object creation).
     */
    public static final NodeCodecProvider FOR_DB = new NodeCodecProvider(true);

    /**
     * For the API, we don't want to store the node's inputtypes (they aren't needed for the frontend), but we do want
     * to store connectors.
     */
    public static final NodeCodecProvider FOR_API = new NodeCodecProvider(false);
    private final boolean isForDb;

    public NodeCodecProvider(boolean isForDb) {
        this.isForDb = isForDb;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (Node.class.isAssignableFrom(clazz)) {
            return (Codec<T>) new Node.NodeCodec(registry, this.isForDb);
        } else {
            return null;
        }
    }
}
