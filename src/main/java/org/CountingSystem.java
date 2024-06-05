package main.java.org;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;

public class CountingSystem extends ListenerAdapter {
    private static Emoji sucessemoji = Emoji.fromFormatted("✅"); //Das Emoji ✅
    private static Emoji falseemoji =  Emoji.fromFormatted("❌"); //Das Emoji ❌
    private static Emoji falseemoji2 =  Emoji.fromFormatted("<:DieZweiWas:1206167736498257970>"); //Das Emoji <:DieZweiWas:1206167736498257970>
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if(!event.getChannel().getId().equals(Variable.countingChannelID)) return; //Es wird der Channel überprüft
        if(event.getMember().getUser().isBot()) return;

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
                        .setDescription(event.getMember().getAsMention() + " hat zweimal eine Nummer geschrieben <:DieZweiWas:1206167736498257970>!\n" +
                                " Der Counter wurde auf **0** zurückgesetzt!")
                        .setColor(Color.red);

                event.getChannel().sendMessageEmbeds(e.build()).queue();
                nextNum = 1;

                MyJDBC.setCountNumber("1", nextNum);

        }
    }
}
