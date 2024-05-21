package main.java.org;


import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BotSettingsCommands extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("bot")) return;

        if (event.getSubcommandName().equals("restart")) {
            if (event.getMember().getId().equals(Main.botOwnerId) || event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.reply("Bot wird neugestartet...").setEphemeral(true).queue();
                Main.client.shutdown();
                System.out.println("Bot ist jetzt Offline");
                try {
                    Main.startBot();
                    System.out.println("Bot ist jetzt wieder Online");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                event.reply("Beep, Du kannst den Bot nicht neu starten, da du keine Administratorrechte hast.").setEphemeral(true).queue();
            }
        } else if (event.getSubcommandName().equals("stop")) {
            if (event.getMember().getId().equals(Main.botOwnerId) || event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.reply("Bot wird heruntergefahren...").setEphemeral(true).queue();
                Main.client.shutdown();
                System.out.println("Bot ist jetzt Offline");

            } else {
                event.reply("Beep, Du kannst den Bot nicht stoppen, da du keine Administratorrechte hast.").setEphemeral(true).queue();
            }
        }
    }
}
