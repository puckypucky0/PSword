package dev.pucky.util;

import dev.pucky.PSwordMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public static String legacyToMini(String text) {
        if (text == null) return null;

        String converted = LEGACY_SPIGOT_HEX.matcher(text).replaceAll("<reset><!italic><#$1$2$3$4$5$6>");
        converted = LEGACY_HEX.matcher(converted).replaceAll("<reset><!italic><#$1>");

        return LEGACY_SINGLE.matcher(converted)
                .replaceAll(match -> LEGACY_TAGS.get(match.group(1).toLowerCase().charAt(0)));
    }

    public static List<String> legacyToMini(List<String> lines) {
        List<String> converted = new ArrayList<>();
        for (String line : lines)
            converted.add(legacyToMini(line));

        return converted;
    }

    private static final Pattern LEGACY_SPIGOT_HEX = Pattern.compile(
            "(?i)[&§]x[&§]([0-9a-f])[&§]([0-9a-f])[&§]([0-9a-f])[&§]([0-9a-f])[&§]([0-9a-f])[&§]([0-9a-f])"
    );
    private static final Pattern LEGACY_HEX = Pattern.compile("(?i)[&§]#([0-9a-f]{6})");
    private static final Pattern LEGACY_SINGLE = Pattern.compile("(?i)[&§]([0-9a-fk-or])");

    private static final Map<Character, String> LEGACY_TAGS = Map.ofEntries(
            Map.entry('0', "<reset><!italic><black>"),
            Map.entry('1', "<reset><!italic><dark_blue>"),
            Map.entry('2', "<reset><!italic><dark_green>"),
            Map.entry('3', "<reset><!italic><dark_aqua>"),
            Map.entry('4', "<reset><!italic><dark_red>"),
            Map.entry('5', "<reset><!italic><dark_purple>"),
            Map.entry('6', "<reset><!italic><gold>"),
            Map.entry('7', "<reset><!italic><gray>"),
            Map.entry('8', "<reset><!italic><dark_gray>"),
            Map.entry('9', "<reset><!italic><blue>"),
            Map.entry('a', "<reset><!italic><green>"),
            Map.entry('b', "<reset><!italic><aqua>"),
            Map.entry('c', "<reset><!italic><red>"),
            Map.entry('d', "<reset><!italic><light_purple>"),
            Map.entry('e', "<reset><!italic><yellow>"),
            Map.entry('f', "<reset><!italic><white>"),
            Map.entry('k', "<obfuscated>"),
            Map.entry('l', "<bold>"),
            Map.entry('m', "<strikethrough>"),
            Map.entry('n', "<underlined>"),
            Map.entry('o', "<italic>"),
            Map.entry('r', "<reset><!italic>")
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
