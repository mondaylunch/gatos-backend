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
        NodeConnectorCodec inputCodec = new NodeConnectorCodec(this.registry, NodeConnector.Input.class);
        NodeConnectorCodec outputCodec = new NodeConnectorCodec(this.registry, NodeConnector.Output.class);
        NodeConnector.Input<Object> input = (NodeConnector.Input<Object>) decoderContext.decodeWithChildContext(inputCodec, reader);
        NodeConnector.Output<Object> output = (NodeConnector.Output<Object>) decoderContext.decodeWithChildContext(outputCodec, reader);
        return new NodeConnection<>(output, input);
    }

    @Override
    public void encode(BsonWriter writer, NodeConnection<?> value, EncoderContext encoderContext) {
        NodeConnectorCodec inputCodec = new NodeConnectorCodec(this.registry, NodeConnector.Input.class);
        NodeConnectorCodec outputCodec = new NodeConnectorCodec(this.registry, NodeConnector.Output.class);
        encoderContext.encodeWithChildContext(inputCodec, writer, value.to());
        encoderContext.encodeWithChildContext(outputCodec, writer, value.from());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<NodeConnection<?>> getEncoderClass() {
        return (Class<NodeConnection<?>>) (Object) NodeConnection.class;
    }
}
