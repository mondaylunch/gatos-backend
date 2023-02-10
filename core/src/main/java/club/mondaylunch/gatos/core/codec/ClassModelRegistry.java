package club.mondaylunch.gatos.core.codec;

import java.util.HashMap;
import java.util.List;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

public class ClassModelRegistry {

    private static final HashMap<Class<?>, ClassModel<?>> registry = new HashMap<>();

    /**
     * Registers class models.
     *
     * @param classes The classes.
     */
    public static void register(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            registry.computeIfAbsent(clazz, cls -> ClassModel.builder(cls)
                .conventions(List.of(Conventions.ANNOTATION_CONVENTION))
                .build()
            );
        }
    }

    /**
     * Creates a codec registry from the registered class models.
     *
     * @return The codec registry.
     */
    public static CodecRegistry createCodecRegistry() {
        return CodecRegistries.fromProviders(createCodecProvider());
    }

    /**
     * Creates a codec provider from the registered class models.
     *
     * @return The codec provider.
     */
    private static CodecProvider createCodecProvider() {
        return PojoCodecProvider.builder().register(registry.values().toArray(new ClassModel<?>[0])).build();
    }
}
