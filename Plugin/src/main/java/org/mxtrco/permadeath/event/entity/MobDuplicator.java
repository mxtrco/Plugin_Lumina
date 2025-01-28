package org.mxtrco.permadeath.event.entity;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class MobDuplicator implements Listener {

    private final NamespacedKey duplicatedKey;

    public MobDuplicator(JavaPlugin plugin) {
        // Crear una clave única para identificar mobs duplicados
        this.duplicatedKey = new NamespacedKey(plugin, "duplicated");
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Filtrar mobs que no sean naturales o sean spawneados por plugins (CUSTOM)

        // Comprobar si el mundo es el Overworld
        if (event.getEntity().getWorld().getName().equalsIgnoreCase("gulag")) {
            return; // Salir si el mundo es "gulag"
        }

        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }

        Entity entity = event.getEntity();

        // Filtrar solo mobs específicos (Creepers, Zombies, Skeletons)
        switch (entity.getType()) {
            case CREEPER:
            case ZOMBIE:
            case SKELETON:
            case SPIDER:
            case BLAZE:
            case BOGGED:
            case BREEZE:
            case HUSK:
            case PHANTOM:
            case CAVE_SPIDER:
            case STRAY:
            case WITCH:
            case WITHER:
            case ZOMBIE_VILLAGER:
                break; // Continuar solo si es uno de los tipos permitidos
            default:
                return; // Ignorar otros mobs
        }

        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();

        // Verificar si la entidad ya fue duplicada
        if (dataContainer.has(duplicatedKey, PersistentDataType.BYTE)) {
            return; // No duplicar entidades que ya tienen la marca
        }

        // Marcar la entidad original
        dataContainer.set(duplicatedKey, PersistentDataType.BYTE, (byte) 1);

        // Duplicar la entidad en la misma posición
        Entity duplicate = entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());

        // Marcar el duplicado para evitar futuras duplicaciones
        duplicate.getPersistentDataContainer().set(duplicatedKey, PersistentDataType.BYTE, (byte) 1);

        // Informar en consola sobre la duplicación
        //Bukkit.getLogger().info("Entidad duplicada: " + entity.getType() + " en la posición " + entity.getLocation());
    }

}

