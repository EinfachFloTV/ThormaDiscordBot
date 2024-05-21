package main.java.org.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.format.DateTimeFormatter;


public class messageinfocontextmenu extends ListenerAdapter {

    public void onMessageContextInteraction(MessageContextInteractionEvent event) {

        if (event.getName().equals("Nachricht Info")) {

            Message m = event.getTarget();

            EmbedBuilder info = new EmbedBuilder();
            info.setTitle("Infos zu einer Nachricht!");
            info.setDescription("Hier findest du Infos zu folgender Nachricht:\n" +  m.getContentStripped());
            info.addField("ID:", m.getId(), false);
            info.addField("Zur Nachricht springen:", m.getJumpUrl(), false);
            info.addField("Channel", m.getGuildChannel().getAsMention(), false);
            info.addField("Nachricht Autor:", m.getAuthor().getAsMention() + "\n" + m.getAuthor(), false);
            info.addField("Nachricht Type:", String.valueOf(m.getType()), false);
            info.addField("Länge:", String.valueOf(m.getContentRaw().length()), false);
            info.addField("Time-Created:", m.getTimeCreated().format(DateTimeFormatter.BASIC_ISO_DATE), false);
            if(m.isEdited()) {
                info.addField("Time Edited:", String.valueOf(m.getTimeEdited()), false);
            }

            if(m.isEphemeral()) {
                info.addField("Ephemeral:", "Ja", false);
            }
            if(m.isTTS()) {
                info.addField("TSS:", "Ja", false);
            }
            if(m.isPinned()) {
                info.addField("Gepinnt:", "Ja", false);
            }
            info.setFooter("© " + event.getGuild().getName(), event.getGuild().getIconUrl());

            event.replyEmbeds(info.build()).setEphemeral(true).queue();

        }

    }

}
