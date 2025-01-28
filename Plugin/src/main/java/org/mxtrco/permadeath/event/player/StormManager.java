package org.mxtrco.permadeath.event.player;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.mxtrco.permadeath.Main;

import java.time.LocalDate;

public class StormManager {

    private final int STORM_DURATION_SECONDS = getStormDuration();
    private long stormEndTime = 0;
    private boolean isStormActive = false;
    private BukkitRunnable stormTask;

    private final FileConfiguration config;

    public StormManager(org.mxtrco.permadeath.Main plugin) {
        this.config = plugin.getConfig();
    }

    public int getStormDuration() {
        // Obtiene la fecha actual
        LocalDate today = LocalDate.now();

        // Obtiene el día del mes y ajusta el inicio del mes restando 3
        int adjustedDay = today.getDayOfMonth() - 3;

        // Asegura que el valor no sea negativo
        adjustedDay = Math.max(adjustedDay, 0);

        // Calcula la duración de la tormenta
        int BASE_STORM_DURATION_SECONDS = 3600;
        return BASE_STORM_DURATION_SECONDS * adjustedDay;
    }

    public void onPlayerDeath(World world) {
        if (!isStormActive) {
            startStorm(world, STORM_DURATION_SECONDS);
        } else {
            extendStorm(world);
        }
    }

    public void startStorm(World world, long durationInSeconds) {
        // Cancel any existing task before starting a new one
        if (stormTask != null) {
            stormTask.cancel();
            stormTask = null;
        }

        // Calculate the storm duration
        long duration = (durationInSeconds > 0) ? durationInSeconds : STORM_DURATION_SECONDS;
        stormEndTime = System.currentTimeMillis() + (duration * 1000);

        // Set up the world weather
        isStormActive = true;
        world.setStorm(true);
        world.setThundering(true);
        world.setThunderDuration((int) duration * 20); // Convert seconds to ticks
        world.setWeatherDuration((int) duration * 20); // Convert seconds to ticks

        // Schedule the end of the storm
        BukkitRunnable stormRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() >= stormEndTime) {
                    stopStorm(world);
                }
            }
        };
        stormTask = stormRunnable;
        stormRunnable.runTaskLater(Main.getInstance(), duration * 20); // Convert seconds to ticks

        // Optionally start a countdown or any additional logic
        startStormCountdown(world);
        saveStormState();
    }

    public void extendStorm(World world) {
        if (!isStormActive) {
            return;
        }

        // Cancelar la tarea existente antes de extender
        if (stormTask != null) {
            stormTask.cancel();
            stormTask = null;
        }

        stormEndTime = stormEndTime + (STORM_DURATION_SECONDS * 1000);
        world.setWeatherDuration(STORM_DURATION_SECONDS * 20);
        startStormCountdown(world); // Reiniciar el contador con el nuevo tiempo
        saveStormState();
    }

    public void startStormCountdown(World world) {
        // Cancel the previous task if it exists
        if (stormTask != null) {
            stormTask.cancel();
            stormTask = null;
        }

        stormTask = new BukkitRunnable() {
            private long lastDisplayedTime = -1;

            @Override
            public void run() {
                if (!isStormActive) {
                    cancel();
                    return;
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime < stormEndTime) {
                    long remainingTime = (stormEndTime - currentTime) / 1000;
                    if (remainingTime != lastDisplayedTime) {
                        lastDisplayedTime = remainingTime;
                        String formattedTime = formatTime((int) remainingTime);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendActionBar("§7Tiempo restante de tormenta: §7" + formattedTime);
                        }
                    }
                } else {
                    endStorm(world);
                }
            }
        };
        stormTask.runTaskTimer(Main.getInstance(), 0L, 20L); // Schedule to run every tick (20 ticks per second)
    }

    private void endStorm(World world) {
        if (stormTask != null) {
            stormTask.cancel();
            stormTask = null;
        }

        world.setStorm(false);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar("§a¡La tormenta ha terminado!");
        }
        isStormActive = false;
        saveStormState();
    }

    public void stopStorm(World world) {
        if (stormTask != null) {
            stormTask.cancel();
            stormTask = null;
        }
        stormEndTime = 0;
        world.setStorm(false);
        world.setThundering(false);
        isStormActive = false;
        saveStormState();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar("§a¡La tormenta ha sido detenida manualmente!");
        }
    }

    public void addTimeToStorm(World world, long timeInSeconds) {
        if (!isStormActive()) {
            return;
        }
        stormEndTime += (timeInSeconds * 1000);
    }

    public boolean removeTimeFromStorm(World world, long timeInSeconds) {
        if (!isStormActive()) {
            return false;
        }
        long timeToRemove = timeInSeconds * 1000;
        if (stormEndTime - System.currentTimeMillis() <= timeToRemove) {
            stormEndTime = 0;
            stopStorm(world);
            return false;
        } else {
            stormEndTime -= timeToRemove;
            return true;
        }
    }

    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void saveStormState() {
        // Guardar el estado de la tormenta y la hora de finalización en el archivo de configuración
        config.set("storm.isActive", isStormActive);
        config.set("storm.endTime", stormEndTime); // Guardar la hora en la que debe terminar la tormenta
        org.mxtrco.permadeath.Main.getInstance().saveConfig(); // Guardar el archivo de configuración
    }

    public void loadStormState() {
        // Cargar el estado de la tormenta y la hora de finalización desde el archivo de configuración
        isStormActive = config.getBoolean("storm.isActive", false);
        stormEndTime = config.getLong("storm.endTime", 0);

        if (isStormActive && stormEndTime > 0) {
            long currentTime = System.currentTimeMillis();

            // Si la tormenta no ha terminado, iniciar la tormenta con el tiempo restante
            if (currentTime < stormEndTime) {
                long remainingTime = (stormEndTime - currentTime) / 1000; // Calcular el tiempo restante en segundos
                World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);

                // Verificar que el mundo esté disponible
                if (world == null) {
                    //getLogger().severe("No se encontró ningún mundo cargado para restaurar la tormenta.");
                    return;
                }

                // Ajustar la duración de la tormenta según el tiempo restante
                int remainingTicks = (int) (remainingTime * 20); // Convertir segundos a ticks (1 segundo = 20 ticks)
                world.setWeatherDuration(remainingTicks);
                world.setStorm(true);
                world.setThundering(true);

                startStormCountdown(world); // Iniciar la cuenta atrás de la tormenta con el tiempo restante
            } else {
                // Si el tiempo ha pasado, finalizar la tormenta
                World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
                if (world != null) {
                    endStorm(world);
                }
            }
        } else {
            // Si la tormenta no está activa, no hacer nada
            //getLogger().info("La tormenta no está activa o no hay tiempo de finalización disponible.");
        }
    }

    public boolean isStormActive() {
        return isStormActive;
    }
}
