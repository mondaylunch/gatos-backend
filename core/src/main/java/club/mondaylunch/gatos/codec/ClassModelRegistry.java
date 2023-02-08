package club.mondaylunch.gatos.codec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

public class ClassModelRegistry {

    private static final Set<ClassModel<?>> registry = new HashSet<>();

    /**
     * Registers class models.
     *
     * @param classes The classes.
     */
    public static void register(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            ClassModel<?> model = ClassModel.builder(clazz)
                .conventions(List.of(Conventions.ANNOTATION_CONVENTION))
                .build();
            registry.add(model);
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
        return PojoCodecProvider.builder().register(registry.toArray(new ClassModel<?>[0])).build();
    }
}
