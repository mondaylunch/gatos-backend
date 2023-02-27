package club.mondaylunch.gatos.core.codec;

import java.util.UUID;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * Codec for UUIDs stored as strings.
 */
public enum UuidStringCodec implements Codec<UUID> {

    INSTANCE;

    @Override
    public UUID decode(BsonReader reader, DecoderContext decoderContext) {
        return UUID.fromString(reader.readString());
    }

    @Override
    public void encode(BsonWriter writer, UUID value, EncoderContext encoderContext) {
        writer.writeString(value.toString());
    }

    @Override
    public Class<UUID> getEncoderClass() {
        return UUID.class;
    }
}
