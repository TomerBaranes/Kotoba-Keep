package id.glitchkid.kotobakeep.client;

import id.glitchkid.kotobakeep.KotobaKeep;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** On-demand vocabulary, independent of microphone readiness and challenge progress. */
final class ItemNameTooltip {
    private ItemNameTooltip() {}

    static void add(ItemStack stack, List<Component> lines) {
        if (stack.isEmpty()) return;
        // Use the same base item name as the spoken challenges, preserving custom names above it.
        var word = KotobaKeep.JAPANESE.item(stack.getItem().getDescriptionId());
        if (word == null) return;

        int at = Math.min(1, lines.size());
        var config = KotobaKeep.CONFIG;
        if (config.tooltipJapanese) lines.add(at++, Component.literal(word.text()).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (config.tooltipHiragana && !word.reading().isBlank()
                && (!config.tooltipJapanese || !word.reading().equals(word.text()))) {
            lines.add(at++, Component.literal(word.reading()).withStyle(ChatFormatting.GRAY));
        }
        if (config.tooltipRomaji && word.pronounceable()) {
            String romaji = word.ruby().stream().map(r -> Romaji.of(r.reading()))
                .reduce((a, b) -> a + " " + b).orElse("");
            if (!romaji.isBlank()) lines.add(at, Component.literal(romaji).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
