package main.java.org.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Random;

public class sonstiges extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        // Deaktiviert
       /* if(event.getName().equals("thorma")) {
            String[] RandomNachrichten = { "Thorma stinkt nach verfaulten Thormatensuppe", "Thorma ist ein Minigame suchti", "Thorma müffelt" };
            Random random = new Random();
            int randomIndex = random.nextInt(RandomNachrichten.length);

            event.reply(RandomNachrichten[randomIndex]).queue();
        }

        if(event.getName().equals("stinker")) {

            User user = event.getOption("user").getAsUser();


            if(user.getId().equals("691564223787958292")) {

                event.reply(user.getEffectiveName() + " stinkt zu " + "100" + "%").queue();

            } else {

                Random random = new Random();
                int stinkerwert = random.nextInt(100) + 1;

                event.reply(user.getEffectiveName() + " stinkt zu " + stinkerwert + "%").queue();

            }
        }*/
    }
}
