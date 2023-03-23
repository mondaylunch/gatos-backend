package club.mondaylunch.gatos.core.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public enum JsonElementCodec implements Codec<JsonElement> {

    INSTANCE;

    @Override
    public JsonElement decode(BsonReader reader, DecoderContext decoderContext) {
        var jsonString = reader.readString();
        return JsonParser.parseString(jsonString);
    }

    @Override
    public void encode(BsonWriter writer, JsonElement value, EncoderContext encoderContext) {
        var jsonString = value.toString();
        writer.writeString(jsonString);
    }

    @Override
    public Class<JsonElement> getEncoderClass() {
        return JsonElement.class;
    }
}
