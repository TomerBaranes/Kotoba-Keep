package id.glitchkid.kotobakeep.server;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.util.*;

/** Permanent milestones, separate from the spaced-review streak which resets after a failure. */
public final class CollectionProgress {
    private static final class Entry {
        Set<String> collected = new HashSet<>();
        Set<String> correct = new HashSet<>();
    }
    private Map<String, Entry> players = new HashMap<>();
    private Entry entry(UUID player) { return players.computeIfAbsent(player.toString(), key -> new Entry()); }
    public boolean collect(UUID player, String item) { return entry(player).collected.add(item); }
    public boolean correct(UUID player, String item) { return entry(player).correct.add(item); }
    public Set<String> collected(UUID player) { return Set.copyOf(entry(player).collected); }
    public Set<String> correct(UUID player) { return Set.copyOf(entry(player).correct); }
    public boolean importHistory(UUID player, LearningProgress history) {
        boolean changed = false;
        for (var word : history.words(player).entrySet()) {
            if (word.getKey().startsWith("entity/")) continue;
            changed |= collect(player, word.getKey());
            if (word.getValue().streak() > 0) changed |= correct(player, word.getKey());
        }
        return changed;
    }
    public String toJson() { return new GsonBuilder().setPrettyPrinting().create().toJson(players); }
    public static CollectionProgress fromJson(String json) {
        var result = new CollectionProgress();
        Map<String, Entry> data = new GsonBuilder().create().fromJson(json,
            new TypeToken<Map<String, Entry>>(){}.getType());
        if (data != null) {
            data.forEach((id, entry) -> {
                if (entry == null) return;
                if (entry.collected == null) entry.collected = new HashSet<>();
                if (entry.correct == null) entry.correct = new HashSet<>();
                result.players.put(id, entry);
            });
        }
        return result;
    }
}
