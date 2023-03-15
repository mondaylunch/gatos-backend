package club.mondaylunch.gatos.core.data;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import club.mondaylunch.gatos.core.Registry;

/**
 * Specialised Registry for DataTypes which generates derived types when requested.
 */
public class DataTypeRegistry extends Registry<DataType<?>> {
    protected DataTypeRegistry() {
        super(DataType.class);
    }

    @Override
    public Optional<DataType<?>> get(@NotNull String name) {
        return super.get(name).or(() -> {
            if (name.startsWith(OptionalDataType.PREFIX)) {
                return this.get(name.substring(OptionalDataType.PREFIX.length())).map(DataType::optionalOf);
            } else if (name.startsWith(ListDataType.PREFIX)) {
                return this.get(name.substring(ListDataType.PREFIX.length())).map(DataType::listOf);
            } else {
                return Optional.empty();
            }
        });
    }

    public Optional<DataType<?>> getWithoutGenerating(String name) {
        return super.get(name);
    }
}
