package club.mondaylunch.gatos.core.codec;

import java.util.List;
import java.util.Optional;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectionCodecProvider;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.Parameterizable;
import org.bson.codecs.configuration.CodecRegistry;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.ListDataType;
import club.mondaylunch.gatos.core.data.OptionalDataType;

public class DataBoxCodec implements Codec<DataBox<?>> {
    private final CodecRegistry registry;

    public DataBoxCodec(CodecRegistry registry) {
        this.registry = registry;
    }

    @Override
    public DataBox<?> decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        reader.readName("type");
        DataType<?> type = decoderContext.decodeWithChildContext(this.registry.get(DataType.class), reader);
        reader.readName("value");
        return this.decodeValue(reader, type, decoderContext);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encode(BsonWriter writer, DataBox<?> value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeName("type");
        encoderContext.encodeWithChildContext((Codec<DataType<?>>) this.registry.get(value.type().getClass()), writer, value.type());
        writer.writeName("value");
        this.encodeValue(writer, value, encoderContext);
        writer.writeEndDocument();
    }

    @SuppressWarnings("unchecked")
    private void encodeValue(BsonWriter writer, DataBox<?> box, EncoderContext encoderContext) {
        DataType<?> type = box.type();
        Encoder<?> encoder;
        Object toEncode;
        if (type instanceof ListDataType<?> listType) {
            encoder = ((Parameterizable) new CollectionCodecProvider().get(listType.clazz(), this.registry))
                .parameterize(this.registry, List.of(listType.contains().clazz()));
            toEncode = box.value();
        } else if (type instanceof OptionalDataType<?> optType) {
            encoder = this.registry.get(optType.contains().clazz());
            toEncode = ((Optional<?>) box.value()).orElse(null);
        } else {
            encoder = this.registry.get(type.clazz());
            toEncode = box.value();
        }
        encoderContext.encodeWithChildContext((Encoder<Object>) encoder, writer, toEncode);
    }

    @SuppressWarnings("unchecked")
    private <T> DataBox<?> decodeValue(BsonReader reader, DataType<?> type, DecoderContext decoderContext) {
        if (type instanceof ListDataType<?> listType) {
            var codec = ((Parameterizable) new CollectionCodecProvider().get(listType.clazz(), this.registry))
                .parameterize(this.registry, List.of(listType.contains().clazz()));
            return ((DataType<T>) type).create((T) decoderContext.decodeWithChildContext(codec, reader));
        } else if (type instanceof OptionalDataType<?> optType) {
            var codec = this.registry.get(optType.contains().clazz());
            return ((DataType<Optional<T>>) type).create(Optional.ofNullable((T) decoderContext.decodeWithChildContext(codec, reader)));
        } else {
            var codec = this.registry.get(type.clazz());
            return ((DataType<T>) type).create((T) decoderContext.decodeWithChildContext(codec, reader));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<DataBox<?>> getEncoderClass() {
        return (Class<DataBox<?>>) (Object) DataBox.class;
    }
}
