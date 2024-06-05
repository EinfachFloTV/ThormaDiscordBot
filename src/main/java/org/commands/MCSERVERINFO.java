package main.java.org.commands;

import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MCSERVERINFO extends ListenerAdapter {

    private static class ServerStatus {
        Players players;

        private static class Players {
            int online;
            int max;
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("minecraft")) return;
        if(event.getSubcommandName().equals("server")) {
            String ip = "thorma90.net";
            int javaPort = 25565;
            int bedrockPort = 19132;

            try {
                ServerStatus javaStatus = getServerStatus("https://api.mcstatus.io/v2/status/java/", ip, javaPort);
                ServerStatus bedrockStatus = getServerStatus("https://api.mcstatus.io/v2/status/bedrock/", ip, bedrockPort);

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(Color.GREEN);
                embedBuilder.setTitle("Server Status");
                embedBuilder.setDescription("Hinweis für Konsolen-Spieler (PlayStation, Xbox): Um dem Server beizutreten, benötigst du eine zusätzliche App. Für Unterstützung kannst du einfachflo_tv auf Discord kontaktieren.");
                embedBuilder.setFooter("Bitte halte dich an die Serverregeln. Verstöße gegen die Regeln können zu einer Verwarnung oder einem permanenten Ausschluss führen.");

                if (javaStatus != null) {
                    embedBuilder.addField("Java Edition", "Server-IP: " + ip + "\nPort: " + javaPort + " (nicht erforderlich)" + "\nOnline: " + javaStatus.players.online + "/" + javaStatus.players.max, false);
                } else {
                    embedBuilder.addField("Java Edition", "Nicht erreichbar", false);
                }

                if (bedrockStatus != null && bedrockStatus.players != null) {
                    embedBuilder.addField("Bedrock Edition", "Server-IP: " + ip + "\nPort: " + bedrockPort + "\nOnline: " + bedrockStatus.players.online + "/" + bedrockStatus.players.max, false);
                } else {
                    embedBuilder.addField("Bedrock Edition", "Nicht erreichbar", false);
                }

               Button bedrock = Button.link("https://mcstatus.io/status/bedrock/" + ip + ":" + bedrockPort, "Bedrock Status Web");
               Button java = Button.link("https://mcstatus.io/status/java/" + ip + ":" + javaPort, "Java Status Web");
               Button Information = Button.secondary("minecraftstatusmehrinfos", "Mehr Infos").withEmoji(Emoji.fromFormatted("❓"));

                event.replyEmbeds(embedBuilder.build()).addActionRow(java,bedrock,Information).setEphemeral(true).queue();

            } catch (IOException e) {
                event.reply("Der Serverstatus konnte nicht abgerufen werden. Bitte versuche es später erneut.").setEphemeral(true).queue();
                e.printStackTrace();
            }
        }
    }
public void onButtonInteraction(ButtonInteractionEvent event) {
    if (event.getComponentId().equals("minecraftstatusmehrinfos")) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Woher beziehen wir unsere Infos?");
        embedBuilder.addField("Mojang API", "Überprüft die anfragen.", false);
        embedBuilder.addField("MCSTATUS IO API", "Mit der API können wir auslesen wie viele Spieler online sind und ob das Netzwerk online ist.", false);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }
}
    private ServerStatus getServerStatus(String apiUrl, String ip, int port) throws IOException {
        URL url = new URL(apiUrl + ip + ":" + port);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            Gson gson = new Gson();
            return gson.fromJson(response.toString(), ServerStatus.class);
        }
        return null;
    }
}
