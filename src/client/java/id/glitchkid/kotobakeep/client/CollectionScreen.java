package id.glitchkid.kotobakeep.client;

import id.glitchkid.kotobakeep.KotobaKeep;
import id.glitchkid.kotobakeep.language.Japanese;
import id.glitchkid.kotobakeep.network.Packets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** A paged field guide: independent collection and correct-answer milestones for this world. */
final class CollectionScreen extends Screen {
    private record Entry(String id, ItemStack stack, String name, Japanese.Word word, String search) {}
    private static final String[] FILTERS = {"All items", "Not collected", "Not said correctly", "Collected", "Said correctly", "Collected: practice"};
    private final Screen parent;
    private final List<Entry> entries = new ArrayList<>();
    private final List<Button> tiles = new ArrayList<>();
    private List<Entry> visible = List.of();
    private EditBox search;
    private Button filterButton, previous, next;
    private Entry selected;
    private int filter, page, columns, rows, pageSize, gridLeft;
    private boolean supported;

    CollectionScreen(Screen parent) {
        super(Component.literal("Kotoba Keep · Item Collection"));
        this.parent = parent;
        for (var item : BuiltInRegistries.ITEM) {
            var word = KotobaKeep.JAPANESE.item(item.getDescriptionId());
            var stack = new ItemStack(item);
            if (word == null || stack.isEmpty()) continue;
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            String name = stack.getHoverName().getString();
            entries.add(new Entry(id, stack, name, word,
                (id + " " + name + " " + word.text() + " " + word.reading() + " " + Romaji.of(word.reading())).toLowerCase(Locale.ROOT)));
        }
        entries.sort(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER).thenComparing(Entry::id));
    }
    protected void init() {
        String query = search == null ? "" : search.getValue();
        int contentWidth = Math.min(480, width - 24);
        int left = (width - contentWidth) / 2;
        search = addRenderableWidget(new EditBox(font, left, 48, contentWidth - 134, 20, Component.literal("Search items")));
        search.setHint(Component.literal("Search name, Japanese, or item ID..."));
        search.setMaxLength(128);
        search.setValue(query);
        search.setResponder(value -> { page = 0; refresh(); });
        filterButton = addRenderableWidget(Button.builder(Component.literal(FILTERS[filter]), button -> {
            filter = (filter + 1) % FILTERS.length; page = 0; filterButton.setMessage(Component.literal(FILTERS[filter])); refresh();
        }).bounds(left + contentWidth - 130, 48, 130, 20).build());
        columns = Math.max(1, contentWidth / 34);
        rows = Math.max(1, (height - 166) / 34);
        pageSize = columns * rows;
        gridLeft = (width - columns * 34) / 2;
        tiles.clear();
        for (int i = 0; i < pageSize; i++) {
            final int slot = i;
            tiles.add(addRenderableWidget(Button.builder(Component.empty(), button -> {
                int index = page * pageSize + slot;
                if (index < visible.size()) selected = visible.get(index);
            }).bounds(gridLeft + i % columns * 34, 76 + i / columns * 34, 32, 32).build()));
        }
        previous = addRenderableWidget(Button.builder(Component.literal("<"), button -> { page--; refresh(); }).bounds(left, height - 27, 32, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"), button -> { page++; refresh(); }).bounds(left + 96, height - 27, 32, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Refresh"), button -> request()).bounds(left + contentWidth - 166, height - 27, 78, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose()).bounds(left + contentWidth - 82, height - 27, 82, 20).build());
        request();
        refresh();
    }
    private KotobaClient state() { return KotobaClient.INSTANCE; }
    private void request() {
        supported = ClientPlayNetworking.canSend(Packets.CollectionRequest.TYPE);
        if (supported) ClientPlayNetworking.send(new Packets.CollectionRequest());
    }
    void refresh() {
        if (search == null) return;
        String query = search.getValue().trim().toLowerCase(Locale.ROOT);
        visible = entries.stream().filter(entry -> entry.search.contains(query)).filter(entry -> switch (filter) {
            case 1 -> !state().collectedItems.contains(entry.id);
            case 2 -> !state().correctItems.contains(entry.id);
            case 3 -> state().collectedItems.contains(entry.id);
            case 4 -> state().correctItems.contains(entry.id);
            case 5 -> state().collectedItems.contains(entry.id) && !state().correctItems.contains(entry.id);
            default -> true;
        }).toList();
        page = Math.clamp(page, 0, Math.max(0, (visible.size() - 1) / pageSize));
        for (int i = 0; i < tiles.size(); i++) tiles.get(i).visible = page * pageSize + i < visible.size();
        previous.active = page > 0;
        next.active = (page + 1) * pageSize < visible.size();
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.centeredText(font, title, width / 2, 9, 0xFFC888E8);
        long collected = entries.stream().filter(e -> state().collectedItems.contains(e.id)).count();
        long correct = entries.stream().filter(e -> state().correctItems.contains(e.id)).count();
        String summary = !supported ? "This server needs the collection update too" : !state().collectionLoaded ? "Loading world progress..." :
            "Collected " + collected + "/" + entries.size() + "   ·   Said correctly " + correct + "/" + entries.size();
        g.centeredText(font, summary, width / 2, 25, 0xFFE6DDED);
        for (int i = 0; i < tiles.size(); i++) {
            int index = page * pageSize + i;
            if (index >= visible.size()) break;
            var entry = visible.get(index);
            int x = gridLeft + i % columns * 34, y = 76 + i / columns * 34;
            g.item(entry.stack, x + 8, y + 5);
            boolean got = state().collectedItems.contains(entry.id), said = state().correctItems.contains(entry.id);
            g.fill(x + 4, y + 26, x + 15, y + 29, got ? 0xFFFFC857 : 0xFF51495B);
            g.fill(x + 17, y + 26, x + 28, y + 29, said ? 0xFF86E6AA : 0xFF51495B);
            if (entry == selected) g.fill(x + 2, y + 1, x + 30, y + 3, 0xFFC888E8);
            if (mouseX >= x && mouseX < x + 32 && mouseY >= y && mouseY < y + 32) {
                var lines = new ArrayList<>(Screen.getTooltipFromItem(minecraft, entry.stack));
                lines.add(Component.literal("Collected: " + (got ? "Yes" : "No") + " · Said correctly: " + (said ? "Yes" : "No")));
                g.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
            }
        }
        if (visible.isEmpty()) g.centeredText(font, "No matching items", width / 2, 91, 0xFFB9A9C9);
        int detailY = height - 82;
        if (selected != null) {
            g.centeredText(font, font.plainSubstrByWidth(selected.name + " · " + selected.word.text(), width - 24), width / 2, detailY, 0xFFE6DDED);
            g.centeredText(font, font.plainSubstrByWidth(selected.word.reading() + " · " + Romaji.of(selected.word.reading()), width - 24), width / 2, detailY + 12, 0xFFB9A9C9);
        } else g.centeredText(font, "Select an item · gold: collected · green: said correctly", width / 2, detailY, 0xFFE6DDED);
        g.centeredText(font, "Includes creative-only items. Older pickups may be unknown.", width / 2, height - 49, 0xFFB9A9C9);
        g.text(font, (page + 1) + "/" + Math.max(1, (visible.size() + pageSize - 1) / pageSize), (width - Math.min(480, width - 24)) / 2 + 39, height - 21, 0xFFE6DDED);
    }
    @Override public void onClose() { minecraft.gui.setScreen(parent); }
}
