package ru.partyislife;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("tickets.json")), StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(content);
            JSONArray tickets = jsonObject.getJSONArray("tickets");

            Map<String, Long> minFlightTimes = new HashMap<>();
            List<Double> prices = new ArrayList<>();

            tickets.forEach(ticket -> {
                JSONObject ticketJson = (JSONObject) ticket;
                if (ticketJson.getString("origin").equals("VVO") &&
                    ticketJson.getString("destination").equals("TLV")) {
                    String carrier = ticketJson.getString("carrier");
                    String departureTime = ticketJson.getString("departure_time");
                    String arrivalTime = ticketJson.getString("arrival_time");
                    long duration = ChronoUnit.MINUTES.between(
                            LocalTime.parse(departureTime, DateTimeFormatter.ofPattern("H:mm")),
                            LocalTime.parse(arrivalTime, DateTimeFormatter.ofPattern("H:mm")));
                    minFlightTimes.putIfAbsent(carrier, duration);
                    minFlightTimes.computeIfPresent(carrier, (k, v) -> Math.min(v, duration));
                    prices.add(ticketJson.getDouble("price"));
                }
            });
            System.out.println("Minimum flight times per carrier:");
            minFlightTimes.forEach(Main::print);
            if (!prices.isEmpty()) {
                double averagePrice = prices.stream().mapToDouble(v -> v).average().orElse(0);
                double medianPrice = getMedian(prices);
                System.out.println("Average Price: " + averagePrice);
                System.out.println("Median Price: " + medianPrice);
                System.out.println("Difference: " + Math.abs(averagePrice - medianPrice));
            } else {
                System.out.println("No ticket prices available for the route Vladivostok - Tel Aviv.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static double getMedian(List<Double> prices) {
        Collections.sort(prices);
        if (prices.size() % 2 == 0) {
            return (prices.get(prices.size() / 2 - 1) + prices.get(prices.size() / 2)) / 2;
        } else {
            return prices.get(prices.size() / 2);
        }
    }

    private static void print(String s, Long v){
        System.out.println("Carrier: " + s + ", Min Time: " + v + " minutes");
    }
}
