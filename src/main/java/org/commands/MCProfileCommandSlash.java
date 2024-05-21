package main.java.org.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class  MCProfileCommandSlash extends ListenerAdapter {

    public void onSlashCommandInteraction (SlashCommandInteractionEvent event) {

        if (event.getName().equals("minecraft")) {
            String name = Objects.requireNonNull(event.getOption("name")).getAsString();

            // Set the API endpoint URL
            String apiUrl = "https://api.mojang.com/users/profiles/minecraft/" + name;

         /*   if(name.length() < 2)  {
             event.reply("Dieser Name konnte nicht gefunden werden!").setEphemeral(true).queue();
            }*/

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/json");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                String response = content.toString();
                //System.out.println(response);
                /*
                // Open a connection to the API endpoint
                URL url = new URL(apiUrl);
                URLConnection connection = url.openConnection();

                // Cast the connection to a HTTP connection
                HttpURLConnection httpConnection = (HttpURLConnection) connection;

                // Set the request method and properties
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Content-Type", "application/json");

                // Read the response
                InputStream inputStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();*/ //alte URL connection + alte Regex Filter
                /*
                // Parse the JSON response
                JSONObject json = new JSONObject(response.toString());

                // Print the cat fact
                String id = json.getString("id");
                String PlayerName = json.getString("name");
                String NameMcProfile = "https://de.namemc.com/profile/" + PlayerName;

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setImage("https://crafatar.com/renders/body/" + id + ".png?overlay");
                //embedBuilder.setTitle("Profil");
                //embedBuilder.setDescription("**Name:** " + PlayerName);
                embedBuilder.addField("**Name:**", PlayerName, false);
                embedBuilder.addField("**Uuid:**", id, false);
                */ //Parse the JSON response

                //UUID
                //Pattern pattern = Pattern.compile("\"id\":\"(.*?)\"");
                Pattern pattern = Pattern.compile("\"id\"\\s*:\\s*\"(\\w+)\"");
                Matcher matcher = pattern.matcher(response);
                String uuid = "null";
                if (matcher.find()) {
                    uuid = matcher.group(1);
                    //System.out.println(uuid);
                }
                //Name
                //Pattern pattern1 = Pattern.compile("\"name\":\"(.*?)\"");
                Pattern pattern1 = Pattern.compile("\"name\"\\s*:\\s*\"(\\w+)\"");
                Matcher matcher1 = pattern1.matcher(response);
                String PlayerName = "null";
                if (matcher1.find()) {
                    PlayerName = matcher1.group(1);
                    //System.out.println(PlayerName);
                }

                String NameMcProfile = "https://de.namemc.com/profile/" + PlayerName;


                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setImage("https://minotar.net/armor/body/"+ uuid);
                embedBuilder.setColor(0x2e3137);
                //embedBuilder.setTitle("Minecraft-Profil");
                //embedBuilder.setDescription("**Name:** " + PlayerName);
                embedBuilder.addField("**Name:**", PlayerName, false);
                embedBuilder.addField("**Uuid:**", uuid, false);

                Button downloadSkin = Button.link("https://crafatar.com/skins/" + uuid, "Download Skin");
                Button MCProfile = Button.link(NameMcProfile, "View Profile");
                Button Information = Button.secondary("mehrinfos", "Mehr Infos").withEmoji(Emoji.fromFormatted("❓"));


                event.replyEmbeds(embedBuilder.build()).addActionRow(downloadSkin, MCProfile, Information).queue();
            } catch (IOException e) {
                /*
                EmbedBuilder error = new EmbedBuilder();
                EmbedBuilder error = new EmbedBuilder();
                error.setTitle("Fehler, konnte Profil nicht finden.");
                error.setDescription("Hast du den Namen richtig eingegeben?");
                error.addField("", "Falls du alles richtig eingegeben hast, kannst du diesen Error gerne mir Privat ( <@682264027723989073> ) oder in einem Ticket ( <#1030775534587756554> ) melden. Dann werde ich mein Bestes geben, den Fehler zu beheben.", false);

                event.replyEmbeds(error.build()).queue();
                 */
                e.printStackTrace();
            }

        }
    }

    public void onButtonInteraction (ButtonInteractionEvent event) {
        if (event.getComponentId().equals("mehrinfos")) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Woher beziehen wir unsere Infos?");
            embedBuilder.addField("Mojang API", "Mithilfe des angegebenden Namens gelangen wir an die uuid.", false);
            embedBuilder.addField("Minotar/Craftar API", "Mit der uuid kann man sich mithilfe von Minotar und Craftar Render des Skins machen lassen.", false);
            embedBuilder.addField("NameMC", "NameMC nutzen wir, damit der User weitere Informationen zu einem Spieler bekommen kann.", false);
            embedBuilder.addField("null?", "null bedeutet das dieser Spieler nicht Existiert oder nicht gefunden wurde.", false);

            event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
        }
    }
}
