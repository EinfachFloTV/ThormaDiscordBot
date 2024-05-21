package main.java.org;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class BotInfoCommand extends ListenerAdapter {

    private long commandCounter = 0;
    private final OffsetDateTime botStartTime;
    private final List<OffsetDateTime> commandTimestamps = new ArrayList<>();

    public BotInfoCommand(OffsetDateTime botStartTime) {
        this.botStartTime = botStartTime;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getUser().isBot()) return;
        if (!event.getName().equals("bot")) return;
        if (!event.getSubcommandName().equals("info")) return;

       event.deferReply().setEphemeral(true).queue();

        commandCounter++;
        commandTimestamps.add(event.getTimeCreated());

        long ping = event.getJDA().getGatewayPing();

        OffsetDateTime currentTime = OffsetDateTime.now();
        long uptimeSeconds = botStartTime.until(currentTime, ChronoUnit.SECONDS);

        long days = uptimeSeconds / (60 * 60 * 24);
        long hours = (uptimeSeconds % (60 * 60 * 24)) / (60 * 60);
        long minutes = (uptimeSeconds % (60 * 60)) / 60;
        long seconds = uptimeSeconds % 60;

        String uptimeString = String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Bot-Info")
                .addField("General Informations",
                        "**Developers:** `einfachflo_tv` \n" +
                                "**Language:** Java \n" +
                                "**API:** Discord JDA: 5.0.0-beta.23 \n", true);


        List<String> guildNames = new ArrayList<>();
        for (Guild guild : event.getJDA().getGuilds()) {
            guildNames.add(guild.getName());
        }
        String guildNamesList = String.join("\n", guildNames);

        // Hier die Anzahl der Codezeilen und Wörter berechnen
        int[] codeStats = calculateCodeStats(new File("src/main/java"));
        int totalLines = codeStats[0];
        int totalWords = codeStats[1];

       /* embedBuilder.addField("Servers:", guildNamesList, true);*/
                embedBuilder.addField("Anzahl der Codezeilen:", String.valueOf(totalLines), true)
                .addField("Anzahl der Wörter:", String.valueOf(totalWords), true)
                .addField("Ping:", ping + "ms", true)
                .addField("Bot Uptime:", "`" + uptimeString + "`", true);

        embedBuilder.setColor(0xF01B0F);
        embedBuilder.setFooter("Hast du Fragen zum Bot? " + "Dann kannst du dich bei einfachflo_tv melden.");
        embedBuilder.setTimestamp(OffsetDateTime.now());

        event.getHook().sendMessageEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }

    private int[] calculateCodeStats(File directory) {
        int totalLines = 0;
        int totalWords = 0;

        try {
            List<File> javaFiles = listJavaFiles(directory);
            for (File file : javaFiles) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    totalLines++;
                    totalWords += line.split("\\s+").length;
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new int[]{totalLines, totalWords};
    }

    private List<File> listJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    javaFiles.addAll(listJavaFiles(file));
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
        return javaFiles;
    }
}

