package main.java.org.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;

public class WitzGen extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("witz")) {
            if(event.getSubcommandName().equals("en")) {

                OkHttpClient client2 = new OkHttpClient();
                String url2 = "https://v2.jokeapi.dev/joke/Any";

                Request request2 = new Request.Builder()
                        .url(url2)
                        .build();

                try (Response response2 = client2.newCall(request2).execute()) {
                    if (!response2.isSuccessful()) {
                        throw new IOException("Unexpected code " + response2);
                    }

                    JSONObject json = new JSONObject(response2.body().string());
                    String joke2 = json.getString("setup");
                    String joke4 = json.getString("delivery");
                    String cat2 = json.getString("category");
                    String type1 = json.getString("type");
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("A joke to laugh at");
                    builder.setDescription(joke2+"\n"+joke4);
                    builder.setColor(0x00FF00);
                    builder.setTimestamp(OffsetDateTime.now(Clock.systemUTC()));
                    builder.setFooter("Category: " + cat2 + " • Type: " + type1, null);

                    event.replyEmbeds(builder.build()).queue();

                } catch (IOException e) {
                    System.err.println("Error getting joke: " + e.getMessage());
                }
            }
            else if(event.getSubcommandName().equals("de")) {

                OkHttpClient client3 = new OkHttpClient();
                String url3 = "https://v2.jokeapi.dev/joke/Any?lang=de";

                Request request3 = new Request.Builder()
                        .url(url3)
                        .build();

                try (Response response3 = client3.newCall(request3).execute()) {
                    if (!response3.isSuccessful()) {
                        throw new IOException("Unexpected code " + response3);
                    }

                    JSONObject json = new JSONObject(response3.body().string());
                    String joke3 = json.getString("setup");
                    String joke5 = json.getString("delivery");
                    String cat3 = json.getString("category");
                    String type2 = json.getString("type");
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("Ein Witz zum Lachen");
                    builder.setDescription(joke3+"\n"+joke5);
                    builder.setColor(0x00FF00);
                    builder.setTimestamp(OffsetDateTime.now(Clock.systemUTC()));
                    builder.setFooter("Kategorie: " + cat3 + " • Type: " + type2, null);

                    event.replyEmbeds(builder.build()).queue();

                } catch (IOException e) {
                    System.err.println("Fehler beim Witz: " + e.getMessage());
                }
            }
        }
    }
}
