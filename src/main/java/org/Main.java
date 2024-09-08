package main.java.org;


import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import main.java.org.commands.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;

public class Main {
    @Getter
    static Dotenv env;

   // public static final List<String> blacklistCountingSystem = List.of("000");
    public static List<String> whitelistedServers = List.of("1081550768903049279", "1091783756429398016", "1172605145834594425"); // Füge die erlaubten Server-IDs hier ein
    public static String prefix;
    public static String botOwnerId;
    public static JDA client;

    public static void main(String[] args) throws  InterruptedException {
        setupEnv();

        env = Dotenv.configure().filename(".env").load();

        prefix = env.get("BOT_PREFIX");
        botOwnerId = env.get("BOT_OWNER_ID");

        startBot();

        new Thread(() -> {

            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            String command = "";

            try {
                while ((command = input.readLine()) != null) {
                    if (!command.startsWith("!")) return;
                    switch (command.substring(1)) {
                        case "bot-stop":
                            System.out.println("Bot ist jetzt Offline");
                            Main.client.shutdown();
                            break;
                        case "bot-start":
                            System.out.println("Bot wird gestartet");
                            try {
                                Main.startBot();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            break;
                        case "bot-restart":
                            Main.client.shutdown();
                            System.out.println("Bot ist jetzt Offline");
                            try {
                                Main.startBot();
                                System.out.println("Bot ist jetzt wieder Online");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }).start();
    }

    public static void startBot() throws InterruptedException {

        String token = Variable.token;
        JDABuilder jda = JDABuilder.createDefault(token)
                .setAutoReconnect(true)
                .setStatus(OnlineStatus.ONLINE)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_INVITES,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                        GatewayIntent.GUILD_MESSAGE_TYPING,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MODERATION,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS);


        jda.setActivity(Activity.streaming("mit Thorma", "https://twitch.tv/thorma90"));


        EnumSet<CacheFlag> enumSet = EnumSet.of(CacheFlag.EMOJI, CacheFlag.VOICE_STATE, CacheFlag.STICKER);
        jda.enableCache(enumSet);
        OffsetDateTime botStartTime = OffsetDateTime.now();
        jda.addEventListeners(
                new Variable(),
                new BotSettingsCommands(),
                new BotInfoCommand(botStartTime),
                new BirthdaySystem(),
                new RandomMeme(),
                new wetterbericht(),
                new GuildLeaveOnJoin(),
                new WitzGen(),
                new MCProfileCommandSlash(),
                new embed(),
                new messageinfocontextmenu(),
                new serverinfo(),
                new avatar(),
                new saySlashCommand(),
                new sonstiges(),
                new MCSERVERINFO(),
                new FlipCoinSlashCommand(),
                new CountingSystem(),
                new AutoPublish(),
                new MessageReaction(),
                new OneWordManager()
        );

        client = jda.build().awaitReady();

        List<Guild> guilds = client.getGuilds();

        System.out.println("[Bot] Der Bot ist nun online");

        System.out.println("[Bot] Der Prefix lautet: " + Main.prefix);

        // Gildenliste drucken
        if (!guilds.isEmpty()) {
            System.out.println("[Bot] Befindet sich auf folgenden Servern:");
            for (Guild guild : guilds) {
                System.out.println("Server-Name: " + guild.getName() + ", Server-ID: " + guild.getId());
            }
        }


        // Überprüfe jeden Server in der Gildenliste
        for (Guild guild : guilds) {
            if (!Main.whitelistedServers.contains(guild.getId())) {
                guild.leave().queue(); // Bot verlässt den nicht autorisierten Server
                System.out.println("Bot wurde von einem nicht autorisierten Server entfernt " +
                        "Server-Name: " + guild.getName() + ", Server-ID: " + guild.getId());
            }
        }


        OptionData optionDataminecraftprofilename = new OptionData(OptionType.STRING, "name", "Gebe hier denn spielernamen ein!", true);
        OptionData optionDataUserId = new OptionData(OptionType.STRING, "countingsystemuserid", "Hier eine userId von Discord angeben!", true);
        OptionData optionDatanextNum = new OptionData(OptionType.INTEGER, "countingsystemnextnum", "Hier muss die nächste Zahl die geschrieben werden sollt rein!", true);
        OptionData optionDataNachricht = new OptionData(OptionType.STRING, "nachricht", "Die Nachricht die vorgelesen werden soll!", true);
        OptionData optionDataUser = new OptionData(OptionType.USER, "user", "Wähle einen Benutzer!", true);

    client.getGuildById(Variable.server_id).updateCommands().addCommands(

            Commands.slash("bot", "Bot Einstellungen!")
                        .addSubcommands(new SubcommandData("info", "Zeigt Information zum Bot an!"))
                        .addSubcommands(new SubcommandData("restart", "Hier mit kannst du den Bot Restarten!"))
                        .addSubcommands(new SubcommandData("stop", "Hier mit kannst du den Bot Stoppen!")),

                //Command: /birthday
                Commands.slash("birthday", "Bearbeite den Geburtstag")
                        .addSubcommands(new SubcommandData("help", "Hilfe zum Geburtstagssystem.")
                        )
                        .addSubcommands(new SubcommandData("add", "Füge deinen Geburtstag hinzu.")
                               /* .addOption(OptionType.INTEGER, "tag", "Der Tag deines Geburtstags.", true)
                                .addOption(OptionType.INTEGER, "monat", "Der Monat deines Geburtstags.", true)
                                .addOption(OptionType.INTEGER, "jahr", "Das Jahr in dem du geboren wurdest.", false)*/
                        )
                        .addSubcommands(new SubcommandData("show", "Zeigt den Geburtstag von einem User.")
                                .addOption(OptionType.USER, "user", "Hier bitte den Nutzer auswählen.", true)
                        )
                        .addSubcommands(new SubcommandData("list", "Zeigt alle Geburtstage an.")
                        )
                        .addSubcommands(new SubcommandData("next", "Zeigt alle Geburtstage an.")
                        )
                        .addSubcommands(new SubcommandData("delete", "Lösche dein Geburtstag aus der Liste.")
                        )
                        .addSubcommands(new SubcommandData("admin-remove", "Lösche einen Geburtstag von einem User aus der Liste.")
                                .addOption(OptionType.USER, "user", "Hier bitte den Nutzer auswählen.", true)
                        )
                        .addSubcommands(new SubcommandData("admin-add", "Füge einen Geburtstag von einem User zu der Liste hinzu.")
                                .addOption(OptionType.USER, "user", "Hier bitte den Nutzer auswählen.", true)
                                .addOption(OptionType.INTEGER, "tag", "Der Tag deines Geburtstags.", true)
                                .addOption(OptionType.INTEGER, "monat", "Der Monat deines Geburtstags.", true)
                                .addOption(OptionType.INTEGER, "jahr", "Das Jahr in dem du geboren wurdest.", false)
                        ),

                //Ende

                //Command: /stinker
                Commands.slash("stinker", "Sehe wie viel Prozent jemand stinkt").addOptions(optionDataUser),
                //Command: /thorma
                Commands.slash("thorma", "Informationen zu Thorma"),
                //Command: /say <nachricht>
                Commands.slash("say", "Hiermit kannst du über den Bot eine Nachricht senden!").addOptions(optionDataNachricht),
                // Command: /avatar <user>
                Commands.slash("avatar", "Schickt ein Bild des Users").addOptions(optionDataUser),
                // Ende
                // Command: /embed
                Commands.slash("embed", "Sende ein belibiges Embed in diesen Channel!"),
                // Ende

                //Command: /countingsystem
            Commands.slash("countingsystem", "Das Counting System!")
                    .addSubcommands(new SubcommandData("set", "Setze die nächste Zahl die geschrieben werden soll!")
                            .addOptions(optionDataUserId)
                            .addOptions(optionDatanextNum)
                    ),
                //Ende

                // Command: /flipcoin
                Commands.slash("flipcoin", "Werfe eine Münze"),
                // Ende

                // Command: /meme
                Commands.slash("meme", "Ein zufälliges Meme wird in den Chat gesendet!"),
                // Ende

                // Command: /minecraft
                Commands.slash("minecraft", "Minecraft Befehle!")
                        .addSubcommands(new SubcommandData("regeln", "Seh dir die Regeln zum Thorma90 Netzwerk an!"))
                        .addSubcommands(new SubcommandData("server", "Seh dir Informationen zum Thorma90 Netzwerk an!"))
                        .addSubcommands(new SubcommandData("profile", "Minecraft Informationen zu einem Spieler!").addOptions(optionDataminecraftprofilename)),
                // Ende

                // Command: /witz
                Commands.slash("witz", "Generiere einen zufälligen Witz!")
                        .addSubcommands(new SubcommandData("en", "Englische Witze"))
                        .addSubcommands(new SubcommandData("de", "Deutsche Witze")),
                // Ende

                // Command: /würfel
                Commands.slash("würfel", "Würfle eine zufällige Zahl!"),
                // Ende

                // Command: /wetter
                Commands.slash("wetter", "Gibt dir das aktuelle Wetter für eine Stadt!")
                        .addOption(OptionType.STRING, "ort", "Der Name der Stadt", true),
                //Ende



                Commands.context(Command.Type.MESSAGE, "Edit Embed"),
                Commands.context(Command.Type.MESSAGE, "Nachricht Info")
        ).queue();

    }

    private static void setupEnv() {

        Path envPath = Path.of(".env");
        try {
            if (!Files.exists(envPath)) {
                Files.createFile(envPath);
                String values = "TOKEN=\nBOT_PREFIX=\nBOT_OWNER_ID=\nDB_URL=\nWEATHER_API_KEY=";
                Files.writeString(envPath, values, StandardOpenOption.WRITE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}