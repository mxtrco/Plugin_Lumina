package org.mxtrco.permadeath.event.entity;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PhantomMount implements Listener {

    public PhantomMount(JavaPlugin plugin) {
        // Registra esta clase como un listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPhantomSpawn(CreatureSpawnEvent event) {
        // Verifica si la entidad generada es un phantom
        if (event.getEntityType() == EntityType.PHANTOM) {
            Phantom phantom = (Phantom) event.getEntity();
            Skeleton skeleton = (Skeleton) phantom.getWorld().spawnEntity(phantom.getLocation(), EntityType.SKELETON);
            phantom.addPassenger(skeleton);
        }
    }
}

