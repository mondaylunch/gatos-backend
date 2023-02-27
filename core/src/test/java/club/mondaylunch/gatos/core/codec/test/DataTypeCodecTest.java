package club.mondaylunch.gatos.core.codec.test;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.collection.BaseCollection;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.models.BaseModel;

public class DataTypeCodecTest {

    private static final BaseCollection<DataTypeContainer> CONTAINER_COLLECTION = new BaseCollection<>("dataTypeContainers", DataTypeContainer.class);
    private static final BaseCollection<DataTypeContainerContainer> CONTAINER_CONTAINER_COLLECTION = new BaseCollection<>("dataTypeContainerContainers", DataTypeContainerContainer.class);

    @BeforeEach
    void setUp() {
        this.reset();
        assertContainerCount(0);
        assertContainerContainerCount(0);
    }

    @AfterEach
    void tearDown() {
        this.reset();
    }

    private void reset() {
        CONTAINER_COLLECTION.clear();
        CONTAINER_CONTAINER_COLLECTION.clear();
    }

    @Test
    public void canInsertDataTypeContainer() {
        assertInsertDataTypeContainers(DataType.NUMBER);
    }

    @Test
    public void canInsertMultipleDataTypeContainers() {
        assertInsertDataTypeContainers(
            DataType.NUMBER,
            DataType.BOOLEAN,
            DataType.STRING
        );
    }

    @Test
    public void canInsertDataTypeContainersWithDuplicates() {
        assertInsertDataTypeContainers(
            DataType.NUMBER,
            DataType.BOOLEAN,
            DataType.STRING,
            DataType.NUMBER,
            DataType.BOOLEAN,
            DataType.STRING
        );
    }

    @Test
    public void canInsertNestedContainer() {
        UUID id = UUID.randomUUID();
        DataType<?> dataType = DataType.NUMBER;
        DataTypeContainer container = new DataTypeContainer(UUID.randomUUID(), dataType);
        DataTypeContainerContainer containerContainer = new DataTypeContainerContainer(id, container);
        CONTAINER_CONTAINER_COLLECTION.insert(containerContainer);
        assertContainerContainerCount(1);
        DataTypeContainerContainer retrievedContainerContainer = CONTAINER_CONTAINER_COLLECTION.get(id);
        Assertions.assertNotNull(retrievedContainerContainer);
        DataTypeContainer retrievedContainer = retrievedContainerContainer.dataTypeContainer;
        Assertions.assertNotNull(retrievedContainer);
        Assertions.assertSame(dataType, retrievedContainer.dataType);
    }

    private static UUID insertDataTypeContainer(DataType<?> dataType) {
        UUID id = UUID.randomUUID();
        DataTypeContainer container = new DataTypeContainer(id, dataType);
        long countBefore = CONTAINER_COLLECTION.size();
        CONTAINER_COLLECTION.insert(container);
        assertContainerCount(countBefore + 1);
        return id;
    }

    private static void assertInsertDataTypeContainers(DataType<?>... dataTypes) {
        int inserted = 0;
        for (DataType<?> dataType : dataTypes) {
            UUID id = insertDataTypeContainer(dataType);
            inserted++;
            assertContainerCount(inserted);
            DataTypeContainer retrievedContainer = CONTAINER_COLLECTION.get(id);
            Assertions.assertNotNull(retrievedContainer);
            Assertions.assertSame(dataType, retrievedContainer.dataType);
        }
    }

    private static void assertContainerCount(long expected) {
        Assertions.assertEquals(expected, CONTAINER_COLLECTION.size());
    }

    private static void assertContainerContainerCount(long expected) {
        Assertions.assertEquals(expected, CONTAINER_CONTAINER_COLLECTION.size());
    }

    public static class DataTypeContainer extends BaseModel {

        public DataType<?> dataType;

        public DataTypeContainer(UUID id, DataType<?> dataType) {
            super(id);
            this.dataType = dataType;
        }

        @SuppressWarnings("unused")
        public DataTypeContainer() {
        }
    }

    public static class DataTypeContainerContainer extends BaseModel {

        public DataTypeContainer dataTypeContainer;

        public DataTypeContainerContainer(UUID id, DataTypeContainer dataTypeContainer) {
            super(id);
            this.dataTypeContainer = dataTypeContainer;
        }

        @SuppressWarnings("unused")
        public DataTypeContainerContainer() {
        }
    }
}
