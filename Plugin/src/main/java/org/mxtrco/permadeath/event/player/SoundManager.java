package org.mxtrco.permadeath.event.player;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {

    public static void playDeathSounds() {
        // Reproduce un sonido a todos los jugadores
        Bukkit.getOnlinePlayers().forEach(player ->
                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, Float.MAX_VALUE, -0.1f)
        );
    }

    public static void playDelayedSound(int delayInTicks) {
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                // Reproduce un sonido a todos los jugadores
                Bukkit.getOnlinePlayers().forEach(player ->
                        player.playSound(player.getLocation(), Sound.ENTITY_SKELETON_HORSE_DEATH, Float.MAX_VALUE, 1)
                );
            }
        }.runTaskLater(org.mxtrco.permadeath.Main.getInstance(), delayInTicks);
    }

    public static void playExplosionSound(Player player) {
        // Reproducir sonido de explosión
        player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f);
    }
}

