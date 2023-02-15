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
public class NodeConnectorCodec<T extends NodeConnector<?>> implements Codec<T> {

    private final CodecRegistry registry;
    private final Class<? super T> type;

    public NodeConnectorCodec(CodecRegistry registry, Class<? super T> type) {
        this.registry = registry;
        this.type = type;
    }

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        Codec<UUID> uuidCodec = this.registry.get(UUID.class);
        reader.readStartDocument();
        UUID nodeId = decoderContext.decodeWithChildContext(uuidCodec, reader);
        String name = reader.readString();
        DataType<?> dataType = decoderContext.decodeWithChildContext(DataTypeCodec.INSTANCE, reader);
        reader.readEndDocument();
        return this.createNodeConnector(nodeId, name, dataType);
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        Codec<UUID> uuidCodec = this.registry.get(UUID.class);
        writer.writeStartDocument();
        writer.writeName("nodeId");
        encoderContext.encodeWithChildContext(uuidCodec, writer, value.nodeId());
        writer.writeName("name");
        writer.writeString(value.name());
        writer.writeName("type");
        encoderContext.encodeWithChildContext(DataTypeCodec.INSTANCE, writer, value.type());
        writer.writeEndDocument();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getEncoderClass() {
        return (Class<T>) this.type;
    }

    @SuppressWarnings("unchecked")
    private T createNodeConnector(UUID nodeId, String name, DataType<?> dataType) {
        if (this.type == NodeConnector.Input.class) {
            return (T) new NodeConnector.Input<>(nodeId, name, dataType);
        } else if (this.type == NodeConnector.Output.class) {
            return (T) new NodeConnector.Output<>(nodeId, name, dataType);
        } else {
            throw new IllegalArgumentException("Unknown node connector type: " + this.type);
        }
    }
}
