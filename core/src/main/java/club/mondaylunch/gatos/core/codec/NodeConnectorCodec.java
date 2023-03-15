package club.mondaylunch.gatos.core.codec;

import java.util.UUID;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;

/**
 * Needed to avoid errors with generics with the default codec.
 */
public class NodeConnectorCodec implements Codec<NodeConnector<?>> {

    private final CodecRegistry registry;
    private final Class<?> type;

    public NodeConnectorCodec(CodecRegistry registry, Class<?> type) {
        this.registry = registry;
        this.type = type;
    }

    @Override
    public NodeConnector<?> decode(BsonReader reader, DecoderContext decoderContext) {
        Codec<UUID> uuidCodec = this.registry.get(UUID.class);
        return SerializationUtils.readDocument(reader, () -> {
            UUID nodeId = decoderContext.decodeWithChildContext(uuidCodec, reader);
            String name = reader.readString();
            DataType<?> dataType = decoderContext.decodeWithChildContext(this.registry.get(DataType.class), reader);
            return this.createNodeConnector(nodeId, name, dataType);
        });
    }

    @Override
    public void encode(BsonWriter writer, NodeConnector<?> value, EncoderContext encoderContext) {
        Codec<UUID> uuidCodec = this.registry.get(UUID.class);
        SerializationUtils.writeDocument(writer, () -> {
            writer.writeName("node_id");
            encoderContext.encodeWithChildContext(uuidCodec, writer, value.nodeId());
            writer.writeName("name");
            writer.writeString(value.name());
            writer.writeName("type");
            encoderContext.encodeWithChildContext(this.registry.get(DataType.class), writer, value.type());
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<NodeConnector<?>> getEncoderClass() {
        return (Class<NodeConnector<?>>) (Object) NodeConnector.class;
    }

    private NodeConnector<?> createNodeConnector(UUID nodeId, String name, DataType<?> dataType) {
        if (this.type == NodeConnector.Input.class) {
            return new NodeConnector.Input<>(nodeId, name, dataType);
        } else if (this.type == NodeConnector.Output.class) {
            return new NodeConnector.Output<>(nodeId, name, dataType);
        } else {
            throw new IllegalArgumentException("Unknown node connector type: " + this.type);
        }
    }
}
