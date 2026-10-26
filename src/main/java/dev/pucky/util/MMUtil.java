package dev.pucky.util;

import dev.pucky.PSwordMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MMUtil {

    public static final Component EMPTY_COMPONENT = Component.empty();

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    /** Kısa renk etiketlerini (<n>, <v>, <f>, <s>) açar. */
    public static Component formatColorsPrefixed(String mm) {
        String title = PSwordMain.instance.getConfig().getString("title") + " ";
        return deserialize(title + mm
                .replace("<n>", "<#FF9B45>")
                .replace("<v>", "<#AD5900>")
                .replace("<f>", "<#B21E1A>")
                .replace("<s>", "<#23D127>"))
                .hoverEvent(HoverEvent.showText(deserialize(title)));
    }

    public static Component formatColors(String mm) {
        String title = PSwordMain.instance.getConfig().getString("title") + " ";
        return deserialize(mm
                .replace("<n>", "<#FF9B45>")
                .replace("<v>", "<#AD5900>")
                .replace("<f>", "<#B21E1A>")
                .replace("<s>", "<#23D127>"))
                .hoverEvent(HoverEvent.showText(deserialize(title)));
    }

    public static Component title() {
        String title = PSwordMain.instance.getConfig().getString("title") + " ";
        return deserialize(title);
    }

    public static Component deserialize(String mm) {
        return miniMessage.deserialize(mm);
    }

    public static List<Component> deserialize(List<String> lore) {
        List<Component> components = new ArrayList<>();
        for (String line : lore)
            components.add(deserialize(line));

        return components;
    }

    public static String stripLegacy(String text) {
        if (text == null) return null;

        return LEGACY_CODES.matcher(text).replaceAll("");
    }

    private static final Pattern LEGACY_CODES = Pattern.compile(
            "(?i)[&§]x(?:[&§][0-9a-f]){6}"   // §x§F§F§A§A§0§0
            + "|[&§]#[0-9a-f]{6}"            // &#FFAA00
            + "|[&§][0-9a-fk-or]"            // &a, §l, &r
    );

    public static String serialize(Component component) {
        return miniMessage.serialize(component);
    }

    public static List<String> serialize(List<Component> lore) {
        List<String> lines = new ArrayList<>();
        for (Component component : lore)
            lines.add(serialize(component));

        return lines;
    }
}
