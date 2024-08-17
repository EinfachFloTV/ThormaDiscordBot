package main.java.org;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;

public class CountingSystem extends ListenerAdapter {
    private static Emoji sucessemoji = Emoji.fromFormatted("✅"); //Das Emoji ✅
    private static Emoji falseemoji =  Emoji.fromFormatted("❌"); //Das Emoji ❌
    private static Emoji falseemoji2 =  Emoji.fromFormatted("<:DieZweiWas:1206167736498257970>"); //Das Emoji <:DieZweiWas:1206167736498257970>
    private static Emoji falseemoji3 =  Emoji.fromFormatted("<:thorma8Facepalm:1105470844051927060>"); //Das Emoji <:thorma8Facepalm:1105470844051927060>

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (!event.getName().equals("countingsystem")) return;

        if (event.getMember().getId().equals(Main.botOwnerId)) {

            if (event.getSubcommandName().equals("set")) {

                int nextNum = event.getOption("countingsystemnextnum").getAsInt();
                String userId = event.getOption("countingsystemuserid").getAsString();

                event.reply("Bitte Warten!").setEphemeral(true).queue();

                MyJDBC.setCountNumber(userId, nextNum);

                event.getHook().editOriginal("Erfolgreich!" +
                        "\n userId: " + userId +
                        "\n nextNum: " + nextNum).queue();
            }
        } else {
            event.reply("Hierzu hast du Keine Rechte!").setEphemeral(true).queue();
        }
    }
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if(!event.getChannel().getId().equals(Variable.countingChannelID)) return; //Es wird der Channel überprüft
        if(event.getMember().getUser().isBot()) return;

        //if(!Main.blacklistCountingSystem.contains(event.getMember().getId())) { //Wenn der Nutzer nicht auf der Blacklist steht

            try {
                int num = Integer.parseInt(event.getMessage().getContentStripped());
            } catch (NumberFormatException e) {
                return;
            }

            int num = Integer.parseInt(event.getMessage().getContentStripped());

            String[] data = MyJDBC.getCountNumber(); //data = die daten aus der CounterSystem-Tabelle;
            assert data != null;

            int nextNum;


            if (!event.getMember().getId().equals(data[0])) { // 0 ist die Stelle wo die UserId gespeichert ist.
                if (Integer.parseInt(data[1]) == num) { //1 ist die Stelle wo die nächte Nummer gespeichert ist.

                    event.getMessage().addReaction(sucessemoji).queue();
                    nextNum = num + 1;

                    MyJDBC.setCountNumber(event.getMember().getId(), nextNum);

                } else {
                    //der User hat eine falsche nummer geschrieben
                    event.getMessage().addReaction(falseemoji).queue();
                    event.getMessage().addReaction(falseemoji3).queue();

                    EmbedBuilder e = new EmbedBuilder()
                            .setTitle("Falsch! Die Nächste Zahl ist: **1**")
                            .setDescription(event.getMember().getAsMention() + " hat eine falsche Nummer geschrieben!\n" +
                                    " Der Counter wurde auf **0** zurückgesetzt!")
                            .setColor(Color.red);

                    event.getChannel().sendMessageEmbeds(e.build()).queue();
                    nextNum = 1;
                    MyJDBC.setCountNumber("1", nextNum);
                }
            } else {

                // der User hat zweimal hintereinander eine Nummer geschrieben.
                event.getMessage().addReaction(falseemoji).queue();
                event.getMessage().addReaction(falseemoji2).queue();

                EmbedBuilder e = new EmbedBuilder()
                        .setTitle("Falsch! Die Nächste Zahl ist: **1**")
                        .setDescription(event.getMember().getAsMention() + " hat zweimal eine Nummer geschrieben! <:DieZweiWas:1206167736498257970> \n" +
                                " Der Counter wurde auf **0** zurückgesetzt!")
                        .setColor(Color.red);

                event.getChannel().sendMessageEmbeds(e.build()).queue();
                nextNum = 1;

                MyJDBC.setCountNumber("1", nextNum);

            }
       /* } else { //Wenn der Nutzer auf der Blacklist ist führt er dass aus:
            event.getMessage().delete().queue();
           event.getMember().getUser().openPrivateChannel().complete().sendMessage("Du wurdest aus dem Counting System gesperrt, deshalb wurde deine Nachricht gelöscht.").queue();
        }*/
    }
 /*   @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {

        if(!event.getChannel().getId().equals(Variable.countingChannelID)) return; //Es wird der Channel überprüft
        if(event.getMember().getUser().isBot()) return;

        if(event.getMessage().isEdited()) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(event.getMember().getEffectiveName() + " hat die Nachricht bearbeitet")
                .setThumbnail("https://cdn.discordapp.com/emojis/1206167736498257970.webp?size=48&quality=lossless")
                .setDescription("Du Schlingel, dass habe ich gesehen " + event.getMember().getAsMention());
                .addField("Vor der Bearbeitung", event.getMessage().get, true )
                .addField("Nach der Bearbeitung", event.getMessage().getContentRaw(), true);

        event.getMessage().replyEmbeds(eb.build()).queue();

            }
    }*/
}
