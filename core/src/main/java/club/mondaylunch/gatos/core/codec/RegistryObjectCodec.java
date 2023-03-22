package club.mondaylunch.gatos.core.codec;

import java.util.NoSuchElementException;
import java.util.Objects;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import club.mondaylunch.gatos.core.Registry;

public class RegistryObjectCodec<T> implements Codec<T> {
    private final Registry<T> registry;

    public RegistryObjectCodec(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public T decode(BsonReader reader, DecoderContext decoderContext) {
        String nodeTypeName = reader.readString();
        Objects.requireNonNull(nodeTypeName, "No registry object name found");
        return this.registry.get(nodeTypeName)
            .orElseThrow(() -> new NoSuchElementException("No object in registry %s with name %s".formatted(Registry.REGISTRIES.getName(this.registry), nodeTypeName)));
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        Objects.requireNonNull(value);
        writer.writeString(this.registry.getName(value)
            .orElseThrow(() -> new NullPointerException("Object %s is not registered in registry %s".formatted(value, Registry.REGISTRIES.getName(this.registry)))));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getEncoderClass() {
        return (Class<T>) this.registry.getClazz();
    }

    public enum Provider implements CodecProvider {
        INSTANCE;

        @Override
        @SuppressWarnings("unchecked")
        public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
            return (Codec<T>) Registry.getRegistryByClass(clazz).map(RegistryObjectCodec::new).orElse(null);
        }
    }
}
