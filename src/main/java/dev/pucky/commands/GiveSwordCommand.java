package dev.pucky.commands;

import dev.pucky.PSwordMain;
import dev.pucky.commands.util.MinecraftCommand;
import dev.pucky.commands.util.MinecraftCommandMetadata;
import dev.pucky.commands.util.tab.TabCompleteEntry;
import dev.pucky.config.ConfigManager;
import dev.pucky.sword.PSword;
import dev.pucky.util.MMUtil;
import dev.pucky.util.PlayerUtil;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveSwordCommand implements MinecraftCommand {
    public ConfigManager configManager;

    public GiveSwordCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(MMUtil.formatColorsPrefixed(
                    "<n>Komutun doğru kullanımı: <v>/psword giveSword <sword-id>"
            ));
            return;
        }

        PSword sword = configManager.getSword(args[0]).swordInstance();
        if (sword == null) {
            player.sendMessage(MMUtil.formatColorsPrefixed(
                    "<f>Geçersiz kılıç ID'si."
            ));
            return;
        }

        player.sendMessage(MMUtil.formatColorsPrefixed(
                "<s>Eşya envanterinize eklendi veya yere düştü."
        ));
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.3f);

        if (PlayerUtil.itemCanFit(player, sword.swordItem()))
            player.getInventory().addItem(sword.swordItem());

        else player.getWorld().dropItemNaturally(player.getLocation().add(0, 3, 0), sword.swordItem());
    }

    @Override
    public MinecraftCommandMetadata metadata() {
        return MinecraftCommandMetadata.of("giveSword", "Give a sword to yourself.")
                .tabCompletes(
                        TabCompleteEntry.of(0, ctx -> PSwordMain.instance.getConfigManager().getSwords().keySet())
                )
                .playerOnly()
                .permission("psword.givesword");
    }
}
