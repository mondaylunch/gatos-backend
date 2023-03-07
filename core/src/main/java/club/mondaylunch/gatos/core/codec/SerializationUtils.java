package club.mondaylunch.gatos.core.codec;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectionCodecProvider;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.MapCodecProvider;
import org.bson.codecs.Parameterizable;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.json.JsonReader;
import org.bson.json.JsonWriter;

import club.mondaylunch.gatos.core.Database;

/**
 * Holds utility methods for serialization.
 */
public final class SerializationUtils {

    private static final CodecRegistry JSON_CODEC_REGISTRY = createRegistry();

    /**
     * Reads a set from BSON.
     * @param reader    the BSON reader
     * @param context   the decoder context
     * @param clazz     the class of the contents of the set
     * @param registry  the codec registry
     * @param <T>       the type of the contents of the set
     * @return          a deserialized set
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> readSet(BsonReader reader, DecoderContext context, Class<? super T> clazz, CodecRegistry registry) {
        var genericCodec = (Parameterizable) new CollectionCodecProvider().get(Set.class, registry);
        var parameterizedCodec = (Codec<Set<T>>) genericCodec.parameterize(registry, List.of(clazz));
        return context.decodeWithChildContext(parameterizedCodec, reader);
    }

    /**
     * Reads a map from BSON. Note that BSON object keys are always strings, so you must provide a string-to-key function.
     * @param reader        the BSON reader
     * @param context       the decoder context
     * @param classV        the class of the values of the map
     * @param stringToKey   a function to convert strings into map keys
     * @param registry      the codec registry
     * @param <K>           the type of the keys of the map
     * @param <V>           the type of the values of the map
     * @return              a deserialized map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> readMap(BsonReader reader, DecoderContext context, Class<? super V> classV, Function<String, K> stringToKey, CodecRegistry registry) {
        var genericCodec = (Parameterizable) new MapCodecProvider().get(Map.class, registry);
        var parameterizedCodec = (Codec<Map<String, V>>) genericCodec.parameterize(registry, List.of(String.class, classV));
        Map<String, V> stringToValueMap = context.decodeWithChildContext(parameterizedCodec, reader);
        Map<K, V> res = new HashMap<>();
        for (var entry : stringToValueMap.entrySet()) {
            res.put(stringToKey.apply(entry.getKey()), entry.getValue());
        }
        return res;
    }

    /**
     * Writes a set to BSON.
     * @param writer    the BSON writer
     * @param context   the encoder context
     * @param clazz     the class of the contents of the set
     * @param registry  the codec registry
     * @param set       the set
     * @param <T>       the type of the contents of the set
     */
    @SuppressWarnings("unchecked")
    public static <T> void writeSet(BsonWriter writer, EncoderContext context, Class<? super T> clazz, CodecRegistry registry, Set<T> set) {
        var genericCodec = (Parameterizable) new CollectionCodecProvider().get(Set.class, registry);
        var parameterizedCodec = (Codec<Set<T>>) genericCodec.parameterize(registry, List.of(clazz));
        context.encodeWithChildContext(parameterizedCodec, writer, set);
    }

    /**
     * Writes a map to BSON. Note that BSON object keys are always strings, so you must provide a key-to-string function.
     * @param writer        the BSON writer
     * @param context       the encoder context
     * @param classV        the class of the values of the map
     * @param keyToString   a function to convert map keys into strings
     * @param registry      the codec registry
     * @param map           the map
     * @param <K>           the type of the keys of the map
     * @param <V>           the type of the values of the map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> void writeMap(BsonWriter writer, EncoderContext context, Class<? super V> classV, Function<K, String> keyToString, CodecRegistry registry, Map<K, V> map) {
        var genericCodec = (Parameterizable) new MapCodecProvider().get(Map.class, registry);
        var parameterizedCodec = (Codec<Map<String, V>>) genericCodec.parameterize(registry, List.of(String.class, classV));
        Map<String, V> toWrite = new HashMap<>();
        for (var entry : map.entrySet()) {
            toWrite.put(keyToString.apply(entry.getKey()), entry.getValue());
        }
        context.encodeWithChildContext(parameterizedCodec, writer, toWrite);
    }

    /**
     * Writes the start & end points of a BSON document and runs the provided runnable between them.
     * @param writer    the BSON writer
     * @param function  the function to run
     */
    public static void writeDocument(BsonWriter writer, Runnable function) {
        writer.writeStartDocument();
        function.run();
        writer.writeEndDocument();
    }

    /**
     * Reads the start & end points of a BSON document and runs the provided runnable between them.
     * @param reader    the BSON reader
     * @param function  the function to run
     */
    @SuppressWarnings("unused")
    public static void readDocument(BsonReader reader, Runnable function) {
        reader.readStartDocument();
        function.run();
        reader.readEndDocument();
    }

    /**
     * Reads the start & end points of a BSON document and runs the provided function between them.
     * @param reader    the BSON reader
     * @param function  the function to run
     * @return          the result of the function
     */
    public static <T> T readDocument(BsonReader reader, Supplier<T> function) {
        reader.readStartDocument();
        var res = function.get();
        reader.readEndDocument();
        return res;
    }

    /**
     * Serializes an object to JSON.
     *
     * @param object The object to serialize
     * @return The JSON representation of the object
     */
    public static String toJson(Object object) {
        var stringWriter = new StringWriter();
        try (var jsonWriter = new JsonWriter(stringWriter)) {
            @SuppressWarnings("unchecked")
            var codec = (Codec<Object>) JSON_CODEC_REGISTRY.get(object.getClass());
            var context = EncoderContext.builder().build();
            codec.encode(jsonWriter, object, context);
            return stringWriter.toString();
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try (var reader = new JsonReader(json)) {
            var codec = JSON_CODEC_REGISTRY.get(clazz);
            var context = DecoderContext.builder().build();
            return codec.decode(reader, context);
        }
    }

    public static <K, V> Map<K, V> readMap(String json, Function<String, K> stringToKey, Class<V> valueType) {
        try (var jsonReader = new JsonReader(json)) {
            var context = DecoderContext.builder().build();
            return readMap(jsonReader, context, valueType, stringToKey, JSON_CODEC_REGISTRY);
        }
    }

    private static CodecRegistry createRegistry() {
        return CodecRegistries.fromRegistries(
            CodecRegistries.fromCodecs(UuidStringCodec.INSTANCE),
            CodecRegistries.fromProviders(NodeCodecProvider.FOR_API),
            Database.getCodecRegistry()
        );
    }

    private SerializationUtils() {}
}
