package org.mxtrco.permadeath.event.entity;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.mxtrco.permadeath.event.items.ItemManager;

import java.util.Random;

public class ArmorDropHandler implements Listener {
    private final Random random = new Random();
    private final ItemManager itemManager;
    private static final double ARMOR_DROP_CHANCE = 0.20; // 20% drop chance

    public ArmorDropHandler(ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        EntityType entityType = event.getEntityType();
        double roll = random.nextDouble();

        // Comprobar si el mundo es el Overworld
        if (event.getEntity().getWorld().getName().equalsIgnoreCase("gulag")) {
            return; // Salir si el mundo es "gulag"
        }

        switch (entityType) {
            case ELDER_GUARDIAN -> {
                if (roll < ARMOR_DROP_CHANCE) {
                    event.getDrops().add(ItemManager.pantalones);
                }
            }
            case WARDEN -> {
                if (roll < ARMOR_DROP_CHANCE) {
                    event.getDrops().add(ItemManager.pechera);
                }
            }
        }
    }
}
