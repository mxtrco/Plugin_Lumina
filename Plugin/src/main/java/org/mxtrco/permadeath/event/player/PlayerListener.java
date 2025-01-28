package org.mxtrco.permadeath.event.player;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.mxtrco.permadeath.Main;
import org.mxtrco.permadeath.event.player.SoundManager;
import org.mxtrco.permadeath.event.player.StormManager;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.bukkit.Bukkit.getLogger;

public class PlayerListener implements Listener {
    // En lugar de crear una nueva instancia, obtener la referencia del Main
    private final StormManager stormManager;
    private final String webhookUrl = "https://discord.com/api/webhooks/1326719768593432677/98BIeSevKIkHWbr7t4DKdPqpPybHZMqmU_8G6WUyTApM8TuZG34Vr557kLRvdI2lqRVf"; // Cambia esta URL por la del webhook

    public PlayerListener() {
        // Obtener la instancia existente del StormManager desde Main
        this.stormManager = Main.getInstance().getStormManager(); // Necesitarás añadir este getter en Main
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Obtener al jugador y el mundo donde reaparecerá
        World respawnWorld = event.getPlayer().getWorld();

        // Configurar el punto de reaparición (si es necesario)
        Location respawnLocation = respawnWorld.getSpawnLocation();
        event.setRespawnLocation(respawnLocation);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        String playerName = player.getName();

        // Obtener la causa de la muerte
        String deathMessage = e.getDeathMessage();

        // Comprobar si el mundo es el gulag
        if (player.getWorld().getName().equalsIgnoreCase("gulag")) {
            return; // Salir si el mundo es "gulag"
        }

        // Reproduce un sonido a todos los jugadores
        //SoundManager.playDeathSounds();

        // Retraso en ticks (20 ticks = 1 segundo)
        int delayInTicks = 140; // 7 segundos de retraso
        //SoundManager.playDelayedSound(delayInTicks);

        // Avisar a todos los jugadores que se inició/extiende una tormenta
        Bukkit.broadcastMessage("\u00a7c¡Una tormenta se ha desatado debido a la muerte de " + player.getName() + "!");

        // Extender el tiempo de la tormenta usando extendStorm
        stormManager.onPlayerDeath(player.getWorld());

        // Cambiar el modo de juego a espectador inmediatamente
        player.setGameMode(GameMode.SPECTATOR);

        // Reiniciar todas las estadísticas del jugador
        player.setStatistic(Statistic.ANIMALS_BRED, 0);
        player.setStatistic(Statistic.TRADED_WITH_VILLAGER, 0);
        player.setStatistic(Statistic.DAMAGE_ABSORBED, 0);
        player.setStatistic(Statistic.DAMAGE_BLOCKED_BY_SHIELD, 0);
        player.setStatistic(Statistic.DAMAGE_DEALT, 0);
        player.setStatistic(Statistic.DAMAGE_DEALT_ABSORBED, 0);
        player.setStatistic(Statistic.DAMAGE_DEALT_RESISTED, 0);
        player.setStatistic(Statistic.DAMAGE_TAKEN, 0);
        player.setStatistic(Statistic.DAMAGE_RESISTED, 0);
        player.setStatistic(Statistic.HORSE_ONE_CM, 0);
        player.setStatistic(Statistic.CROUCH_ONE_CM, 0);
        player.setStatistic(Statistic.WALK_ONE_CM, 0);
        player.setStatistic(Statistic.FALL_ONE_CM, 0);
        player.setStatistic(Statistic.SPRINT_ONE_CM, 0);
        player.setStatistic(Statistic.BOAT_ONE_CM, 0);
        player.setStatistic(Statistic.CLIMB_ONE_CM, 0);
        player.setStatistic(Statistic.SWIM_ONE_CM, 0);
        player.setStatistic(Statistic.RAID_TRIGGER, 0);
        player.setStatistic(Statistic.RAID_WIN, 0);
        player.setStatistic(Statistic.MOB_KILLS, 0);
        player.setStatistic(Statistic.ITEM_ENCHANTED, 0);
        player.setStatistic(Statistic.FISH_CAUGHT, 0);
        player.setStatistic(Statistic.WALK_UNDER_WATER_ONE_CM, 0);
        player.setStatistic(Statistic.WALK_ON_WATER_ONE_CM, 0);
        player.setStatistic(Statistic.JUMP, 0);
        player.setStatistic(Statistic.SNEAK_TIME, 0);
        player.setStatistic(Statistic.TIME_SINCE_REST, 0);
        player.setStatistic(Statistic.PLAY_ONE_MINUTE, 0);
        player.setStatistic(Statistic.SLEEP_IN_BED, 0);

        // Ejecutar comandos adicionales
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "animation false 1 55 123");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute at @a run playsound minecraft:dedsafio ambient @p");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a actionbar {\"text\":\"" + player.getName() + " ha muerto\",\"color\":\"dark_red\"}");

        // Ejecutar la desconexión y el baneo con un retraso de 10 segundos
        Bukkit.getScheduler().runTaskLater(
                Main.getInstance(),
                () -> {
                    String playerIp = player.getAddress().getAddress().getHostAddress();
                    Bukkit.getBanList(org.bukkit.BanList.Type.IP)
                            .addBan(playerIp, "\u00a7cHas sido Permabaneado.", null, null);

                    player.kickPlayer("\u00a7cHas sido Permabaneado.");
                },
                delayInTicks + 20
        );

        // Obtener la URL del avatar del jugador
        String playerAvatarUrl = getPlayerAvatarUrl(player);

        // Enviar el mensaje a Discord con la causa de la muerte
        sendDeathMessageWithAvatar(playerName, deathMessage, playerAvatarUrl);

    }

    public String getPlayerAvatarUrl(Player player) {
        // URL de Crafatar para obtener el avatar del jugador
        return "https://minotar.net/avatar/" + player.getName() + "/100";
    }

    public void sendDeathMessageWithAvatar(String playerName, String deathMessage, String playerAvatarUrl) {
        try {
            // Crear la conexión HTTP
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // Crear el cuerpo del mensaje en formato JSON (con embed)
            String jsonMessage = "{"
                    + "\"embeds\": [{"
                    + "\"title\": \"" + playerName + " ha muerto\","
                    + "\"description\": \"" + deathMessage + "\","
                    + "\"color\": 16711680," // Color rojo
                    + "\"thumbnail\": {"
                    + "\"url\": \"" + playerAvatarUrl + "\""
                    + "}"
                    + "}]"
                    + "}";

            // Enviar el mensaje
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonMessage.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Verificar la respuesta
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                getLogger().info("Mensaje enviado a Discord con éxito.");
            } else {
                getLogger().warning("Error al enviar el mensaje a Discord: " + responseCode);
            }
        } catch (Exception e) {
            getLogger().severe("Error al enviar el mensaje a Discord.");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerTrySleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        if (stormManager.isStormActive()) {
            event.setCancelled(true);
            SoundManager.playExplosionSound(player);
            player.sendMessage("§cNo puedes dormir durante el death train.");
            player.sendMessage("§cTu tiempo de descanzo ha sido reseteado.");
            player.setStatistic(Statistic.TIME_SINCE_REST, 0);
        }
    }
}
