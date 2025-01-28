package org.mxtrco.permadeath.event.entity;

import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class CreeperRandomTp implements Listener {
    private static final double RANDOM_TP_CREEPER_CHANCE = 0.20; // 20% chance
    private final Random random = new Random();
    private final Plugin plugin;

    public CreeperRandomTp(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();

        // Comprobar si el mundo es el Overworld
        if (entity.getWorld().getName().equalsIgnoreCase("gulag")) {
            return; // Salir si el mundo no es "world"
        }

        if (entity instanceof Creeper) {
            Creeper creeper = (Creeper) entity;

            if (random.nextDouble() < RANDOM_TP_CREEPER_CHANCE) {
                // Asignar metadata al Creeper
                creeper.setMetadata("RandomTpCreeper", new FixedMetadataValue(plugin, true));
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Creeper) {
            Creeper creeper = (Creeper) entity;

            if (creeper.hasMetadata("RandomTpCreeper")) {

                // Permitir la explosión visual pero sin destruir bloques
                event.blockList().clear();
                event.setYield(0);

                // Teletransportar a todas las entidades cercanas
                Location creeperLocation = creeper.getLocation();
                creeper.getWorld().getNearbyEntities(creeperLocation, 3, 3, 3).forEach(nearbyEntity -> {
                    if (nearbyEntity instanceof LivingEntity && nearbyEntity != creeper) {
                        Location randomLocation = getRandomLocation(creeperLocation, 500);
                        if (randomLocation != null) {
                            // Si es un jugador, aplicar efectos especiales
                            if (nearbyEntity instanceof Player) {
                                Player player = (Player) nearbyEntity;
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 800, 0));
                                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0));
                                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 800, 0));
                                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 800, 0));
                                player.sendMessage("¡Un CreeperRandomTp te ha teletransportado 500 bloques a la redonda!");
                            }
                            // Teletransportar a la entidad
                            nearbyEntity.teleport(randomLocation);
                        }
                    }
                });
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Creeper) {
            Creeper creeper = (Creeper) damager;
            if (creeper.hasMetadata("RandomTpCreeper")) {
                event.setCancelled(true);
            }
        }
    }

    private Location getRandomLocation(Location origin, int radius) {
        int worldMinY = origin.getWorld().getMinHeight();
        int worldMaxY = origin.getWorld().getMaxHeight();
        int x = origin.getBlockX() + random.nextInt(radius * 2) - radius;
        int z = origin.getBlockZ() + random.nextInt(radius * 2) - radius;
        int y = random.nextInt(worldMaxY - worldMinY) + worldMinY;
        return new Location(origin.getWorld(), x + 0.5, y, z + 0.5);
    }
}
