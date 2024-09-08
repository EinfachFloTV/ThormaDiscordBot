package main.java.org.commands;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RandomMeme extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("meme")) {
            try {
                JSONObject memeInfo = getMeme();
                if (!memeInfo.has("title") || !memeInfo.has("imageUrl") || !memeInfo.has("postUrl") || !memeInfo.has("subreddit")) {
                    throw new Exception("Unvollständige Meme-Daten vom Server");
                }

                String title = memeInfo.getString("title");
                String imageUrl = memeInfo.getString("imageUrl");
                String postUrl = memeInfo.getString("postUrl");
                String subreddit = memeInfo.getString("subreddit");

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.BLUE);
                builder.setTitle(title, postUrl);
                builder.setImage(imageUrl);
                String footerText = "Meme von r/" + subreddit;
                builder.setFooter(footerText);

                event.replyEmbeds(builder.build()).queue();
            }
            catch (Exception e) {
                event.reply("Es gab einen Fehler beim Abrufen des Memes: " + e.getMessage()).queue();
            }
        }
    }

    private JSONObject getMeme() throws Exception {
        URL url = new URL("https://bensonheimer992.np200.de/animalmemes");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Server returned code " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String jsonResponse = response.toString();

        if (jsonResponse.isEmpty()) {
            throw new Exception("Empty response from server");
        }

        return new JSONObject(jsonResponse);
    }
}

