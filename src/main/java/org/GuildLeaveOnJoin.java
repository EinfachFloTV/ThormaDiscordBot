package main.java.org;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildLeaveOnJoin extends ListenerAdapter {
    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();

        if (!Main.whitelistedServers.contains(guild.getId())) {
            TextChannel channelToSend = null;

            // Suche nach einem Kanal, in dem der Bot Schreibrechte hat
            for (TextChannel channel : guild.getTextChannels()) {
                if (channel.canTalk()) {
                    channelToSend = channel;
                    break;
                }
            }

            if (channelToSend != null) {
                channelToSend.sendMessage(event.getGuild().getOwner().getAsMention() + " Dein Server '" + guild.getName() + "' ist leider nicht auf der Whitelist vom Thorma90 Bot.\n " +
                        "Um zur Whitelist hinzugefügt zu werden, kontaktiere bitte EinfachFloTV (<@871714118946660352>).").queue();

            } else {
                System.out.println("Kein geeigneter Kanal gefunden, um eine Nachricht zu senden.");
            }

            System.out.println("Bot wurde von einem nicht autorisierten Server entfernt " +
                    "Server-Name: " + guild.getName() + ", Server-ID: " + guild.getId());

            guild.leave().queue(); // Bot verlässt den nicht autorisierten Server

        }
    }
}