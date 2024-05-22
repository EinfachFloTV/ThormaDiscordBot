package main.java.org.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Random;

public class sonstiges extends ListenerAdapter {

    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if(event.getName().equals("thorma")) {

            event.reply("Thorma stinkt nach verfaulten Thormatensuppe").queue();
        }

        if(event.getName().equals("stinker")) {

            User user = event.getOption("user").getAsUser();

            Random random = new Random();
            int randomNumber = random.nextInt(100) + 1;

            event.reply(user.getAsMention() + " stinkt zu " + randomNumber + "%").queue();
        }
    }
}
