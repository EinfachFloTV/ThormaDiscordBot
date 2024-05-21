package main.java.org.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import main.java.org.Variable;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

public class wetterbericht extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("wetter")) {
            String ort = event.getOption("ort").getAsString();
            String wetterbericht = getWeatherReport(ort);
            event.reply(wetterbericht).queue();
        }
    }
    private String getTimeString(long unixTime) {
        Date date = new Date(unixTime * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String formattedTime = sdf.format(date);
        return formattedTime;
    }
    private String getWindDirection(double degrees) {
        if (degrees > 337.5 || degrees <= 22.5) {
            return "N";
        } else if (degrees > 22.5 && degrees <= 67.5) {
            return "NE";
        } else if (degrees > 67.5 && degrees <= 112.5) {
            return "E";
        } else if (degrees > 112.5 && degrees <= 157.5) {
            return "SE";
        } else if (degrees > 157.5 && degrees <= 202.5) {
            return "S";
        } else if (degrees > 202.5 && degrees <= 247.5) {
            return "SW";
        } else if (degrees > 247.5 && degrees <= 292.5) {
            return "W";
        } else {
            return "NW";
        }
    }

    private String getWeatherReport(String ort) {
        try {
            // Öffne eine Verbindung zur OpenWeatherMap API
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + ort + "&appid=" + Variable.WEATHER_API_KEY;
            URL url = new URL(apiUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            // Lese die API-Antwort als String
            InputStream inputStream = connection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            String response = scanner.useDelimiter("\\A").next();

            // Verarbeite die JSON-Antwort, um den Wetterbericht zu extrahieren
            // Hier ist ein Beispiel, wie der Wetterbericht aussehen könnte:
            // "In Berlin beträgt die Temperatur 15.5 °C und es ist leicht bewölkt."
            JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
            JsonObject main = jsonObject.getAsJsonObject("main");
            JsonObject wind = jsonObject.getAsJsonObject("wind");
            JsonObject clouds = jsonObject.getAsJsonObject("clouds");
            JsonObject sys = jsonObject.getAsJsonObject("sys");

            DecimalFormat df = new DecimalFormat("#.#");

            double temperature = main.get("temp").getAsDouble() - 273.15;
            double feelsLike = main.get("feels_like").getAsDouble() - 273.15;
            double minTemperature = main.get("temp_min").getAsDouble() - 273.15;
            double maxTemperature = main.get("temp_max").getAsDouble() - 273.15;
            int pressure = main.get("pressure").getAsInt();
            int humidity = main.get("humidity").getAsInt();
            double windSpeed = wind.get("speed").getAsDouble();
            String windDirection = getWindDirection(wind.get("deg").getAsDouble());
            int cloudiness = clouds.get("all").getAsInt();
            String sunriseTime = getTimeString(sys.get("sunrise").getAsLong());
            String sunsetTime = getTimeString(sys.get("sunset").getAsLong());
            String temperatureString = df.format(temperature);
            String feelsLikeString = df.format(feelsLike);
            String minTemperatureString = df.format(minTemperature);
            String maxTemperatureString = df.format(maxTemperature);
            String windSpeedString = df.format(windSpeed);
            /*String description = jsonObject.getAsJsonArray("weather")
                    .get(0).getAsJsonObject().get("description").getAsString();*/
            String description = response.split("\"description\":\"")[1].split("\"")[0];

            String wetterbericht = "**Wetterbericht für " + ort + "**\n" +
                    "Beschreibung: " + description + "\n" +
                    "Temperatur: " + temperatureString + " °C\n" +
                    "Gefühlte Temperatur: " + feelsLikeString + " °C\n" +
                    "Minimale Temperatur: " + minTemperatureString + " °C\n" +
                    "Maximale Temperatur: " + maxTemperatureString + " °C\n" +
                    "Luftdruck: " + pressure + " hPa\n" +
                    "Luftfeuchtigkeit: " + humidity + "%\n" +
                    "Windgeschwindigkeit: " + windSpeedString + " km/h\n" +
                    "Windrichtung: " + windDirection + "\n" +
                    "Bewölkung: " + cloudiness + "%\n" +
                    "Sonnenaufgang: " + sunriseTime + "\n" +
                    "Sonnenuntergang: " + sunsetTime;
            return wetterbericht;
        } catch (IOException e) {
            e.printStackTrace();
            return "Es gab einen Fehler beim Abrufen des Wetterberichts für " + ort + ".";
        }
    }
}
