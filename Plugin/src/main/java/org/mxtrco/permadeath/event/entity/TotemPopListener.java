package org.mxtrco.permadeath.event.entity;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class TotemPopListener implements Listener {

    private static final Random RANDOM = new Random();
    private static final int PROBABILIDAD_EXITO = 95;
    private final String webhookUrl = "https://discord.com/api/webhooks/1326719768593432677/98BIeSevKIkHWbr7t4DKdPqpPybHZMqmU_8G6WUyTApM8TuZG34Vr557kLRvdI2lqRVf";

    @EventHandler
    public void onPop(EntityResurrectEvent event) {
        Entity entity = event.getEntity();

        // Comprobar si el mundo es el Overworld
        if (entity.getWorld().getName().equalsIgnoreCase("gulag")) {
            return;
        }

        if (entity instanceof Player) {
            Player player = (Player) entity;

            // Verificar si el jugador tiene un tótem en alguna de las manos
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            ItemStack offHandItem = player.getInventory().getItemInOffHand();

            if (!mainHandItem.getType().equals(Material.TOTEM_OF_UNDYING) &&
                    !offHandItem.getType().equals(Material.TOTEM_OF_UNDYING)) {
                return;
            }

            // Determinar si el tótem tiene éxito
            int probabilidad = RANDOM.nextInt(100) + 1;
            if (probabilidad > PROBABILIDAD_EXITO) {
                player.sendMessage("§c¡Tu tótem ha fallado!");
                Bukkit.broadcastMessage("§c¡" + player.getName() +
                        " ha usado un tótem con probabilidad: " + probabilidad + " > " + PROBABILIDAD_EXITO);
                event.setCancelled(true);

                // Enviar mensaje a Discord (Tótem fallido)
                sendDiscordMessage(player.getName(), false, probabilidad);
            } else {
                Bukkit.broadcastMessage("§7" + player.getName() +
                        " ha usado un tótem con probabilidad: " + probabilidad + " <= " + PROBABILIDAD_EXITO);

                // Enviar mensaje a Discord (Tótem exitoso)
                sendDiscordMessage(player.getName(), true, probabilidad);
            }
        }
    }

    private void sendDiscordMessage(String playerName, boolean success, int probability) {
        try {
            // Crear el JSON para el mensaje de Discord
            String json = "";

            if (success) {
                json = "{" +
                        "\"embeds\": [" +
                        "{" +
                        "\"title\": \"🎉 Tótem Usado con Éxito\"," +
                        "\"description\": \"" + playerName + " ha usado un tótem con éxito (" + probability + " <= " + PROBABILIDAD_EXITO + ").\"," +
                        "\"color\": 3066993," +
                        "\"footer\": {\"text\": \"Sistema de Eventos\"}," +
                        "\"timestamp\": \"" + java.time.Instant.now() + "\"" +
                        "}" +
                        "]}";
            } else {
                json = "{" +
                        "\"embeds\": [" +
                        "{" +
                        "\"title\": \"⚠️ Tótem Fallido\"," +
                        "\"description\": \"" + playerName + " intentó usar un tótem, pero falló (" + probability + " > " + PROBABILIDAD_EXITO + ").\"," +
                        "\"color\": 15158332," +
                        "\"footer\": {\"text\": \"Sistema de Eventos\"}," +
                        "\"timestamp\": \"" + java.time.Instant.now() + "\"" +
                        "}" +
                        "]}";
            }

            // Enviar el mensaje al webhook de Discord
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(json.getBytes());
            outputStream.flush();
            outputStream.close();

            connection.getResponseCode(); // Esto asegura que se envíe la solicitud
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}