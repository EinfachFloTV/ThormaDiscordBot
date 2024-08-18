package main.java.org;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageReaction extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if(event.isFromGuild()) {
            String message = event.getMessage().getContentRaw().toLowerCase();
            if(message.contains("gumo") || message.contains("guten morgen") || message.contains("mahlzeit")) {
                event.getMessage().addReaction(Emoji.fromFormatted("<:thorma8Herz:1102177773780467712>")).queue();
                event.getMessage().addReaction(Emoji.fromFormatted("<:thorma8Hi:1105470782756360272>")).queue();
            } else if(message.contains("guna") || message.contains("gute nacht")) {
                event.getMessage().addReaction(Emoji.fromFormatted("<:thorma8Herz:1102177773780467712>")).queue();
                event.getMessage().addReaction(Emoji.fromFormatted("<:PandazZz:1206174677639110666>")).queue();
            }
        }
    }
}