package org.mxtrco.permadeath.event.entity;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.mxtrco.permadeath.event.items.ItemManager;

import java.util.concurrent.ThreadLocalRandom;

public class DifficultyManager2 implements Listener {

    private final NamespacedKey cubeTypeKey;
    private final NamespacedKey slimeTypeKey;
    private final JavaPlugin plugin;

    public DifficultyManager2(JavaPlugin plugin) {
        this.plugin = plugin;
        this.cubeTypeKey = new NamespacedKey(plugin, "cube_type");
        this.slimeTypeKey = new NamespacedKey(plugin, "slime_type");
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity().getWorld().getName().equalsIgnoreCase("gulag")) {
            return;
        }

        if (event.getEntity() instanceof MagmaCube) {
            MagmaCube cube = (MagmaCube) event.getEntity();
            PersistentDataContainer data = cube.getPersistentDataContainer();

            if (data.has(cubeTypeKey, PersistentDataType.STRING)) {
                return;
            }

            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL ||
                    event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG ||
                    event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.COMMAND) {

                double chance = Math.random(); // Genera un número entre 0.0 y 1.0
                if (chance < 0.15) { // 20% de probabilidad
                    setupGigaMagmacube(cube);
                    //plugin.getLogger().info("Giga MagmaCube created at: " + cube.getLocation());
                } else {
                    event.setCancelled(true);
                }
            }
        }

        if (event.getEntity() instanceof Slime) {
            Slime slime = (Slime) event.getEntity();
            PersistentDataContainer data = slime.getPersistentDataContainer();

            if (data.has(slimeTypeKey, PersistentDataType.STRING)) {
                return;
            }

            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL ||
                    event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG ||
                    event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.COMMAND) {

                double chance = Math.random(); // Genera un número entre 0.0 y 1.0
                if (chance < 0.30) { // 40% de probabilidad
                    setupGigaSlime(slime);
                    //plugin.getLogger().info("Giga Slime created at: " + slime.getLocation());
                } else {
                    event.setCancelled(true);
                }
            }
        }

        if (event.getEntity() instanceof Ghast) {
            Ghast ghast = (Ghast) event.getEntity();
            PersistentDataContainer data = ghast.getPersistentDataContainer();

            if (data.has(cubeTypeKey, PersistentDataType.STRING)) {
                return;
            }

            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL ||
                    event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG ||
                    event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.COMMAND) {
                setupDemonGhast(ghast);
                //plugin.getLogger().info("Giga Ghast created at: " + ghast.getLocation());
            }
        }
    }

    @EventHandler
    public void onSlimeSplit(SlimeSplitEvent event) {
        if (event.getEntity().getWorld().getName().equalsIgnoreCase("gulag")) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getWorld().getName().equalsIgnoreCase("gulag")) {
            return;
        }

        Entity deadEntity = event.getEntity();

        // Manejar la muerte de Magma Cubes
        if (deadEntity instanceof MagmaCube) {
            MagmaCube deadCube = (MagmaCube) deadEntity;
            handleMagmaCubeDeath(deadCube, event);
            return; // Salir después de manejar el Magma Cube
        }

        // Manejar la muerte de Slimes
        if (deadEntity instanceof Slime) {
            Slime deadSlime = (Slime) deadEntity;
            handleSlimeDeath(deadSlime, event);
            return; // Salir después de manejar el Slime
        }

        // Manejar la muerte de Ghasts
        if (deadEntity instanceof Ghast) {
            Ghast deadGhast = (Ghast) deadEntity;
            handleGhastDeath(deadGhast, event);
        }
    }

    private void handleMagmaCubeDeath(MagmaCube deadCube, EntityDeathEvent event) {
        PersistentDataContainer data = deadCube.getPersistentDataContainer();

        if (!data.has(cubeTypeKey, PersistentDataType.STRING)) {
            return;
        }

        String cubeType = data.get(cubeTypeKey, PersistentDataType.STRING);
        Location location = deadCube.getLocation();
        event.getDrops().clear();

        switch (cubeType) {
            case "GIGA":
                for (int i = 0; i < 4; i++) {
                    spawnMediumMagmacube(location);
                }
                //plugin.getLogger().info("Giga killed - Spawned 4 Medium");
                break;
            case "MEDIUM":
                for (int i = 0; i < 4; i++) {
                    spawnSmallMagmacube(location);
                }
                //plugin.getLogger().info("Medium killed - Spawned 4 Small");
                break;
            case "SMALL":
                handleSmallMagmaCubeDrops(event);
                break;
        }
    }

    private void handleSlimeDeath(Slime deadSlime, EntityDeathEvent event) {
        PersistentDataContainer data = deadSlime.getPersistentDataContainer();

        if (!data.has(slimeTypeKey, PersistentDataType.STRING)) {
            return;
        }

        String slimeType = data.get(slimeTypeKey, PersistentDataType.STRING);
        Location location = deadSlime.getLocation();
        event.getDrops().clear();

        switch (slimeType) {
            case "GIGA":
                for (int i = 0; i < 4; i++) {
                    spawnMediumSlime(location);
                }
                //plugin.getLogger().info("Giga Slime killed - Spawned 4 Medium");
                break;
            case "MEDIUM":
                for (int i = 0; i < 4; i++) {
                    spawnSmallSlime(location);
                }
                //plugin.getLogger().info("Medium Slime killed - Spawned 4 Small");
                break;
            case "SMALL":
                handleSmallSlimeDrops(event);
                break;
        }
    }

    private void handleGhastDeath(Ghast deadGhast, EntityDeathEvent event) {
        PersistentDataContainer data = deadGhast.getPersistentDataContainer();

        if (!data.has(cubeTypeKey, PersistentDataType.STRING)) {
            return;
        }

        String cubeType = data.get(cubeTypeKey, PersistentDataType.STRING);
        Location location = deadGhast.getLocation();
        event.getDrops().clear();

        if (cubeType.equals("DEMON")) {
            int randomAmount2 = ThreadLocalRandom.current().nextInt(0, 100);
            if (randomAmount2 < 10) {
                event.getDrops().add(ItemManager.casco);
            }
            //plugin.getLogger().info("Demon Ghast killed - Dropped a helmet");
        }
    }

    private void handleSmallMagmaCubeDrops(EntityDeathEvent event) {
        // Probabilidad de drop de Magma Cream (entre 1 y 2)
        int randomAmount1 = ThreadLocalRandom.current().nextInt(1, 3);
        event.getDrops().add(new ItemStack(Material.MAGMA_CREAM, randomAmount1));

        // Probabilidad del 10% de dropear botas personalizadas
        int randomChance = ThreadLocalRandom.current().nextInt(0, 100);
        if (randomChance < 3) {
            event.getDrops().add(ItemManager.botas);
            //plugin.getLogger().info("Small Magma Cube killed - Dropped botas");
        }
    }

    private void handleSmallSlimeDrops(EntityDeathEvent event) {
        int randomAmount1 = ThreadLocalRandom.current().nextInt(0, 3);
        if (randomAmount1 > 0) {
            event.getDrops().add(new ItemStack(Material.SLIME_BALL, randomAmount1));
        }
    }

    private void setupDemonGhast(Ghast ghast) {
        double health = ThreadLocalRandom.current().nextInt(40, 61);
        ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
        ghast.setHealth(health);
        ghast.setExplosionPower(ThreadLocalRandom.current().nextInt(3, 6));
        ghast.getPersistentDataContainer().set(cubeTypeKey, PersistentDataType.STRING, "DEMON");
    }

    private void setupGigaMagmacube(MagmaCube cube) {
        cube.setSize(16);
        cube.setRemoveWhenFarAway(false);
        cube.getPersistentDataContainer().set(cubeTypeKey, PersistentDataType.STRING, "GIGA");
    }

    private void setupGigaSlime(Slime slime) {
        slime.setSize(16);
        slime.setRemoveWhenFarAway(false);
        slime.getPersistentDataContainer().set(slimeTypeKey, PersistentDataType.STRING, "GIGA");
    }

    private void spawnMediumMagmacube(Location location) {
        if (location.getWorld() == null) return;

        MagmaCube cube = (MagmaCube) location.getWorld().spawnEntity(location, EntityType.MAGMA_CUBE);
        cube.setSize(8);
        cube.getPersistentDataContainer().set(cubeTypeKey, PersistentDataType.STRING, "MEDIUM");
    }

    private void spawnSmallMagmacube(Location location) {
        if (location.getWorld() == null) return;

        MagmaCube cube = (MagmaCube) location.getWorld().spawnEntity(location, EntityType.MAGMA_CUBE);
        cube.setSize(4);
        cube.getPersistentDataContainer().set(cubeTypeKey, PersistentDataType.STRING, "SMALL");
    }

    private void spawnMediumSlime(Location location) {
        if (location.getWorld() == null) return;

        Slime slime = (Slime) location.getWorld().spawnEntity(location, EntityType.SLIME);
        slime.setSize(8);
        slime.getPersistentDataContainer().set(slimeTypeKey, PersistentDataType.STRING, "MEDIUM");
    }

    private void spawnSmallSlime(Location location) {
        if (location.getWorld() == null) return;

        Slime slime = (Slime) location.getWorld().spawnEntity(location, EntityType.SLIME);
        slime.setSize(4);
        slime.getPersistentDataContainer().set(slimeTypeKey, PersistentDataType.STRING, "SMALL");
    }
}

