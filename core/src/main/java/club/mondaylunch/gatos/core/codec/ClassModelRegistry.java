package club.mondaylunch.gatos.core.codec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;

import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.graph.Graph;
import club.mondaylunch.gatos.core.graph.Node;
import club.mondaylunch.gatos.core.models.Flow;
import club.mondaylunch.gatos.core.models.User;

public class ClassModelRegistry {

    private static final Map<Class<?>, ClassModel<?>> registry = new HashMap<>();

    static {
        register(
            User.class,
            Flow.class,
            Graph.class,
            Node.class,
            DataBox.class
        );
    }

    /**
     * Registers class models.
     *
     * @param classes The classes.
     */
    public static void register(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            registry.computeIfAbsent(clazz, ClassModelRegistry::createClassModel);
        }
    }

    private static ClassModel<?> createClassModel(Class<?> clazz) {
        return ClassModel.builder(clazz)
            .conventions(List.of(
                Conventions.ANNOTATION_CONVENTION,
                Conventions.SET_PRIVATE_FIELDS_CONVENTION
            ))
            .build();
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
        return PojoCodecProvider.builder()
            .register(registry.values().toArray(ClassModel[]::new))
            .build();
    }
}
