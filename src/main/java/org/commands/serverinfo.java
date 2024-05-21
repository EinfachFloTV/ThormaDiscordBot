package main.java.org.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class serverinfo extends ListenerAdapter {


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (!event.getName().equals("server")) return;

        if (!event.getSubcommandName().equals("info")) return;

        Guild guild = event.getGuild();

        int memberCount = guild.getMemberCount();
        int boostCount = guild.getBoostCount();
        int textChannels = guild.getTextChannels().size();
        int voiceChannels = guild.getVoiceChannels().size();
        int roleCount = guild.getRoles().size();
        int categoryCount = guild.getCategories().size();
        int emojiCount = guild.getEmojis().size();
        List<net.dv8tion.jda.api.entities.Member> members = event.getGuild().getMembers();
        List<Member> bots = new ArrayList<>();

        int humanCount = 0;

        for (Member member : members) {
            if (!member.getUser().isBot()) {
                humanCount++;
            }
        }

        for (net.dv8tion.jda.api.entities.Member member : members) {
            if (member.getUser().isBot()) {
                bots.add(member);
            }
        }
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(0x3498db)
                .setTitle("Server Information for " + guild.getName())
                .setFooter(guild.getDescription())
                .addField("Server Owner", guild.getOwner().getAsMention(), false)
                .addField("Creation Date", guild.getTimeCreated().format(DateTimeFormatter.ISO_LOCAL_DATE), false)
                .addField("Members", String.valueOf(humanCount), true);
        StringBuilder botList = new StringBuilder();
        for (Member bot : bots) {
            botList.append(bot.getUser().getName() + "\n");

        }
        embed.addField("Bots", bots.size() + "\n" + botList, true)
                .addField("Boosts", String.valueOf(boostCount), true)
                .addField("All Channels", String.valueOf(textChannels + voiceChannels), true)
                .addField("Text Channels", String.valueOf(textChannels), true)
                .addField("Voice Channels", String.valueOf(voiceChannels), true)
                .addField("Roles", String.valueOf(roleCount), true)
                .addField("Categories", String.valueOf(categoryCount), true)
                .addField("Emojis", String.valueOf(emojiCount), true);

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
