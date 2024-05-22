package main.java.org.commands;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    private static final String[] MEME_SUBREDDITS = { "AnimalMemes", "wholesomememes" };
    private static Random random = new Random();
    private static int randomIndex = random.nextInt(MEME_SUBREDDITS.length);
    private static String REDDIT_API_ENDPOINT = "https://www.reddit.com/r/"+ MEME_SUBREDDITS[randomIndex] +"/hot.json";

    private static OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder().header("User-Agent", "RandomMemeThormaBot/1.0").build();
                return chain.proceed(request);
            }).build();
    private final Set<String> fetchedMemes = new HashSet<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("meme")) {

            String memeUrl = getRandomMemeUrlFromReddit();
            if(memeUrl != null) {
                MessageEmbed embed = new EmbedBuilder().setTitle("Hier ist ein Random Meme:").setImage(memeUrl)
                        .setFooter("Die Posts stammen aus Reddit!").build();
                event.replyEmbeds(embed).queue();
            } else {
                event.reply("Bitte versuche es später noch einmal!").setEphemeral(true).queue();
            }
        }
    }

    private String getRandomMemeUrlFromReddit() {
        for (String subreddit : MEME_SUBREDDITS) {
            String requestUrl = String.format(REDDIT_API_ENDPOINT, subreddit);
            try {
                Request request = new Request.Builder().url(requestUrl).build();
                Response response = httpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                JSONObject jsonResponse = new JSONObject(response.body().string());
                JSONArray posts = jsonResponse.getJSONObject("data").getJSONArray("children");
                for (int i = 0; i < posts.length(); i++) {
                    JSONObject postData = posts.getJSONObject(i).getJSONObject("data");
                    String imageUrl = postData.getString("url");
                    if (!fetchedMemes.contains(imageUrl) && (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".png")
                            || imageUrl.endsWith(".gif"))) {
                        fetchedMemes.add(imageUrl);
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