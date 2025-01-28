package org.mxtrco.permadeath;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.mxtrco.permadeath.event.commands.MainCommand;
import org.mxtrco.permadeath.event.entity.*;
import org.mxtrco.permadeath.event.items.ItemManager;
import org.mxtrco.permadeath.event.player.PlayerListener;
import org.mxtrco.permadeath.event.player.StormManager;
import org.mxtrco.permadeath.event.end.EndManager;
import org.mxtrco.permadeath.event.end.DemonPhase;

import org.bukkit.attribute.Attribute;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin {

    private static Main instance;
    private StormManager stormManager;
    private DifficultyManager difficultyManager;
    private MobDuplicator mobDuplicator;
    private TotemPopListener totemPopListener;
    private ArmorDropHandler armorDropHandler;
    private DifficultyManager2 difficultyManager2;
    private CreeperRandomTp creeperRandomTp;
    private LittleZombies littleZombies;
    private PhantomMount phantomMount;
    private SkeletonClases skeletonClases;
    private DifficultyManager3 difficultyManager3;


    private final boolean[] difficultyStates = new boolean[14];
    private EndTask currentTask;

    public static boolean DEBUG = true;

    public static Main getInstance() {
        return instance;
    }

    public DifficultyManager getDifficultyManager() {
        return difficultyManager;
    }

    public MobDuplicator getMobDuplicator() {
        return mobDuplicator;
    }

    public StormManager getStormManager() {
        return stormManager;
    }

    public EndTask getTask() {
        return currentTask;
    }

    public void setTask(EndTask endTask) {
        if (currentTask != null) {
            currentTask.cancel();
        }
        this.currentTask = endTask;
    }

    @Override
    public void onEnable() {
        instance = this;
        ItemManager itemManager = ItemManager.getInstance(this);
        getServer().getPluginManager().registerEvents(itemManager, this);
        this.armorDropHandler = new ArmorDropHandler(ItemManager.getInstance(this));

        stormManager = new StormManager(this);
        difficultyManager = new DifficultyManager();
        mobDuplicator = new MobDuplicator(this);
        totemPopListener = new TotemPopListener();
        difficultyManager2 = new DifficultyManager2(this);
        creeperRandomTp = new CreeperRandomTp(this);
        littleZombies = new LittleZombies(this);
        phantomMount = new PhantomMount(this);
        skeletonClases = new SkeletonClases(this);
        difficultyManager3 = new DifficultyManager3(this);


        EndManager endManager = new EndManager(this);

        loadListenersState();
        stormManager.loadStormState();
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(endManager, this);

        // Registrar comandos
        registerCommands();

        if (DEBUG) {
            getLogger().info("Plugin habilitado en modo depuración.");
        }
    }

    @Override
    public void onDisable() {
        // Cancelar tareas activas
        if (currentTask != null) {
            currentTask.cancel();
        }
        HandlerList.unregisterAll(this);
        getLogger().info("Plugin deshabilitado correctamente.");
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("permadeath")).setExecutor(new MainCommand());
    }

    private void loadListenersState() {
        FileConfiguration config = getConfig();

        for (int i = 1; i <= 14; i++) {
            boolean isActive = config.getBoolean("Difficulty." + i + ".Active", false);
            difficultyStates[i - 1] = isActive;
            setDifficultyActive(isActive, i);
        }
    }

    public void setDifficultyActive(boolean active, int difficulty) {
        int index = difficulty - 1;

        // Actualizar estado
        difficultyStates[index] = active;

        // Manejar eventos según dificultad
        if (difficulty == 1) {
            HandlerList.unregisterAll(difficultyManager);
            HandlerList.unregisterAll(creeperRandomTp);
            if (active) {
                getServer().getPluginManager().registerEvents(difficultyManager, this);
                getServer().getPluginManager().registerEvents(creeperRandomTp, this);
            }
        } else if (difficulty == 2) {
            HandlerList.unregisterAll(mobDuplicator);
            if (active) {
                getServer().getPluginManager().registerEvents(mobDuplicator, this);
            }
        } else if (difficulty == 3) {
            HandlerList.unregisterAll(totemPopListener);
            HandlerList.unregisterAll(armorDropHandler);
            HandlerList.unregisterAll(difficultyManager2);
            HandlerList.unregisterAll(littleZombies);
            HandlerList.unregisterAll(phantomMount);
            HandlerList.unregisterAll(skeletonClases);
            if (active) {
                getServer().getPluginManager().registerEvents(totemPopListener, this);
                getServer().getPluginManager().registerEvents(armorDropHandler, this);
                getServer().getPluginManager().registerEvents(difficultyManager2, this);
                getServer().getPluginManager().registerEvents(littleZombies, this);
                getServer().getPluginManager().registerEvents(phantomMount, this);
                getServer().getPluginManager().registerEvents(skeletonClases, this);
            }
        } else if (difficulty == 4) {
            HandlerList.unregisterAll(difficultyManager3);
            if (active) {
                getServer().getPluginManager().registerEvents(difficultyManager3, this);
            }
        }

        // Guardar estado en configuración
        FileConfiguration config = getConfig();
        config.set("Difficulty." + difficulty + ".Active", active);
        saveConfig();

        if (DEBUG) {
            getLogger().info("Dificultad " + difficulty + " establecida a: " + active);
        }
    }

    public boolean areDifficultyActive(int difficulty) {
        int index = difficulty - 1;
        if (index >= 0 && index < difficultyStates.length) {
            return difficultyStates[index];
        }
        return false;
    }

    public static class TNTTask extends BukkitRunnable {
        private final EnderDragon dragon;
        private static final double RADIUS = 5.0; // Radio del círculo donde se colocarán los TNTs
        private static final int TNT_COUNT = 12; // Número de TNTs en el círculo (se puede ajustar)

        public TNTTask(EnderDragon dragon) {
            this.dragon = dragon;
        }

        @Override
        public void run() {
            // Verificar si el dragón está muerto, y cancelar la tarea si es así
            if (dragon.isDead()) {
                cancel();
                return;
            }

            // Lanza los TNTs en un círculo
            triggerExplosionsInCircle(dragon.getLocation());
        }

        // Método para lanzar TNTs en un círculo alrededor de la ubicación dada
        private void triggerExplosionsInCircle(Location center) {
            double angleStep = Math.PI * 2 / TNT_COUNT; // Ángulo entre cada TNT (en radianes)

            // Lanzar TNTs en cada posición calculada en el círculo
            for (int i = 0; i < TNT_COUNT; i++) {
                double angle = i * angleStep;
                double x = center.getX() + RADIUS * Math.cos(angle); // Coordenada X
                double z = center.getZ() + RADIUS * Math.sin(angle); // Coordenada Z
                Location tntLocation = new Location(center.getWorld(), x, center.getY(), z);

                // Crear y encender el TNT
                TNTPrimed tnt = tntLocation.getWorld().spawn(tntLocation, TNTPrimed.class);
                tnt.setCustomName("dragontnt"); // Personaliza el TNT con un nombre especial
                tnt.setFuseTicks(80); // Tiempo de fuse de 4 segundos
                tnt.setFireTicks(80); // Asegura que la TNT se encienda
                tnt.setYield(15.0F); // Mayor radio de explosión
            }
        }
    }

    public static class EndTask extends BukkitRunnable {
        private final Main main;
        private final EnderDragon dragon;
        private DemonPhase currentPhase;
        private static final double RADIUS = 5.0; // Radio del círculo donde se colocarán los TNTs
        private static final int TNT_COUNT = 10; // Número de TNTs en el círculo (se puede ajustar)
        private TNTTask tntTask;  // Referencia a la tarea TNTTask

        public EndTask(Main main, EnderDragon dragon) {
            this.main = main;
            this.dragon = dragon;
            this.currentPhase = DemonPhase.NORMAL; // Fase inicial
        }

        // Método para iniciar la tarea repetitiva
        public void start() {
            // Ejecutar la tarea cada 20 ticks (1 segundo)
            this.runTaskTimer(main, 0L, 20L);  // 1 segundo
        }

        @Override
        public void run() {
            // Verificar si el dragón está muerto
            if (dragon.isDead()) {
                cancel(); // Detener la tarea si el dragón está muerto
                return; // Salir del método para no ejecutar más código
            }

            // Ejemplo de cómo cambiar la fase del dragón
            switch (currentPhase) {
                case NORMAL:
                    // Lanza los TNTs en un círculo, si no ha sido iniciado el TNTTask
                    if (tntTask == null) {
                        tntTask = new TNTTask(dragon);
                        tntTask.runTaskTimer(main, 0L, 1200L); // Ejecutar cada 1 minuto (60 segundos)
                    }
                    break;
                case ENRAGED:
                    // Cambia las características del dragón cuando está en fase ENRAGED
                    dragon.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3); // Más rápido
                    break;
                case DEAD:
                    // Si la fase es DEAD, el dragón muere
                    break;
            }

            // Aquí puedes agregar más lógica para cambiar la fase si es necesario
            if (dragon.getHealth() < 50 && currentPhase != DemonPhase.ENRAGED) {
                // Cambia a ENRAGED cuando la salud del dragón sea menor a 50
                currentPhase = DemonPhase.ENRAGED;
                dragon.customName(Component.text("ENRAGED PERMADEATH DEMON", NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD));
            }
        }
    }

}

