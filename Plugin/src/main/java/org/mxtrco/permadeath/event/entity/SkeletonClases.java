package org.mxtrco.permadeath.event.entity;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class SkeletonClases implements Listener {

    public SkeletonClases(JavaPlugin plugin) {
        // Registra esta clase como un listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Verifica si la entidad generada es un esqueleto
        if (event.getEntityType() == EntityType.SKELETON) {
            Skeleton skeleton = (Skeleton) event.getEntity();

            // Asigna una clase aleatoria al esqueleto
            assignSkeletonClass(skeleton);
        }
    }

    private void assignSkeletonClass(Skeleton skeleton) {
        Random rand = new Random();
        int randomClass = rand.nextInt(5);  // Genera un número entre 0 y 4

        switch (randomClass) {
            case 0: // Netherite Archer (más letal)
                assignArmor(skeleton, Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE,
                        Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS, Enchantment.PROTECTION, 5);
                assignBow(skeleton, Enchantment.POWER, 5, Enchantment.FLAME, 1);
                //skeleton.setCustomName("Netherite Archer");
                break;
            case 1: // Diamond Archer
                assignArmor(skeleton, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
                        Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Enchantment.PROTECTION, 4);
                assignBow(skeleton, Enchantment.POWER, 4, Enchantment.FLAME, 1);
                //skeleton.setCustomName("Diamond Archer");
                break;
            case 2: // Iron Archer
                assignArmor(skeleton, Material.IRON_HELMET, Material.IRON_CHESTPLATE,
                        Material.IRON_LEGGINGS, Material.IRON_BOOTS, Enchantment.PROTECTION, 3);
                assignBow(skeleton, Enchantment.POWER, 3, Enchantment.FLAME, 1);
                //skeleton.setCustomName("Iron Archer");
                break;
            case 3: // Golden Archer
                assignArmor(skeleton, Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE,
                        Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS, Enchantment.PROTECTION, 2);
                assignBow(skeleton, Enchantment.POWER, 2, Enchantment.FLAME, 1);
                //skeleton.setCustomName("Golden Archer");
                break;
            case 4: // Leather Archer (menos letal)
                assignArmor(skeleton, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
                        Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Enchantment.PROTECTION, 1);
                assignBow(skeleton, Enchantment.POWER, 1, Enchantment.FLAME, 1);
                //skeleton.setCustomName("Leather Archer");
                break;
        }
    }

    private void assignArmor(Skeleton skeleton, Material helmet, Material chestplate,
                             Material leggings, Material boots, Enchantment enchantment, int level) {
        skeleton.getEquipment().setHelmet(createEnchantedItem(helmet, enchantment, level));
        skeleton.getEquipment().setChestplate(createEnchantedItem(chestplate, enchantment, level));
        skeleton.getEquipment().setLeggings(createEnchantedItem(leggings, enchantment, level));
        skeleton.getEquipment().setBoots(createEnchantedItem(boots, enchantment, level));
    }

    private void assignBow(Skeleton skeleton, Enchantment primaryEnchant, int primaryLevel,
                           Enchantment secondaryEnchant, int secondaryLevel) {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        if (bowMeta != null) {
            bowMeta.addEnchant(primaryEnchant, primaryLevel, true);
            if (secondaryEnchant != null) {
                bowMeta.addEnchant(secondaryEnchant, secondaryLevel, true);
            }
        }
        bow.setItemMeta(bowMeta);
        skeleton.getEquipment().setItemInMainHand(bow);
    }

    private ItemStack createEnchantedItem(Material material, Enchantment enchantment, int level) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        item.setItemMeta(meta);
        return item;
    }
}
