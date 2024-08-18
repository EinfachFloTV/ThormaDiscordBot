package main.java.org;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BirthdaySystem extends ListenerAdapter {

    public void onReady(ReadyEvent event) {scheduleDailyBirthdayCheck();}

    /*@Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().equals("!setup geburtstage")) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("Geburtstags System")
                    .setDescription("Hier könnt ihr sehen, wer alles Geburtstag hat. Wenn du möchtest, dass an deinem Geburtstag eine Nachricht gesendet wird, kannst du deinen Geburtstag hinzufügen mit:\n" +
                            "\n" +
                            "/birthday add [tag] [monat] [jahr (ausgeschrieben z.B 2003) (optional)]\n" +
                            "\n\n" +
                            "Um einen Geburtstag zu löschen, benutze:\n" +
                            "\n" +
                            "/birthday delete\n" +
                            "\n\n" +
                            "Um zu sehen wenn der Nutzer geburtstag hat, benutze:\n" +
                            "\n" +
                            "/birthday show [user]\n" );
            event.getChannel().sendMessageEmbeds(eb.build()).queue();
        }
    }*/


    TextInput tag = TextInput.create("birthdayaddtagmodal", "Tag (z.B. 6 für den 6ten Tag im Monat)", TextInputStyle.SHORT).setRequired(true)
            .setPlaceholder("Bitte gib hier deinen Tag des Geburtstag ein")
            .setMaxLength(2)
            .setMinLength(1)
            .build();
    TextInput monat = TextInput.create("birthdayaddmonatmodal", "Monat (z.B. 4 für April)", TextInputStyle.SHORT).setRequired(true)
            .setPlaceholder("Bitte gib hier deinen Monat des Geburtstag ein")
            .setMaxLength(2)
            .setMinLength(1)
            .build();
    TextInput jahr = TextInput.create("birthdayaddjahrmodal", "Jahr (z.B. 2006), (optional)", TextInputStyle.SHORT).setRequired(false)
            .setPlaceholder("Bitte gib hier dein Geburtsjahr ein (optional)")
            .setMaxLength(4)
            .setMinLength(4).build();

    Modal modaladdbirthday = Modal.create("birthdayaddmodal", "Geburtstag hinzufügen!").addActionRow(tag).addActionRow(monat).addActionRow(jahr).build();


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("birthday")) {
            switch (event.getSubcommandName()) {
                case "help":
                    handlehelpBirthday(event);
                    break;
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
                case "next":
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

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] idParts = event.getComponentId().split(":");
        if (idParts[0].equals("birthday")) {
            int currentPage = Integer.parseInt(idParts[1]);
            List<Map.Entry<String, String>> sortedBirthdays = MyJDBC.getAllBirthdays().entrySet().stream()
                    .sorted((entry1, entry2) -> {
                        LocalDate nextBirthday1 = getNextBirthday(entry1.getValue());
                        LocalDate nextBirthday2 = getNextBirthday(entry2.getValue());
                        return nextBirthday1.compareTo(nextBirthday2);
                    })
                    .collect(Collectors.toList());

            List<MessageEmbed> pages = generateBirthdayPages(sortedBirthdays, null, event);

            if (idParts[2].equals("next")) {
                currentPage++;
            } else if (idParts[2].equals("prev")) {
                currentPage--;
            }

            event.editMessageEmbeds(pages.get(currentPage))
                    .setActionRow(
                            Button.primary("birthday:" + currentPage + ":prev", "⏪").withDisabled(currentPage == 0),
                            Button.primary("birthday:" + currentPage + ":next", "⏩").withDisabled(currentPage == pages.size() - 1)
                    )
                    .queue();
        }
    }

    public void onModalInteraction(ModalInteractionEvent event) {

        if(event.getModalId().equals("birthdayaddmodal")) {

            String userId = event.getUser().getId();
            int day = Integer.parseInt(event.getValue("birthdayaddtagmodal").getAsString());
            int month = Integer.parseInt(event.getValue("birthdayaddmonatmodal").getAsString());
            String yearString = event.getValue("birthdayaddjahrmodal") != null ? event.getValue("birthdayaddjahrmodal").getAsString() : null;
            Integer year = (yearString != null && !yearString.isEmpty()) ? Integer.parseInt(yearString) : null;

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
    }

    private void handlehelpBirthday(SlashCommandInteractionEvent event) {

            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("Geburtstags System")
                    .setDescription("Wenn du möchtest, dass an deinem Geburtstag eine Nachricht gesendet wird, kannst du deinen Geburtstag hinzufügen mit:\n" +
                            "\n" +
                            "/birthday add\n" +
                            "\n\n" +
                            "Um einen Geburtstag zu löschen, benutze:\n" +
                            "\n" +
                            "/birthday delete\n" +
                            "\n\n" +
                            "Um zu sehen wann ein bestimmter Nutzer geburtstag hat, benutze:\n" +
                            "\n" +
                            "/birthday show [user]\n" +
                            "\n\n" +
                            "Um alle Geburtstage zu sehen, benutze:\n" +
                            "\n" +
                            "/birthday list\n");

            event.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }
    private void handleListBirthday(SlashCommandInteractionEvent event) {
        Map<String, String> birthdays = MyJDBC.getAllBirthdays();
        if (birthdays.isEmpty()) {
            event.reply("Es sind keine Geburtstage eingetragen.").setEphemeral(true).queue();
        } else {
            List<Map.Entry<String, String>> sortedBirthdays = birthdays.entrySet().stream()
                    .sorted((entry1, entry2) -> {
                        LocalDate nextBirthday1 = getNextBirthday(entry1.getValue());
                        LocalDate nextBirthday2 = getNextBirthday(entry2.getValue());
                        return nextBirthday1.compareTo(nextBirthday2);
                    })
                    .collect(Collectors.toList());

            List<MessageEmbed> pages = generateBirthdayPages(sortedBirthdays, event, null);
            event.replyEmbeds(pages.get(0))
                    .addActionRow(
                            Button.primary("birthday:0:prev", "⏪").asDisabled(),
                            Button.primary("birthday:0:next", "⏩").withDisabled(pages.size() <= 1)
                    )
                    .queue();
        }
    }

    private List<MessageEmbed> generateBirthdayPages(List<Map.Entry<String, String>> sortedBirthdays, SlashCommandInteractionEvent slashEvent, ButtonInteractionEvent buttonEvent) {
        List<MessageEmbed> pages = new ArrayList<>();
        EmbedBuilder eb = new EmbedBuilder().setTitle("Eingetragene Geburtstage").setColor(Color.BLUE);
        int count = 0;
        int pageNumber = 1;

        for (Map.Entry<String, String> entry : sortedBirthdays) {
            if (count % 7 == 0 && count != 0) {
                eb.setFooter("Seite " + pageNumber);
                pages.add(eb.build());
                eb = new EmbedBuilder().setTitle("Eingetragene Geburtstage").setColor(Color.BLUE);
                pageNumber++;
            }

            String userId = entry.getKey();
            String birthday = entry.getValue();
            String[] birthdaydata = birthday.split(" ");
            int day = Integer.parseInt(birthdaydata[0]);
            int month = Integer.parseInt(birthdaydata[1]);
            int year = birthdaydata.length > 2 && !birthdaydata[2].isEmpty() ? Integer.parseInt(birthdaydata[2]) : 0;

            LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
            if (date.isBefore(LocalDate.now())) {
                date = date.plusYears(1);
            }

            long epochSeconds = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String absoluteTimestamp = "<t:" + epochSeconds + ":F>";
            String relativeTimestamp = "<t:" + epochSeconds + ":R>";

            User user = null;
            if (slashEvent != null) {
                user = slashEvent.getJDA().getUserById(userId);
            } else if (buttonEvent != null) {
                user = buttonEvent.getJDA().getUserById(userId);
            }

            if (user != null) {
                String userMention = user.getEffectiveName();
                int age = year != 0 ? LocalDate.now().getYear() - year : 0;
                if (year != 0) {
                    eb.addField(userMention + " (" + age + " Lebensjahr)", absoluteTimestamp + " " + relativeTimestamp, false);
                } else {
                    eb.addField(userMention, absoluteTimestamp + " " + relativeTimestamp, false);
                }
            }
            count++;
        }
        eb.setFooter("Seite " + pageNumber);
        pages.add(eb.build());
        return pages;
    }

    private LocalDate getNextBirthday(String birthday) {
        String[] parts = birthday.split(" ");
        int day = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int year = parts.length > 2 && !parts[2].isEmpty() ? Integer.parseInt(parts[2]) : LocalDate.now().getYear();

        LocalDate nextBirthday = LocalDate.of(LocalDate.now().getYear(), month, day);
        if (nextBirthday.isBefore(LocalDate.now()) || nextBirthday.isEqual(LocalDate.now())) {
            nextBirthday = nextBirthday.plusYears(1);
        }
        return nextBirthday;
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

        String userId = event.getUser().getId();

        // Überprüfe, ob der Benutzer bereits eingetragen ist
        String existingBirthday = MyJDBC.getBirthday(userId);
        if (existingBirthday != null) {
            event.reply("Du bist bereits eingetragen. Dein Geburtstag ist: " + existingBirthday + ", um dich aus der Datenbank zu löchen gebe `/birthday delete` ein").setEphemeral(true).queue();
            return;
        }

        event.replyModal(modaladdbirthday).queue();

       /* int day = event.getOption("tag").getAsInt();
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

        event.reply(message).setEphemeral(true).queue();*/
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
            //event.reply("Der Geburtstag von: " + user.getAsMention() + " ist am: " + birthday).setEphemeral(true).queue();
        } else {
            event.reply("Es wurde kein Geburtstag zu diesem Nutzer gefunden!").setEphemeral(true).queue();
        }
    }


    private void handleRemoveBirthday(SlashCommandInteractionEvent event) {
        //User user = event.getOption("user").getAsUser();
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

                            String birthday = MyJDBC.getBirthdayUser1(userId);
                            String[] birthdaydata = birthday.split(" ");

                            int day = Integer.parseInt(birthdaydata[0]);
                            int month = Integer.parseInt(birthdaydata[1]);

                            int year = 0;
                            if (birthdaydata.length > 2 && !birthdaydata[2].isEmpty()) {
                                year = Integer.parseInt(birthdaydata[2]);
                            }

                            int yearnow = LocalDate.now().getYear();
                            LocalDate date1 = LocalDate.of(yearnow, month, day);
                            LocalDate today1 = LocalDate.now();
                            int age = year != 0 ? today1.getYear() - year : 0;

                            if (year != 0 && (today1.getMonthValue() < date1.getMonthValue() || (today1.getMonthValue() == date1.getMonthValue() && today1.getDayOfMonth() < date1.getDayOfMonth()))) {
                                age--;
                            }

                            EmbedBuilder eb = new EmbedBuilder()
                                    .setTitle("Happy Birthday!")
                                   .setThumbnail(user.getEffectiveAvatarUrl());

                            if (year != 0) {
                                eb.setDescription(user.getAsMention() + " hat heute Geburtstag!!! 🎉🎉🎉\n"+
                                        "und wird " + age + ". Jahre alt\n" +
                                        "Wir wünschen ihm/ihr alles Gute");
                            } else {
                                eb.setDescription(user.getAsMention() + " hat heute Geburtstag!!! 🎉🎉🎉\n"+
                                        "Wir wünschen ihm/ihr alles Gute");
                            }

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
