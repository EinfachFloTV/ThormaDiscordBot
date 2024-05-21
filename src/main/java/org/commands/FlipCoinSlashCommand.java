package main.java.org.commands;


import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Random;

public class FlipCoinSlashCommand extends ListenerAdapter {
    private final Random random = new Random();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("flipcoin")) {

            int result = random.nextInt(2);
            String outcome = result == 0 ? "Kopf" : "Zahl";
            event.reply("Die Münze ist auf " + outcome + " gelandet.").setEphemeral(true).queue();


        } else if (event.getName().equals("würfel")) {
            int number = rollDice();
            event.reply("Du hast " + number + " gewürfelt!").setEphemeral(true).queue();
        }
    }
    private int rollDice() {
        Random rand = new Random();
        return rand.nextInt(6) + 1;
    }
}
