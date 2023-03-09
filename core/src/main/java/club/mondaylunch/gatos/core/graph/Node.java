package club.mondaylunch.gatos.core.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.jetbrains.annotations.Unmodifiable;

import club.mondaylunch.gatos.core.codec.SerializationUtils;
import club.mondaylunch.gatos.core.data.Conversions;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.graph.connector.NodeConnector;
import club.mondaylunch.gatos.core.graph.type.NodeType;

/**
 * Represents a node in a flow graph.
 * <p>
 * Note that this class is entirely immutable - to modify a node in a graph,
 * replace it with a new version.
 * This can be done with {@link Graph#modifyNode(UUID, UnaryOperator)}.
 * </p>
 */
public final class Node {

    private final UUID id;
    private final NodeType type;
    private final @Unmodifiable Map<String, DataBox<?>> settings;
    private final @Unmodifiable Map<String, NodeConnector.Input<?>> inputs;
    private final @Unmodifiable Map<String, NodeConnector.Output<?>> outputs;
    private final @Unmodifiable Map<String, DataType<?>> inputTypes;

    private Node(
        UUID id,
        NodeType type,
        Map<String, DataBox<?>> settings,
        Set<NodeConnector.Input<?>> inputs,
        Set<NodeConnector.Output<?>> outputs,
        Map<String, DataType<?>> inputTypes) {
        this.id = id;
        this.type = type;
        this.settings = Map.copyOf(settings);
        this.inputs = Map.copyOf(inputs.stream().collect(Collectors.toMap(NodeConnector::name, Function.identity())));
        this.outputs = Map.copyOf(outputs.stream().collect(Collectors.toMap(NodeConnector::name, Function.identity())));
        this.inputTypes = Map.copyOf(inputTypes);
    }

    /**
     * Create a new node with a given node type.
     *
     * @param type the node type
     * @return the new node
     */
    public static Node create(NodeType type) {
        var defaultSettings = type.settings();
        var id = UUID.randomUUID();
        return new Node(
            id,
            type,
            defaultSettings,
            NodeType.inputsOrEmpty(type, id, defaultSettings, Map.of()),
            NodeType.outputsOrEmpty(type, id, defaultSettings, Map.of()),
            Map.of());
    }

    /**
     * Create a new node, the same as this one, but with a modified setting.
     *
     * @param settingKey the key of the setting to modify
     * @param value      the new value of the setting
     * @param <T>        the type of the setting to modify
     * @return the new node
     */
    public <T> Node modifySetting(String settingKey, DataBox<T> value) {
        var setting = this.settings.get(settingKey);
        if (setting == null) {
            throw new IllegalArgumentException("Node contains no such setting " + settingKey);
        }

        if (!setting.type().equals(value.type())) {
            throw new IllegalArgumentException("Setting " + settingKey + " is not of type " + value.type().name());
        }

        var newSettings = new HashMap<>(this.settings);
        newSettings.put(settingKey, value);
        var newInputs = NodeType.inputsOrEmpty(this.type, this.id, newSettings, this.inputTypes);
        var newInputTypes = filterValidInputTypes(this.inputTypes, newInputs.stream().collect(Collectors.toMap(NodeConnector::name, Function.identity())));
        return new Node(
            this.id,
            this.type,
            newSettings,
            newInputs,
            NodeType.outputsOrEmpty(this.type, this.id, newSettings, newInputTypes),
            newInputTypes);
    }

    /**
     * Create a new node, the same as this one, but with possibly-changed outputs due to different input types.
     *
     * @param newInputTypes the canonical types of each input connection
     * @return the new node
     */
    public Node updateInputTypes(Map<String, DataType<?>> newInputTypes) {
        var newInputs = NodeType.inputsOrEmpty(this.type, this.id, this.settings, newInputTypes);
        var filteredInputTypes = filterValidInputTypes(newInputTypes, newInputs.stream().collect(Collectors.toMap(NodeConnector::name, Function.identity())));
        return new Node(
            this.id,
            this.type,
            this.settings,
            newInputs,
            NodeType.outputsOrEmpty(this.type, this.id, this.settings, filteredInputTypes),
            newInputTypes);
    }

    /**
     * Returns the UUID of this node.
     *
     * @return the UUID of this node
     */
    public UUID id() {
        return this.id;
    }

    /**
     * Returns the type of this node.
     *
     * @return the type of this node
     */
    public NodeType type() {
        return this.type;
    }

    /**
     * Returns the settings of this node.
     *
     * @return the settings of this node
     */
    public @Unmodifiable Map<String, DataBox<?>> settings() {
        return this.settings;
    }

    /**
     * Retrieve a setting for a given key.
     *
     * @param key  the setting key
     * @param type the datatype of the setting
     * @param <T>  the type of the setting
     * @return the setting
     * @throws IllegalArgumentException if the node does not contain a setting with
     *                                  the given key
     * @throws IllegalArgumentException if the setting with the given key does not
     *                                  hold data of the expected type
     */
    public <T> DataBox<T> getSetting(String key, DataType<T> type) {
        var setting = this.settings.get(key);
        if (setting == null) {
            throw new IllegalArgumentException("Node contains no such setting " + key);
        }

        if (!setting.type().equals(type)) {
            throw new IllegalArgumentException("Setting " + key + " is not of type " + type.name());
        }

        // We know this cast succeeds because of the check above
        // noinspection unchecked
        return (DataBox<T>) setting;
    }

    /**
     * Returns the inputs of this node.
     *
     * @return the inputs of this node
     */
    public @Unmodifiable Map<String, NodeConnector.Input<?>> inputs() {
        return this.inputs;
    }

    /**
     * Possibly retrieves an input connector with the given name.
     *
     * @param name the name of the connector
     * @return an Optional of the connector
     */
    public Optional<NodeConnector.Input<?>> getInputWithName(String name) {
        return Optional.ofNullable(this.inputs.get(name));
    }

    /**
     * Returns the outputs of this node.
     *
     * @return the outputs of this node
     */
    public @Unmodifiable Map<String, NodeConnector.Output<?>> getOutputs() {
        return this.outputs;
    }

    /**
     * Possibly retrieves an output connector with the given name.
     *
     * @param name the name of the connector
     * @return an Optional of the connector
     */
    public Optional<NodeConnector.Output<?>> getOutputWithName(String name) {
        return Optional.ofNullable(this.outputs.get(name));
    }

    /**
     * Returns the types on the other end of the connections of each input connector.
     * @return the input datatypes
     */
    public Map<String, DataType<?>> inputTypes() {
        return this.inputTypes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        var that = (Node) obj;
        return Objects.equals(this.id, that.id)
            && Objects.equals(this.type, that.type)
            && Objects.equals(this.settings, that.settings)
            && Objects.equals(this.inputs, that.inputs)
            && Objects.equals(this.outputs, that.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.type, this.settings, this.inputs, this.outputs);
    }

    @Override
    public String toString() {
        return "Node[id=%s, type=%s, settings=%s, inputs=%s, outputs=%s, inputTypes=%s]".formatted(
            this.id,
            this.type,
            this.settings,
            this.inputs,
            this.outputs,
            this.inputTypes);
    }

    /**
     * Filters out map entries where the datatype value is not convertable to the type of the input connector specified by the key.
     * @param oldInputTypes the input types to filter
     * @param newInputs     the inputs of the node
     * @return              a filtered input type map
     */
    private static Map<String, DataType<?>> filterValidInputTypes(Map<String, DataType<?>> oldInputTypes, Map<String, NodeConnector.Input<?>> newInputs) {
        return oldInputTypes.entrySet().stream()
            .filter(kv -> newInputs.containsKey(kv.getKey()))
            .filter(kv -> Conversions.canConvert(kv.getValue(), newInputs.get(kv.getKey()).type()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static final class NodeCodec implements Codec<Node> {
        private final CodecRegistry registry;
        private final boolean isForDb;

        public NodeCodec(CodecRegistry registry, boolean isForDb) {
            this.registry = registry;
            this.isForDb = isForDb;
        }

        @Override
        public Node decode(BsonReader reader, DecoderContext decoderContext) {
            return SerializationUtils.readDocument(reader, () -> {
                reader.readName("id");
                UUID id = decoderContext.decodeWithChildContext(this.registry.get(UUID.class), reader);
                reader.readName("type");
                NodeType type = decoderContext.decodeWithChildContext(this.registry.get(NodeType.class), reader);
                reader.readName("settings");
                Map<String, DataBox<?>> settings = SerializationUtils.readMap(reader, decoderContext, DataBox.class, Function.identity(), this.registry);
                if (this.isForDb) {
                    reader.readName("input_types");
                    Map<String, DataType<?>> inputTypes = SerializationUtils.readMap(reader, decoderContext, DataType.class, Function.identity(), this.registry);
                    var inputs = NodeType.inputsOrEmpty(type, id, settings, inputTypes);
                    return new Node(
                        id,
                        type,
                        settings,
                        inputs,
                        NodeType.outputsOrEmpty(type, id, settings, Map.of()),
                        filterValidInputTypes(inputTypes, inputs.stream().collect(Collectors.toMap(NodeConnector::name, Function.identity())))
                    );
                } else {
                    reader.readName("inputs");
                    Map<String, NodeConnector.Input<?>> inputs = SerializationUtils.readMap(reader, decoderContext, NodeConnector.Input.class, Function.identity(), this.registry);
                    reader.readName("outputs");
                    Map<String, NodeConnector.Output<?>> outputs = SerializationUtils.readMap(reader, decoderContext, NodeConnector.Output.class, Function.identity(), this.registry);
                    return new Node(
                        id,
                        type,
                        settings,
                        Set.copyOf(inputs.values()),
                        Set.copyOf(outputs.values()),
                        filterValidInputTypes(Map.of(), inputs)
                    );
                }
            });
        }

        @Override
        public void encode(BsonWriter writer, Node value, EncoderContext encoderContext) {
            SerializationUtils.writeDocument(writer, () -> {
                writer.writeName("id");
                encoderContext.encodeWithChildContext(this.registry.get(UUID.class), writer, value.id);
                writer.writeName("type");
                encoderContext.encodeWithChildContext(this.registry.get(NodeType.class), writer, value.type);
                writer.writeName("settings");
                SerializationUtils.writeMap(writer, encoderContext, DataBox.class, Function.identity(), this.registry, value.settings);
                if (this.isForDb) {
                    writer.writeName("input_types");
                    SerializationUtils.writeMap(writer, encoderContext, DataType.class, Function.identity(), this.registry, value.inputTypes);
                } else {
                    writer.writeName("inputs");
                    SerializationUtils.writeMap(writer, encoderContext, NodeConnector.Input.class, Function.identity(), this.registry, value.inputs);
                    writer.writeName("outputs");
                    SerializationUtils.writeMap(writer, encoderContext, NodeConnector.Output.class, Function.identity(), this.registry, value.outputs);
                }
            });
        }

        @Override
        public Class<Node> getEncoderClass() {
            return Node.class;
        }
    }
}
