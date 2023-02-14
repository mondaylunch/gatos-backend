package club.mondaylunch.gatos.core.codec;

import java.util.NoSuchElementException;
import java.util.Objects;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.data.DataTypeRegistry;

public enum DataTypeCodec implements Codec<DataType<?>> {

    INSTANCE;

    @Override
    public DataType<?> decode(BsonReader reader, DecoderContext decoderContext) {
        String dataTypeName = reader.readString();
        Objects.requireNonNull(dataTypeName, "No data type name found");
        return DataTypeRegistry.get(dataTypeName)
            .orElseThrow(() -> new NoSuchElementException("No data type with name " + dataTypeName));
    }

    @Override
    public void encode(BsonWriter writer, DataType<?> value, EncoderContext encoderContext) {
        Objects.requireNonNull(value);
        writer.writeString(value.name());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<DataType<?>> getEncoderClass() {
        return (Class<DataType<?>>) (Object) DataType.class;
    }
}
