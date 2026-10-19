package dev.pucky.util;

import dev.pucky.PSwordMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

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
