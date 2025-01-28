package org.mxtrco.permadeath.event.entity;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.bukkit.Bukkit.getLogger;

public class DifficultyManager3 implements Listener {

    private final JavaPlugin plugin;
    private final Random rand = new Random();
    private final int TELEPORT_RADIUS = 16; // Radio máximo de teletransportación
    private static final double EXPLOSION_PROBABILITY = 0.25; // 25% de probabilidad
    private static final double SILVERFISH_SPAWN_PROBABILITY = 0.01;
    private final Set<Player> playersInNether = new HashSet<>();
    private final Set<Player> playersWithFireTask = new HashSet<>();


    public DifficultyManager3(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreatureSpawn(EntitySpawnEvent event) {
        if (event.getEntity().getWorld().getName().equalsIgnoreCase("gulag")) {
            return;
        }

        switch (event.getEntityType()) {
            case SQUID:
                handleSquidSpawn(event);
                break;
            case ZOMBIE:
                handleZombieToGhastSpawn(event);
                break;
            case CREEPER:
                handleCreeperSpawn(event);
                break;
            case BAT:
                handleBatToBlazeSpawn(event);
                break;
            case PILLAGER:
                handlePillagerSpawn(event);
                break;
            case VINDICATOR:
                handleVindicatorSpawn(event);
                break;
        }
    }

    private void handlePillagerSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Pillager) {
            Pillager pillager = (Pillager) event.getEntity();

            // Crear y ponerle la armadura al saqueador
            ItemStack helmet = new ItemStack(Material.IRON_HELMET);
            ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
            ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
            ItemStack boots = new ItemStack(Material.IRON_BOOTS);

            // También puedes agregarles un poco de encantamientos si lo deseas
            helmet.addEnchantment(Enchantment.PROTECTION, 2);
            chestplate.addEnchantment(Enchantment.PROTECTION, 2);
            leggings.addEnchantment(Enchantment.PROTECTION, 2);
            boots.addEnchantment(Enchantment.PROTECTION, 2);

            // Equipar al saqueador con la armadura
            pillager.getEquipment().setHelmet(helmet);
            pillager.getEquipment().setChestplate(chestplate);
            pillager.getEquipment().setLeggings(leggings);
            pillager.getEquipment().setBoots(boots);
        }
    }

    @EventHandler
    public void handleVindicatorSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Vindicator) {
            Vindicator vindicator = (Vindicator) event.getEntity();

            // Crear y ponerle la armadura al Vindicator
            ItemStack helmet = new ItemStack(Material.IRON_HELMET);
            ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
            ItemStack leggings = new ItemStack(Material.IRON_LEGGINGS);
            ItemStack boots = new ItemStack(Material.IRON_BOOTS);

            // También puedes agregarles un poco de encantamientos si lo deseas
            helmet.addEnchantment(Enchantment.PROTECTION, 2);
            chestplate.addEnchantment(Enchantment.PROTECTION, 2);
            leggings.addEnchantment(Enchantment.PROTECTION, 2);
            boots.addEnchantment(Enchantment.PROTECTION, 2);

            // Equipar al Vindicator con la armadura
            vindicator.getEquipment().setHelmet(helmet);
            vindicator.getEquipment().setChestplate(chestplate);
            vindicator.getEquipment().setLeggings(leggings);
            vindicator.getEquipment().setBoots(boots);
        }
    }


    private void handleSquidSpawn(EntitySpawnEvent event) {
        int probability = rand.nextInt(100);

        if (probability < 45) {
            event.getEntity().remove();
            event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.GUARDIAN);
        } else if (probability < 70) {
            event.getEntity().remove();
            event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.ELDER_GUARDIAN);
        }
    }

    private void handleZombieToGhastSpawn(EntitySpawnEvent event) {
        int probability = rand.nextInt(100);

        if (probability < 25) {
            Location spawnLoc = event.getLocation().clone().add(0, 10, 0);

            if (hasEnoughSpace(spawnLoc)) {
                event.setCancelled(true);
                event.getEntity().getWorld().spawnEntity(spawnLoc, EntityType.GHAST);
            }
        }
    }

    private void handleBatToBlazeSpawn(EntitySpawnEvent event) {
        event.setCancelled(true);
        Location spawnLoc = event.getLocation();

        if (hasEnoughSpace(spawnLoc)) {
            event.getEntity().getWorld().spawnEntity(spawnLoc, EntityType.BLAZE);
        }
    }

    private void handleCreeperSpawn(EntitySpawnEvent event) {
        Creeper creeper = (Creeper) event.getEntity();
        creeper.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        creeper.setPowered(true);
        startTeleportBehavior(creeper);
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        // Verificamos si la entidad es un creeper
        if (event.getEntity() instanceof Creeper) {
            Creeper creeper = (Creeper) event.getEntity();

            // Eliminar el efecto de invisibilidad antes de la explosión
            if (creeper.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                creeper.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }
    }

    private boolean hasEnoughSpace(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;

        // Verificar que el bloque base (uno debajo de loc) sea sólido
        Block baseBlock = loc.clone().subtract(0, 1, 0).getBlock();
        if (!baseBlock.getType().isSolid()) {
            return false;
        }

        // Verificar que loc.y (posición actual) y loc.y + 1 estén libres
        Block blockAtY = loc.getBlock();
        Block blockAtYPlus1 = loc.clone().add(0, 1, 0).getBlock();

        return blockAtY.isEmpty() && blockAtYPlus1.isEmpty();
    }



    private void startTeleportBehavior(Creeper creeper) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (creeper == null || !creeper.isValid() || creeper.isDead()) {
                        this.cancel();
                        return;
                    }

                    // 15% probability for teleport
                    if (rand.nextInt(100) < 15) {
                        teleportCreeper(creeper);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 200L, 200L);
    }

    private void teleportCreeper(Creeper creeper) {
        Location currentLoc = creeper.getLocation();

        Location newLoc = null;
        int attempts = 0;
        int maxAttempts = 1;

        while (attempts < maxAttempts && newLoc == null) {
            try {
                // Calcular nueva posición X y Z
                double x = currentLoc.getX() + (rand.nextDouble() * TELEPORT_RADIUS * 2) - TELEPORT_RADIUS;
                double z = currentLoc.getZ() + (rand.nextDouble() * TELEPORT_RADIUS * 2) - TELEPORT_RADIUS;

                // Obtener el bloque más alto en esa posición
                int highestY = currentLoc.getWorld().getHighestBlockYAt((int) x, (int) z);

                // Validar si la altura está dentro del rango permitido
                if (highestY < -60 || highestY > 298) {
                    continue; // Saltar este intento
                }

                // Calcular la nueva ubicación
                Location potential = new Location(currentLoc.getWorld(), x, highestY + 1, z);

                // Verificar si hay espacio suficiente
                if (hasEnoughSpace(potential)) {
                    newLoc = potential;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            attempts++;
        }

        if (newLoc != null && newLoc.getWorld() != null) {
            creeper.getWorld().spawnParticle(Particle.PORTAL, creeper.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);
            creeper.teleport(newLoc);
            creeper.getWorld().spawnParticle(Particle.PORTAL, newLoc, 50, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Material bedMaterial = event.getBed().getType();

        // Comprobar si el bloque es una cama (cualquier color)
        if (isBedMaterial(bedMaterial)) {
            Random random = new Random();
            if (random.nextDouble() < EXPLOSION_PROBABILITY) {
                // Hacer que la cama explote
                Location bedLocation = event.getBed().getLocation();
                bedLocation.getWorld().createExplosion(bedLocation, 4.0F); // Potencia de la explosión
                event.getPlayer().sendMessage("¡La cama explotó mientras intentabas dormir!");
                event.setCancelled(true); // Cancela el evento de dormir
            }
        }
    }

    private boolean isBedMaterial(Material material) {
        // Comprobar si el nombre del material termina con "_BED"
        return material != null && material.name().endsWith("_BED");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Random random = new Random();
        if (random.nextDouble() < SILVERFISH_SPAWN_PROBABILITY) {
            Location blockLocation = event.getBlock().getLocation();
            Location centeredLocation = blockLocation.add(0.5, 0.5, 0.5);
            Silverfish silverfish = (Silverfish) centeredLocation.getWorld().spawnEntity(centeredLocation, EntityType.SILVERFISH);
            silverfish.setRemoveWhenFarAway(false);
            silverfish.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 3, false, false));
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Verificar si el jugador está en el Nether
        if (player.getWorld().getName().equals("world_nether")) {
            // Verificar si el jugador ya tiene la tarea en curso
            if (!playersWithFireTask.contains(player)) {
                // Aplicar un efecto de resistencia al fuego permanente al jugador
                //player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));

                // Iniciar un Runnable que lo mantenga quemado
                BukkitRunnable fireTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.getWorld().getName().equals("world_nether")) {
                            player.setFireTicks(100); // Aplica fuego constantemente
                        } else {
                            playersWithFireTask.remove(player); // Remover del conjunto si ya no está en el Nether
                            this.cancel(); // Detener el runnable
                        }
                    }
                };

                // Registrar la tarea para el jugador
                fireTask.runTaskTimer(plugin, 0L, 20L);
                playersWithFireTask.add(player);
            }
        } else {
            // Si el jugador sale del Nether, eliminar de la lista
            playersWithFireTask.remove(player);
        }
    }
    // Variable para almacenar el BukkitRunnable
    private BukkitRunnable spiderTask;

    @EventHandler
    public void onSpiderDeath(EntityDeathEvent event) {
        // Si la entidad muerta es una araña, cancelamos el Runnable
        if (event.getEntity() instanceof Spider) {
            if (spiderTask != null) {
                spiderTask.cancel();
            }
        }
    }

    @EventHandler
    public void onSpiderTargetPlayer(EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof Spider) {
            Spider spider = (Spider) event.getEntity();
            if (event.getTarget() instanceof Player) {
                Player player = (Player) event.getTarget();

                // Iniciar un Runnable que se ejecute periódicamente
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Verifica si el objetivo de la araña es nulo o si la araña está muerta
                        if (spider.getTarget() == null || spider.isDead()) {
                            cancel(); // Detener el runnable si la araña no tiene objetivo o está muerta
                            return;
                        }

                        // Verifica si el jugador sigue siendo el objetivo de la araña
                        if (!spider.getTarget().equals(player)) {
                            cancel(); // Detener el runnable si el jugador ya no es el objetivo
                            return;
                        }

                        // Probabilidad de 30% (puedes ajustarlo como prefieras)
                        if (Math.random() < 0.3) {
                            // Coloca una telaraña en el bloque donde el jugador está parado
                            placeWebOnPlayerBlock(player);

                            // Reproducir el sonido de la araña al jugador
                            player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.0F, 1.0F);
                        }
                    }
                }.runTaskTimer(plugin, 0L, 100L); // Ejecutar cada 5 segundos (100 ticks)
            }
        }
    }

    // Método para colocar telaraña en el bloque en el que está el jugador
    private void placeWebOnPlayerBlock(Player player) {
        // Crear un Runnable para colocar telarañas
        new BukkitRunnable() {
            @Override
            public void run() {
                // Obtener el bloque donde el jugador está parado
                Block playerBlock = player.getLocation().getBlock();

                // Colocar una telaraña (cobweb) en el bloque donde está el jugador
                playerBlock.setType(Material.COBWEB);

                // Opcional: Eliminar la telaraña después de un cierto tiempo (ejemplo, 5 segundos)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        playerBlock.setType(Material.AIR); // Eliminar la telaraña
                    }
                }.runTaskLater(plugin, 200L); // 100L = 5 segundos
            }
        }.runTask(plugin); // Ejecutar una vez, en el siguiente tick
    }

    @EventHandler
    public void onZombieHitPlayer(EntityDamageByEntityEvent event) {
        // Verificar si el atacante es un Zombi
        if (event.getDamager() instanceof Zombie) {
            // Verificar si el objetivo es un jugador
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                Zombie zombie = (Zombie) event.getDamager();

                // Probabilidad de 10% de quitar el objeto en la mano principal
                if (Math.random() < 0.1) {
                    ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                    if (itemInMainHand != null && !itemInMainHand.getType().isAir()) {
                        player.getInventory().setItemInMainHand(null);
                        player.getWorld().dropItem(player.getLocation(), itemInMainHand);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        // Verificar si la entidad que ha aparecido es un Warden o un Elder Guardian
        if (event.getEntity() instanceof Warden || event.getEntity() instanceof ElderGuardian) {
            // Asegurarnos de que la entidad es un mob válido
            LivingEntity entity = (LivingEntity) event.getEntity();

            // Crear un Totem of Undying
            ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);

            // Equipar el Totem en la mano secundaria del mob
            equipTotemInOffHand(entity, totem);
        }
    }

    // Método para equipar el Totem en la mano secundaria del mob
    private void equipTotemInOffHand(LivingEntity entity, ItemStack totem) {
        if (entity instanceof Player) {
            // Si es un jugador, ponemos el Totem en la mano secundaria
            Player player = (Player) entity;
            player.getInventory().setItemInOffHand(totem);
        } else {
            // Para mobs, asignamos el Totem a la mano secundaria del mob
            entity.getEquipment().setItemInOffHand(totem);
        }
    }

    @EventHandler
    public void onPlayerConsumeFood(FoodLevelChangeEvent event) {
        // Verificar si la entidad es un jugador
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Configurar la probabilidad (porcentaje: 1-100)
            int probability = 20; // Por ejemplo, 20% de probabilidad

            // Generar un número aleatorio
            int randomChance = new Random().nextInt(100) + 1; // 1-100

            // Si el número aleatorio es menor o igual a la probabilidad configurada
            if (randomChance <= probability) {
                // Dar debuff de hambre (puedes ajustar la intensidad según tus necesidades)
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 600, 1)); // 5 segundos con debuff de hambre
            }
        }
    }

}
