package club.mondaylunch.gatos.core.codec;

import java.util.NoSuchElementException;
import java.util.Objects;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import club.mondaylunch.gatos.core.graph.type.NodeType;
import club.mondaylunch.gatos.core.graph.type.NodeTypeRegistry;

public enum NodeTypeCodec implements Codec<NodeType> {

    INSTANCE;

    @Override
    public NodeType decode(BsonReader reader, DecoderContext decoderContext) {
        String nodeTypeName = reader.readString();
        Objects.requireNonNull(nodeTypeName, "No node type name found");
        return NodeTypeRegistry.get(nodeTypeName)
            .orElseThrow(() -> new NoSuchElementException("No node type with name " + nodeTypeName));
    }

    @Override
    public void encode(BsonWriter writer, NodeType value, EncoderContext encoderContext) {
        Objects.requireNonNull(value);
        writer.writeString(value.name());
    }

    @Override
    public Class<NodeType> getEncoderClass() {
        return NodeType.class;
    }
}
