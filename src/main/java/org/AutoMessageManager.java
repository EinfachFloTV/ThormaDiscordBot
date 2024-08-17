package main.java.org;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class AutoMessageManager extends ListenerAdapter {

    private final JDA jda;
    private final String channelId;
    private final String[] morningMessages = {
            "Guten Morgen! 🌞",
            "Guten Morgen an alle! 🌞",
            "Einen schönen guten Morgen! ☕",
            "Morgen! Habt einen tollen Tag! 🌅"
    };
    private final String[] eveningMessages = {
            "Gute Nacht an alle! 🌜",
            "Einen schönen Abend! 🌙",
            "Abend! Entspannt euch gut! 🌆"
    };

    public AutoMessageManager(JDA jda, String channelId) {
        this.jda = jda;
        this.channelId = channelId;
        scheduleMessages();
    }

    private void scheduleMessages() {
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendRandomMessage(morningMessages);
            }
        }, getInitialDelay(8, 0), 24 * 60 * 60 * 1000);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendRandomMessage(eveningMessages);
            }
        }, getInitialDelay(20, 0), 24 * 60 * 60 * 1000);
    }

    private void sendRandomMessage(String[] messages) {
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel != null) {
            Random random = new Random();
            String message = messages[random.nextInt(messages.length)];
            channel.sendMessage(message).queue();
        }
    }

    private long getInitialDelay(int targetHour, int targetMinute) {
        long currentTimeMillis = System.currentTimeMillis();
        long targetTimeMillis = currentTimeMillis + ((targetHour * 60 + targetMinute) * 60 * 1000);
        return targetTimeMillis - currentTimeMillis;
    }
}