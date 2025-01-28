package org.mxtrco.permadeath.event.end;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mxtrco.permadeath.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;

public class EndManager implements Listener {

    private final Main main;
//    private final List<Entity> enderCreepers;
//    private final List<Entity> enderGhasts;
//    private final ArrayList<Location> alreadyExploded;
//    private final ArrayList<Enderman> invulnerable;
    private final SplittableRandom random;

    // Constantes para configuración
    private static final int ISLAND_RADIUS = 150;
    private static final int ISLAND_MIN_HEIGHT = 0;
    private static final int ISLAND_MAX_HEIGHT = 110;
    private static final double DRAGON_HEALTH = 300.0;
    private static final float TNT_EXPLOSION_RADIUS = 6.0F;

    public EndManager(Main main) {
        this.main = main;
//        this.enderCreepers = new ArrayList<>();
//        this.enderGhasts = new ArrayList<>();
//        this.alreadyExploded = new ArrayList<>();
//        this.invulnerable = new ArrayList<>();
        this.random = new SplittableRandom();

        main.getServer().getPluginManager().registerEvents(this, main);
    }

    public boolean isInEnd(Location p) {
        return p.getWorld().getName().endsWith("_the_end");
    }

    public static String format(String s) {
        return ChatColor.translateAlternateColorCodes('&', s.replace("#&", "#"));
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent e) {
        if (!isInEnd(e.getEntity().getLocation())) return;

        if (e.getEntity() instanceof TNTPrimed tnt) {
            if (tnt.getCustomName() != null && tnt.getCustomName().equalsIgnoreCase("dragontnt")) {
                e.setRadius(TNT_EXPLOSION_RADIUS);
                createExplosionEffects(tnt.getLocation());
            }
        }
    }

    private void createExplosionEffects(Location location) {
        World world = location.getWorld();
        world.spawnParticle(Particle.EXPLOSION, location, 1);
        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;

        LivingEntity entity = event.getEntity();
        if (!isInEnd(entity.getLocation()) || !(entity instanceof EnderDragon dragon)) return;

        setupDragon(dragon);
        regenerateIslandAndTowers(dragon.getLocation());
        //spawnGuardians(dragon.getLocation());

        if (main.getTask() == null) {
            Main.EndTask task = new Main.EndTask(main, dragon);
            main.setTask(task);
            task.start();
        }
    }

    private void setupDragon(EnderDragon dragon) {
        dragon.customName(Component.text("☠ PERMADEATH DEMON ☠", NamedTextColor.DARK_RED)
                .decorate(TextDecoration.BOLD));
        dragon.setCustomNameVisible(true);

        dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(DRAGON_HEALTH);
        dragon.setHealth(DRAGON_HEALTH);

        dragon.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1));
        dragon.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
    }

    private void regenerateIslandAndTowers(Location center) {
        generateMainIsland(center);
        //generateTowers(center);
        //generateDecorations(center);
    }

    private void generateMainIsland(Location center) {
        World world = center.getWorld();
        Random random = new Random();

        // Configuración de materiales con probabilidades
        Material[] materials = {
                Material.END_STONE,
                Material.END_STONE_BRICKS,
                Material.MAGENTA_STAINED_GLASS
        };
        double[] probabilities = {0.7, 0.29, 0.01}; // Probabilidades correspondientes

        // Verificar que las probabilidades sumen 1
        double sum = 0;
        for (double prob : probabilities) {
            sum += prob;
        }
        if (Math.abs(sum - 1.0) > 1e-6) {
            throw new IllegalArgumentException("Las probabilidades deben sumar 1.");
        }

        // Generar los intervalos acumulativos
        double[] cumulativeProbabilities = new double[probabilities.length];
        cumulativeProbabilities[0] = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            cumulativeProbabilities[i] = cumulativeProbabilities[i - 1] + probabilities[i];
        }

        for (int x = -ISLAND_RADIUS; x <= ISLAND_RADIUS; x++) {
            for (int z = -ISLAND_RADIUS; z <= ISLAND_RADIUS; z++) {
                for (int y = ISLAND_MIN_HEIGHT; y <= ISLAND_MAX_HEIGHT; y++) {
                    Location loc = new Location(world, x, y, z);
                    if (loc.distanceSquared(center) <= ISLAND_RADIUS * ISLAND_RADIUS) {
                        Block block = loc.getBlock();

                        // Reemplazar obsidiana por bedrock
                        if (block.getType() == Material.OBSIDIAN) {
                            block.setType(Material.BEDROCK);
                        }

                        // Reemplazar endstone con probabilidad
                        else if (block.getType() == Material.END_STONE) {
                            double randomValue = random.nextDouble(); // Valor entre 0 y 1
                            for (int i = 0; i < cumulativeProbabilities.length; i++) {
                                if (randomValue <= cumulativeProbabilities[i]) {
                                    block.setType(materials[i]);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }




//    private void generateTowers(Location center) {
//        World world = center.getWorld();
//        int[][] towerLocations = {
//                {96, 64, 0}, {-96, 64, 0},
//                {0, 64, 96}, {0, 64, -96},
//                {68, 64, 68}, {-68, 64, -68},
//                {68, 64, -68}, {-68, 64, 68}
//        };
//
//        for (int[] loc : towerLocations) {
//            generateTower(world, loc[0], loc[1], loc[2]);
//        }
//    }
//
//    private void generateTower(World world, int x, int baseY, int z) {
//        int height = random.nextInt(40) + 100;
//        for (int y = baseY; y <= height; y++) {
//            Location loc = new Location(world, x, y, z);
//            loc.getBlock().setType(Material.AIR); // Cambiado a AIR en lugar de bedrock
//        }
//    }
//
//    private void generateDecorations(Location center) {
//        // No hay decoraciones ya que la isla es de aire
//    }

//    private void spawnGuardians(Location center) {
//        World world = center.getWorld();
//        for (int i = 0; i < 4; i++) {
//            Location spawnLoc = center.clone().add(
//                    random.nextInt(20) - 10,
//                    5,
//                    random.nextInt(20) - 10
//            );
//
//            Enderman guardian = (Enderman) world.spawnEntity(spawnLoc, EntityType.ENDERMAN);
//            guardian.customName(Component.text("End Guardian", NamedTextColor.DARK_PURPLE));
//            guardian.setCustomNameVisible(true);
//            guardian.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
//            guardian.setHealth(100.0);
//            guardian.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2));
//            guardian.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1));
//
//            invulnerable.add(guardian);
//        }
//    }
}
