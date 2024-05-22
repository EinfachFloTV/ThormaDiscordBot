package main.java.org;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BirthdaySystem extends ListenerAdapter {

    public void onReady(ReadyEvent event) {
        scheduleDailyBirthdayCheck();
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getMessage().equals("!setup geburtstage")) {
            event.getMessage().delete().queue();
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("Geburtstags System")
                    .setDescription("Hier könnt ihr sehen, wer alles Geburtstag hat. Wenn du möchtest, dass an deinem Geburtstag eine Nachricht gesendet wird, kannst du deinen Geburtstag hinzufügen mit:\n" +
                            "\n" +
                            "/birthday add [tag] [monat] [jahr (optional)]\n" +
                            "\n" +
                            "Um einen Geburtstag zu löschen, benutze:\n" +
                            "\n" +
                            "/birthday delete\n" +
                            "\n" +
                            "Mit:\n" +
                            "\n" +
                            "/birthday show [user]\n" +
                            "\n" +
                            "kannst du sehen, wann ein bestimmter Nutzer Geburtstag hat.");
            event.getChannel().sendMessageEmbeds(eb.build()).queue();

        }
    }
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("birthday")) {
            switch (event.getSubcommandName()) {
                case "add":
                    handleAddBirthday(event);
                    break;
                case "show":
                    handleGetBirthday(event);
                    break;
                case "delete":
                    handleRemoveBirthday(event);
                    break;
                case "list":
                    handleListBirthday(event);
                    break;
                case "admin-add":
                    handleAdminAddBirthday(event);
                    break;
                case "admin-remove":
                    handleAdminRemoveBirthday(event);
                    break;
            }
        }
    }

    private void handleListBirthday(SlashCommandInteractionEvent event) {
        Map<String, String> birthdays = MyJDBC.getAllBirthdays();
        if (birthdays.isEmpty()) {
            event.reply("Es sind keine Geburtstage eingetragen.").setEphemeral(true).queue();
        } else {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("Eingetragene Geburtstage")
                    .setColor(Color.BLUE);
            birthdays.forEach((userId, birthday) -> {

                String[] birthdaydata = birthday.split(" ");
                int day = Integer.parseInt(birthdaydata[0]);
                int month = Integer.parseInt(birthdaydata[1]);

                int yearnow = LocalDate.now().getYear();

                int year;
                Integer age = null;
                if (!birthdaydata[2].isEmpty()) {
                    year = Integer.parseInt(birthdaydata[2]);
                } else {
                    year = 0;
                }

                LocalDate date = LocalDate.of(yearnow, month, day);

                LocalDate today = LocalDate.now();
                age = today.getYear() - year;
                if (today.getMonthValue() < date.getMonthValue() || (today.getMonthValue() == date.getMonthValue() && today.getDayOfMonth() < date.getDayOfMonth())) {
                    age--;
                }

                long epochSeconds = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

                String absoluteTimestamp = "<t:" + epochSeconds + ":F>";
                String relativeTimestamp = "<t:" + epochSeconds + ":R>";

                User user = event.getJDA().getUserById(userId);
                if (user != null) {
                    String userMention = user.getEffectiveName();

                    if(year != 0) {
                        eb.addField(userMention + " (" + age + " Lebensjahr)", absoluteTimestamp + " " + relativeTimestamp, false);
                    } else {
                        eb.addField(userMention , absoluteTimestamp + " " + relativeTimestamp, false);
                    }
                }
            });

            event.replyEmbeds(eb.build()).setEphemeral(true).queue();
        }
    }
    private void handleAdminAddBirthday(SlashCommandInteractionEvent event) {

        if (event.getMember().hasPermission(Permission.ADMINISTRATOR) || event.getMember().getId().equals(Main.botOwnerId)) {
            int day = event.getOption("tag").getAsInt();
            int month = event.getOption("monat").getAsInt();
            Integer year = event.getOption("jahr") != null ? event.getOption("jahr").getAsInt() : null;
            User user = event.getOption("user").getAsUser();
            String userId = user.getId();

            // Überprüfe, ob der Benutzer bereits eingetragen ist
            String existingBirthday = MyJDBC.getBirthday(userId);
            if (existingBirthday != null) {
                event.reply("Der Nutzer ist bereits eingetragen. Sein Geburtstag ist: " + existingBirthday + ", um ihn aus der Datenbank zu löchen gebe `/birthday admin-remove username` ein").setEphemeral(true).queue();
                return;
            }

            // Überprüfe, ob der Tag und der Monat in den gültigen Bereich fallen
            if (month < 1 || month > 12) {
                event.reply("Der Monat muss zwischen 1 und 12 liegen.").setEphemeral(true).queue();
                return;
            }
            if (day < 1 || day > 31) {
                event.reply("Der Tag muss zwischen 1 und 31 liegen.").setEphemeral(true).queue();
                return;
            }

            // Überprüfe, ob das Jahr nicht größer als das aktuelle Jahr ist
            int currentYear = LocalDate.now().getYear();
            int yearlimit = currentYear - 1;
            if (year != null && year > yearlimit) {
                event.reply("Das Jahr kann nicht größer als " + yearlimit + " sein.").setEphemeral(true).queue();
                return;
            }

            MyJDBC.addBirthday(userId, day, month, year);

            String message = "Der Geburtstag von (" + user.getAsMention() + ") wurde eingetragen als UserId: " + userId + ", Tag: " + day + ", Monat: " + month;
            if (year != null) {
            message += ", Jahr: " + year;
            } else {
            message += ", Jahr: nicht eingetragen";
            }

            event.reply(message).setEphemeral(true).queue();
        } else {
            event.reply("Hierzu hast du keine Rechte!").setEphemeral(true).queue();
        }
    }

    private void handleAddBirthday(SlashCommandInteractionEvent event) {
        int day = event.getOption("tag").getAsInt();
        int month = event.getOption("monat").getAsInt();
        Integer year = event.getOption("jahr") != null ? event.getOption("jahr").getAsInt() : null;

        String userId = event.getUser().getId();

        // Überprüfe, ob der Benutzer bereits eingetragen ist
        String existingBirthday = MyJDBC.getBirthday(userId);
        if (existingBirthday != null) {
            event.reply("Du bist bereits eingetragen. Dein Geburtstag ist: " + existingBirthday + ", um dich aus der Datenbank zu löchen gebe `/birthday delete` ein").setEphemeral(true).queue();
            return;
        }

        // Überprüfe, ob der Tag und der Monat in den gültigen Bereich fallen
        if (month < 1 || month > 12) {
            event.reply("Der Monat muss zwischen 1 und 12 liegen.").setEphemeral(true).queue();
            return;
        }
        if (day < 1 || day > 31) {
            event.reply("Der Tag muss zwischen 1 und 31 liegen.").setEphemeral(true).queue();
            return;
        }

        // Überprüfe, ob das Jahr nicht größer als das aktuelle Jahr ist
        int currentYear = LocalDate.now().getYear();
        int yearlimit = currentYear - 1;
        if (year != null && year > yearlimit) {
            event.reply("Das Jahr kann nicht größer als " + yearlimit + " sein.").setEphemeral(true).queue();
            return;
        }


        MyJDBC.addBirthday(userId, day, month, year);

        String message = "Dein Geburtstag (" + event.getUser().getAsMention() + ") wurde eingetragen als UserId: " + userId + ", Tag: " + day + ", Monat: " + month;
        if (year != null) {
            message += ", Jahr: " + year;
        } else {
            message += ", Jahr: nicht eingetragen";
        }

        event.reply(message).setEphemeral(true).queue();
    }


    private void handleGetBirthday(SlashCommandInteractionEvent event) {
        User user = event.getOption("user").getAsUser();
        String userId = user.getId();
        String birthday = MyJDBC.getBirthday(userId);
        if (birthday != null) {
            EmbedBuilder eb = new EmbedBuilder()
            .setTitle("Geburtstag von " + user.getEffectiveName())
            .setThumbnail(user.getAvatarUrl())
            .setDescription("Der Geburtstag von " + user.getName() + " ist am: " + birthday);
            event./*reply(user.getAsMention()).addEmbeds*/replyEmbeds(eb.build()).setEphemeral(true).queue();
        } else {
            event.reply("Es wurde kein Geburtstag zu diesem Nutzer gefunden!").setEphemeral(true).queue();
        }
    }

    private void handleRemoveBirthday(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        String userId = user.getId();
        if (!MyJDBC.birthdayExists(userId)) {
            event.reply("Du bist nicht in der Datenbank eingetragen!").setEphemeral(true).queue();
        } else {
            MyJDBC.removeBirthday(userId);
            event.reply("Der Geburtstag von " + user.getAsMention() + " wurde entfernt!").setEphemeral(true).queue();
        }
    }

    private void handleAdminRemoveBirthday(SlashCommandInteractionEvent event) {
        if (event.getMember().hasPermission(Permission.ADMINISTRATOR) || event.getMember().getId().equals(Main.botOwnerId)) {

            User user = event.getOption("user").getAsUser();
            //User user = event.getUser();
            String userId = user.getId();
            if (!MyJDBC.birthdayExists(userId)) {
                event.reply("Der Nutzer hat keinen Geburtstagseintrag in der Datenbank.").setEphemeral(true).queue();
            } else {
                MyJDBC.removeBirthday(userId);
                event.reply("Der Geburtstag von " + user.getAsMention() + " wurde entfernt!").setEphemeral(true).queue();
            }
        } else {
            event.reply("Hierzu hast du keine Rechte!").setEphemeral(true).queue();
        }
    }

    private static void scheduleDailyBirthdayCheck() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            LocalDate today = LocalDate.now();
            List<String> birthdayUsers = MyJDBC.getBirthdaysForDate(today.getDayOfMonth(), today.getMonthValue());
            if (!birthdayUsers.isEmpty()) {
                TextChannel channel = Main.client.getTextChannelById(Variable.BirthdayChannelId);
                if (channel != null) {
                    for (String userId : birthdayUsers) {
                        User user = Main.client.retrieveUserById(userId).complete(); // Benutzerobjekt abrufen
                        if (user != null) {
                    EmbedBuilder eb = new EmbedBuilder()
                            .setTitle("Happy Birthday!")
                            .setDescription(user.getAsMention() + " hat heute Geburtstag!!! 🎉🎉🎉\n"+
                                    "Wir wünschen ihm alles gute");

                            channel.sendMessage(user.getAsMention()).addEmbeds(eb.build()).queue(message -> {
                                // Reactions hinzufügen
                                message.addReaction(Emoji.fromFormatted("🎉")).queue();
                                message.addReaction(Emoji.fromFormatted("🥳")).queue();
                                message.addReaction(Emoji.fromFormatted("🎂")).queue();
                            });
                        }
                    }
                }
            }
        };


        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextCheckTime = now.withHour(0).withMinute(0).withSecond(0);
        if (now.compareTo(nextCheckTime) > 0) {
            nextCheckTime = nextCheckTime.plusDays(1); // Wenn die aktuelle Zeit nach 0 Uhr liegt, setze die Überprüfung auf den nächsten Tag
        }
        Duration initialDelay = Duration.between(now, nextCheckTime);

        scheduler.scheduleAtFixedRate(task, initialDelay.getSeconds(), Duration.ofDays(1).getSeconds(), TimeUnit.SECONDS);
    }
}
