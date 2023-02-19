package club.mondaylunch.gatos.core.codec;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import club.mondaylunch.gatos.core.graph.connector.NodeConnection;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;

/**
 * Needed to avoid errors with generics with the default codec.
 */
public class NodeConnectionCodec implements Codec<NodeConnection<?>> {

    private final CodecRegistry registry;

    public NodeConnectionCodec(CodecRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    @Override
    public NodeConnection<?> decode(BsonReader reader, DecoderContext decoderContext) {
        NodeConnectorCodec outputCodec = new NodeConnectorCodec(this.registry, NodeConnector.Output.class);
        NodeConnectorCodec inputCodec = new NodeConnectorCodec(this.registry, NodeConnector.Input.class);
        reader.readStartDocument();
        var output = (NodeConnector.Output<Object>) decoderContext.decodeWithChildContext(outputCodec, reader);
        var input = (NodeConnector.Input<Object>) decoderContext.decodeWithChildContext(inputCodec, reader);
        reader.readEndDocument();
        return new NodeConnection<>(output, input);
    }

    @Override
    public void encode(BsonWriter writer, NodeConnection<?> value, EncoderContext encoderContext) {
        NodeConnectorCodec outputCodec = new NodeConnectorCodec(this.registry, NodeConnector.Output.class);
        NodeConnectorCodec inputCodec = new NodeConnectorCodec(this.registry, NodeConnector.Input.class);
        writer.writeStartDocument();
        writer.writeName("output");
        encoderContext.encodeWithChildContext(outputCodec, writer, value.from());
        writer.writeName("input");
        encoderContext.encodeWithChildContext(inputCodec, writer, value.to());
        writer.writeEndDocument();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<NodeConnection<?>> getEncoderClass() {
        return (Class<NodeConnection<?>>) (Object) NodeConnection.class;
    }
}
