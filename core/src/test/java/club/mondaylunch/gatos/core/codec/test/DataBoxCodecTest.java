package club.mondaylunch.gatos.core.codec.test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.mondaylunch.gatos.core.collection.BaseCollection;
import club.mondaylunch.gatos.core.data.DataBox;
import club.mondaylunch.gatos.core.data.DataType;
import club.mondaylunch.gatos.core.models.BaseModel;

public class DataBoxCodecTest {

    private static final BaseCollection<DataBoxContainer> CONTAINER_COLLECTION = new BaseCollection<>("dataBoxContainers", DataBoxContainer.class);
    private static final BaseCollection<DataBoxContainerContainer> CONTAINER_CONTAINER_COLLECTION = new BaseCollection<>("dataBoxContainerContainers", DataBoxContainerContainer.class);

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
    public void canInsertDataBoxContainer() {
        assertInsertDataBoxContainers(DataType.NUMBER.create(1.0));
    }

    @Test
    public void canInsertMultipleDataBoxContainers() {
        assertInsertDataBoxContainers(
            DataType.NUMBER.create(10.0),
            DataType.BOOLEAN.create(false),
            DataType.STRING.create("fhg;ksghuierhdkfglds"),
            DataType.BOOLEAN.optionalOf().create(Optional.empty()),
            DataType.BOOLEAN.optionalOf().create(Optional.of(true)),
            DataType.STRING.listOf().create(List.of("foo", "fhqwghads", "bar")),
            DataType.STRING.listOf().create(List.of())
        );
    }

    @Test
    public void canInsertDataBoxContainersWithDuplicates() {
        assertInsertDataBoxContainers(
            DataType.NUMBER.create(10.0),
            DataType.BOOLEAN.create(false),
            DataType.BOOLEAN.create(false),
            DataType.BOOLEAN.create(false),
            DataType.NUMBER.create(10.0)
        );
    }

    @Test
    public void canInsertNestedContainer() {
        UUID id = UUID.randomUUID();
        DataBox<?> dataBox = DataType.NUMBER.create(5.0);
        DataBoxContainer container = new DataBoxContainer(UUID.randomUUID(), dataBox);
        DataBoxContainerContainer containerContainer = new DataBoxContainerContainer(id, container);
        CONTAINER_CONTAINER_COLLECTION.insert(containerContainer);
        assertContainerContainerCount(1);
        DataBoxContainerContainer retrievedContainerContainer = CONTAINER_CONTAINER_COLLECTION.get(id);
        Assertions.assertNotNull(retrievedContainerContainer);
        DataBoxContainer retrievedContainer = retrievedContainerContainer.dataBoxContainer;
        Assertions.assertNotNull(retrievedContainer);
        Assertions.assertEquals(dataBox, retrievedContainer.dataBox);
    }

    private static UUID insertDataBoxContainer(DataBox<?> dataBox) {
        UUID id = UUID.randomUUID();
        DataBoxContainer container = new DataBoxContainer(id, dataBox);
        long countBefore = CONTAINER_COLLECTION.size();
        CONTAINER_COLLECTION.insert(container);
        assertContainerCount(countBefore + 1);
        return id;
    }

    private static void assertInsertDataBoxContainers(DataBox<?>... dataBoxs) {
        int inserted = 0;
        for (DataBox<?> dataBox : dataBoxs) {
            UUID id = insertDataBoxContainer(dataBox);
            inserted++;
            assertContainerCount(inserted);
            DataBoxContainer retrievedContainer = CONTAINER_COLLECTION.get(id);
            Assertions.assertNotNull(retrievedContainer);
            Assertions.assertEquals(dataBox, retrievedContainer.dataBox);
        }
    }

    private static void assertContainerCount(long expected) {
        Assertions.assertEquals(expected, CONTAINER_COLLECTION.size());
    }

    private static void assertContainerContainerCount(long expected) {
        Assertions.assertEquals(expected, CONTAINER_CONTAINER_COLLECTION.size());
    }

    public static class DataBoxContainer extends BaseModel {

        public DataBox<?> dataBox;

        public DataBoxContainer(UUID id, DataBox<?> dataBox) {
            super(id);
            this.dataBox = dataBox;
        }

        @SuppressWarnings("unused")
        public DataBoxContainer() {
        }
    }

    public static class DataBoxContainerContainer extends BaseModel {

        public DataBoxContainer dataBoxContainer;

        public DataBoxContainerContainer(UUID id, DataBoxContainer dataBoxContainer) {
            super(id);
            this.dataBoxContainer = dataBoxContainer;
        }

        @SuppressWarnings("unused")
        public DataBoxContainerContainer() {
        }
    }
}
