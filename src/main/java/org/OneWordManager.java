// OneWordManager.java
package main.java.org;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.TimeUnit;

public class OneWordManager extends ListenerAdapter {
    private String channelId = "1274763829108146237";

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isFromGuild() && event.getChannel().getId().equals(channelId)) {
            String message = event.getMessage().getContentRaw().trim();
            String userId = event.getAuthor().getId();

            String lastUserId = MyJDBC.getLastUserId();
            if (userId.equals(lastUserId)) {
                event.getChannel().sendMessage("Warte, bis jemand anderes ein Wort hinzugefügt hat!")
                        .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                try {
                    event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                } catch (InsufficientPermissionException e) {
                    System.out.println("Fehlende Berechtigung zum Löschen der Nachricht.");
                }
                return;
            }

            if (message.contains(" ")) {
                event.getChannel().sendMessage("Bitte sende nur ein Wort auf einmal!")
                        .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
                try {
                    event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                } catch (InsufficientPermissionException e) {
                    System.out.println("Fehlende Berechtigung zum Löschen der Nachricht.");
                }
                return;
            }

            if (message.equalsIgnoreCase("!resetstory") || message.equals(".")) {
                String currentStory = MyJDBC.getCurrentStory().trim();
                event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();
                event.getChannel().sendMessage("Die Geschichte wurde beendet!\nStory: `" + currentStory + "`").queue();
                MyJDBC.saveCurrentStory("");
                MyJDBC.saveLastUserId("");
                event.getChannel().sendMessage("Eine neue Geschichte hat begonnen!").queueAfter(3, TimeUnit.SECONDS);
                return;
            }

            String currentStory = MyJDBC.getCurrentStory();
            currentStory += message + " ";
            MyJDBC.saveCurrentStory(currentStory);
            MyJDBC.saveLastUserId(userId);
            event.getMessage().addReaction(Emoji.fromFormatted("✅")).queue();
        }
    }
}