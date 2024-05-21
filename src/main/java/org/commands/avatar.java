package main.java.org.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class avatar extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getName().equals("avatar")) {

            User u = event.getOption("user").getAsUser();

            String url = u.getEffectiveAvatarUrl();

            EmbedBuilder e = new EmbedBuilder()
                    .setColor(0x2e3137)
                    .setTitle("Avatar von " + u.getAsTag())
                    .setImage(url)
                    .setFooter("© " + event.getGuild().getName(), event.getGuild().getIconUrl());


            event.replyEmbeds(e.build()).setEphemeral(true).queue();

        }

    }

}
