package main.java.org.Thorma90NetworkDC;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;

public class welcomeandleave extends ListenerAdapter {

    private final String[] randomwelcomemessageonImage = {
            "Willkommen!",
            "Hey!",
            "Servus!",
            "Moin!"
    };
    private final String[] randomwelcomemessage = {
            "ist hier.",
            "ist gerade gekommen.",
            "ist auf dem Server gelandet.",
            "ist gerade aufgetaucht.",
            "ist gerade erscheinen."
    };
    private final String[] randomleavessageonImage = {
            "Tschüss!",
            "Bye!"
    };
    private final String[] randomleavemessage = {
            "ist von uns gegangen...",
            "hat uns verlassen..."
    };

    private final String ChannelWelcomeandLeave = "1249011368175337532";
    private final String backgroundImage = "https://media.istockphoto.com/id/1442805032/de/foto/blauer-rauch-oder-dunstiger-nebel-auf-schwarzem-hintergrund-hellblaue-wolkige-textur-elegantes.jpg?s=612x612&w=0&k=20&c=cz9UCpVkAhZXPoirWUZ5cvubrCB7I47IKEyuddWLzxU=";

    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if(!event.getMember().getUser().isBot()) {

            TextChannel tc = event.getGuild().getTextChannelById(ChannelWelcomeandLeave);

            List<Member> members = event.getGuild().getMembers();
            int humanCount = 0;

            for (Member member : members) {
                if (!member.getUser().isBot()) {
                    humanCount++;
                }
            }

            try {
                Random random = new Random();
                int randomIndexWelcomeonimage = random.nextInt(randomleavessageonImage.length);
                BufferedImage background = ImageIO.read(new URL(backgroundImage));

                BufferedImage avatar = ImageIO.read(new URL(event.getMember().getUser().getEffectiveAvatarUrl()));
                int targetWidth = 140; // Smaller size
                int targetHeight = 140; // Ensuring it stays square for the circular crop

                // Resize the avatar
                avatar = Scalr.resize(avatar, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, targetWidth, targetHeight);

                // Create an avatar image with rounded corners
                BufferedImage roundedAvatar = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = roundedAvatar.createGraphics();
                g2.setComposite(AlphaComposite.Src);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, targetWidth, targetHeight, 20, 20)); // 20 is the arc width and height for rounded corners
                g2.setComposite(AlphaComposite.SrcAtop);
                g2.drawImage(avatar, 0, 0, targetWidth, targetHeight, null);
                g2.dispose();


                // Draw the background and avatar
                Graphics2D graphics = background.createGraphics();
                graphics.setColor(Color.WHITE);
                graphics.setFont(new Font("Impact", Font.PLAIN, 40));
                graphics.drawString(randomwelcomemessageonImage[randomIndexWelcomeonimage], 200, 100);
                graphics.drawString(event.getMember().getUser().getName(), 200, 145);
                graphics.drawImage(roundedAvatar, 30, 35, null);
                graphics.dispose();

                File output = new File("welcomeuser.png");
                ImageIO.write(background, "png", output);

                Random random2 = new Random();
                int randomIndex = random2.nextInt(randomwelcomemessage.length);


                tc.sendMessage(event.getMember().getAsMention() + " " + randomwelcomemessage[randomIndex] + " ^^ \n" +
                       // "Lies dir unbedingt die Regeln durch und verifiziere dich! <#1248927469558304931> <#1248720361508569191>\n" +
                        "Wir sind jetzt `" + humanCount + "` Nutzer!").addFiles(FileUpload.fromData(output, "welcomeuser.png")).queue();


            } catch (IOException e) {
                e.printStackTrace();
            }
        } /*else {
            event.getMember().getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(Variable.BotRole_ID)).queue();
        }*/
    }

    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        if(!event.getMember().getUser().isBot()) {
            TextChannel tc = event.getGuild().getTextChannelById(ChannelWelcomeandLeave);
            List<Member> members = event.getGuild().getMembers();
            int humanCount = 0;

            for (Member member : members) {
                if (!member.getUser().isBot()) {
                    humanCount++;
                }
            }

            try {
                Random random = new Random();
                int randomIndexLeaveonimage = random.nextInt(randomleavessageonImage.length);
                BufferedImage background = ImageIO.read(new URL(backgroundImage));

                BufferedImage avatar = ImageIO.read(new URL(event.getMember().getUser().getEffectiveAvatarUrl()));
                int targetWidth = 140; // Smaller size
                int targetHeight = 140; // Ensuring it stays square for the circular crop

                // Resize the avatar
                avatar = Scalr.resize(avatar, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, targetWidth, targetHeight);

                // Create an avatar image with rounded corners
                BufferedImage roundedAvatar = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = roundedAvatar.createGraphics();
                g2.setComposite(AlphaComposite.Src);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, targetWidth, targetHeight, 20, 20)); // 20 is the arc width and height for rounded corners
                g2.setComposite(AlphaComposite.SrcAtop);
                g2.drawImage(avatar, 0, 0, targetWidth, targetHeight, null);
                g2.dispose();


                // Draw the background and avatar
                Graphics2D graphics = background.createGraphics();
                graphics.setColor(Color.WHITE);
                graphics.setFont(new Font("Impact", Font.PLAIN, 40));
                graphics.drawString(randomleavessageonImage[randomIndexLeaveonimage], 200, 100);
                graphics.drawString(event.getMember().getUser().getName(), 200, 145);
                graphics.drawImage(roundedAvatar, 30, 35, null);
                graphics.dispose();

                File output = new File("leaveuser.png");
                ImageIO.write(background, "png", output);

                Random random2 = new Random();
                int randomIndex = random2.nextInt(randomleavemessage.length);


                tc.sendMessage(event.getMember().getUser().getName()  + " " + randomleavemessage[randomIndex] + "\n" + "Wir sind jetzt nur noch `" + humanCount + "` Nutzer!").addFiles(FileUpload.fromData(output, "leaveuser.png")).queue();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
