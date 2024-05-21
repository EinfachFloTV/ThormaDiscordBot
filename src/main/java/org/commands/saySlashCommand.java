package main.java.org.commands;

import main.java.org.Main;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class saySlashCommand extends ListenerAdapter {
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if(!event.getName().equals("say")) return;

        String message = event.getOption("nachricht").getAsString();

        if(event.getMember().hasPermission(Permission.ADMINISTRATOR) || event.getMember().getId().equals(Main.botOwnerId)) {

            if (!message.isEmpty()) {
                event.reply("Erfolgreich!").setEphemeral(true).queue();
                event.getChannel().sendMessage(message).queue();
            } else {
                event.reply("Bitte gebe eine Nachricht an!").setEphemeral(true).queue();
            }
        } else {
            event.reply("Du hast keine Berechtigung dazu!").setEphemeral(true).queue();
        }
    }
}
