package club.mondaylunch.gatos.core.codec;

import java.util.function.Function;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class ByClassCodecProvider<T> implements CodecProvider {
    private final Class<? super T> wantedClass;
    private final Function<CodecRegistry, Codec<T>> codecFunc;

    public ByClassCodecProvider(Class<? super T> wantedClass, Function<CodecRegistry, Codec<T>> codecFunc) {
        this.wantedClass = wantedClass;
        this.codecFunc = codecFunc;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Codec<E> get(Class<E> clazz, CodecRegistry registry) {
        if (this.wantedClass.isAssignableFrom(clazz)) {
            return (Codec<E>) this.codecFunc.apply(registry);
        } else {
            return null;
        }
    }
}
