package id.glitchkid.kotobakeep;

import id.glitchkid.kotobakeep.server.CollectionProgress;
import id.glitchkid.kotobakeep.server.LearningProgress;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CollectionProgressTest {
    private final UUID player = UUID.randomUUID();
    @Test void collectionAndCorrectAnswersAreIndependentAndPersistPerPlayer() {
        var collection = new CollectionProgress();
        assertTrue(collection.collect(player, "minecraft:dirt"));
        assertFalse(collection.collect(player, "minecraft:dirt"));
        assertTrue(collection.correct(player, "minecraft:stone"));
        var restored = CollectionProgress.fromJson(collection.toJson());
        assertTrue(restored.collected(player).contains("minecraft:dirt"));
        assertFalse(restored.correct(player).contains("minecraft:dirt"));
        assertTrue(restored.correct(player).contains("minecraft:stone"));
        assertTrue(restored.collected(UUID.randomUUID()).isEmpty());
    }
    @Test void importingOldHistorySeparatesFailedWordsAndDoesNotEraseMilestones() {
        var history = new LearningProgress();
        history.recordSuccess(player, "minecraft:dirt", streak -> 100);
        history.recordFailure(player, "minecraft:stone");
        history.recordSuccess(player, "entity/minecraft:cow", streak -> 100);
        var collection = new CollectionProgress();
        assertTrue(collection.importHistory(player, history));
        assertEquals(2, collection.collected(player).size());
        assertEquals(java.util.Set.of("minecraft:dirt"), collection.correct(player));
        history.recordFailure(player, "minecraft:dirt");
        collection.importHistory(player, history);
        assertTrue(CollectionProgress.fromJson(collection.toJson()).correct(player).contains("minecraft:dirt"));
    }
    @Test void emptyAndMissingFieldsLoadSafely() {
        assertTrue(CollectionProgress.fromJson("{}").correct(player).isEmpty());
        assertTrue(CollectionProgress.fromJson("{\"" + player + "\":{}}").collected(player).isEmpty());
    }
}
