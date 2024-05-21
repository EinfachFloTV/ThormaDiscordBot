package main.java.org.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

public class RandomMeme extends ListenerAdapter {
    private static final String[] MEME_SUBREDDITS = {"memes", "dankmemes", "wholesomememes"};
    private static final String REDDIT_API_ENDPOINT = "https://www.reddit.com/r/%s/hot.json?limit=50";
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("User-Agent", "RandomMemeBot/1.0")
                        .build();
                return chain.proceed(request);
            })
            .build();
    private final Random random = new Random();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("meme")) {
            String memeUrl = getRandomMemeUrlFromReddit();
            if (memeUrl != null) {
                TextChannel channel = event.getChannel().asTextChannel();
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Hier ist ein Random Meme:")
                        .setImage(memeUrl)
                        .setFooter("Die Posts stammen aus Reddit!")
                        .build();
                event.replyEmbeds(embed).queue();
            } else {
                event.reply("Tut mir leid, ich konnte im Moment keine Memes finden.\nVersuche es später noch einmal!").setEphemeral(true).queue();
            }
        }
    }

    private String getRandomMemeUrlFromReddit() {
        for (String subreddit : MEME_SUBREDDITS) {
            String requestUrl = String.format(REDDIT_API_ENDPOINT, subreddit);
            try {
                Request request = new Request.Builder()
                        .url(requestUrl)
                        .build();
                Response response = httpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                JSONObject jsonResponse = new JSONObject(response.body().string());
                JSONArray posts = jsonResponse.getJSONObject("data").getJSONArray("children");
                if (posts.length() > 0) {
                    JSONObject postData = posts.getJSONObject(random.nextInt(posts.length())).getJSONObject("data");
                    String imageUrl = postData.getString("url");
                    if (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".png") || imageUrl.endsWith(".gif")) {
                        return imageUrl;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
