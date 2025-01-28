package org.mxtrco.permadeath.event.entity;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LittleZombies implements Listener {

    // Registra el evento cuando la clase sea cargada
    public LittleZombies(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onZombieSpawn(CreatureSpawnEvent event) {
        // Verifica si la entidad generada es un zombi
        if (event.getEntityType() == EntityType.ZOMBIE) {
            Zombie zombie = (Zombie) event.getEntity();
            // Configura al zombi como bebé (pequeño)
            zombie.setBaby(true);
        }
    }
}
