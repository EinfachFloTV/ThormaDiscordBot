package main.java.org.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.time.OffsetDateTime;

public class embed extends ListenerAdapter {

    String messageID;

    TextInput TITLE = TextInput.create("titelfeld", "Titel", TextInputStyle.SHORT).setRequired(false)
            .setPlaceholder("Titel des Embeds").build();

    TextInput DESCRIPTION = TextInput.create("beschfeld", "Beschreibung", TextInputStyle.PARAGRAPH).setRequired(false)
            .setPlaceholder("Beschreibung des Embeds").build();

    // TextInput FOOT = TextInput.create("fussfeld", "Fusszeile", TextInputStyle.SHORT).setRequired(false)
            // .setPlaceholder("Fusszeile des Embeds").build();
    Modal modalembed1 = Modal.create("embedmodal1", "Erstelle dein Embed!").addActionRow(TITLE).addActionRow(DESCRIPTION).build();
    Modal modalembed2 = Modal.create("embedmodal2", "Bearbeite dein Embed!").addActionRow(TITLE).addActionRow(DESCRIPTION).build();


    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getName().equals("embed")) {

            event.replyModal(modalembed1).queue();
        }
    }
    public void onModalInteraction(ModalInteractionEvent event) {

        String title = event.getValue("titelfeld").getAsString();
        String description = event.getValue("beschfeld").getAsString();
        // String footer = event.getValue("fussfeld").getAsString();
       // String img = event.getValue("imgfeld32").getAsString();


        if (event.getModalId().equals("embedmodal1")) {

            EmbedBuilder embedBuilder1 = new EmbedBuilder()
                    .setColor(java.awt.Color.GRAY)
                    .setTitle(title)
                    .setDescription(description)
                    .setFooter(event.getUser().getId())
                    .setTimestamp(OffsetDateTime.now())
                    .setAuthor(event.getUser().getName(), null, event.getMember().getEffectiveAvatarUrl());

            event.getChannel().sendMessageEmbeds(embedBuilder1.build()).queue();

           /* if (!img.isEmpty()) {
                embedBuilder1.setImage(img);
            }*/

            event.reply("Embed wurde Abgesendet!").setEphemeral(true).queue();

        } if (event.getModalId().equals("embedmodal2")) {
            EmbedBuilder embedBuilder2 = new EmbedBuilder()
                    .setColor(java.awt.Color.GRAY)
                    .setTitle(title)
                    .setDescription(description)
                    .setFooter(event.getUser().getId())
                    .setTimestamp(OffsetDateTime.now())
                    .setAuthor(event.getUser().getName(), null, event.getMember().getEffectiveAvatarUrl());

         /*   if (!img.isEmpty()) {
                embedBuilder2.setImage(img);
            }*/

            event.getChannel().editMessageEmbedsById(messageID).setEmbeds(embedBuilder2.build()).queue();
            event.reply("Embed wurde Bearbeitet!").setEphemeral(true).queue();

        }
    }

    public void onMessageContextInteraction(MessageContextInteractionEvent event) {

        if (event.getName().equals("Edit Embed")) {

            // Message message = event.getTarget();

            messageID = event.getTarget().getId();

            String AuthorId = null;
            if (!event.getTarget().getEmbeds().isEmpty()) {
                AuthorId = event.getTarget().getEmbeds().get(0).getFooter().getText().toString();
            }

            if (event.getMember().getId().equals(AuthorId)) {
                event.replyModal(modalembed2).queue();
            } else {

                event.reply("Dir gehört dieses Embed nicht! oder es ist ein Fehler aufgetreten!").setEphemeral(true).queue();

            }
        }
    }
}
