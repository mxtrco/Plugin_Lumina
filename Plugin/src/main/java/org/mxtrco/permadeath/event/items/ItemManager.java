package org.mxtrco.permadeath.event.items;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mxtrco.permadeath.Main;

public class ItemManager implements Listener {
    public static ItemStack pechera, casco, pantalones, botas;
    private static NamespacedKey key;
    private static final double EXTRA_HEALTH = 8.0;

    private static ItemManager instance;
    private final JavaPlugin plugin;


    private ItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
        key = new NamespacedKey(plugin, "armadura_lumina");
        createArmadura();
        registerRecipes();
    }

    public static ItemManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new ItemManager(plugin);
        }
        return instance;
    }

    private void createArmadura() {
        casco = createArmorPiece(Material.NETHERITE_HELMET, "Lumina Helmet");
        pechera = createArmorPiece(Material.NETHERITE_CHESTPLATE, "Lumina Chestplate");
        pantalones = createArmorPiece(Material.NETHERITE_LEGGINGS, "Lumina Leggings");
        botas = createArmorPiece(Material.NETHERITE_BOOTS, "Lumina Boots");
    }

    private ItemStack createArmorPiece(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.DARK_RED + displayName);
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    public void registerRecipes() {
        // Las recetas permanecen iguales
        ShapedRecipe recipeCasco = new ShapedRecipe(new NamespacedKey(plugin, "lumina_helmet"), casco);
        recipeCasco.shape("SSS", "S S", "   ");
        recipeCasco.setIngredient('S', Material.SUGAR);
        Bukkit.addRecipe(recipeCasco);

        ShapedRecipe recipePechera = new ShapedRecipe(new NamespacedKey(plugin, "lumina_chestplate"), pechera);
        recipePechera.shape("S S", "SSS", "SSS");
        recipePechera.setIngredient('S', Material.SUGAR);
        Bukkit.addRecipe(recipePechera);

        ShapedRecipe recipePantalones = new ShapedRecipe(new NamespacedKey(plugin, "lumina_leggings"), pantalones);
        recipePantalones.shape("SSS", "S S", "S S");
        recipePantalones.setIngredient('S', Material.SUGAR);
        Bukkit.addRecipe(recipePantalones);

        ShapedRecipe recipeBotas = new ShapedRecipe(new NamespacedKey(plugin, "lumina_boots"), botas);
        recipeBotas.shape("   ", "S S", "S S");
        recipeBotas.setIngredient('S', Material.SUGAR);
        Bukkit.addRecipe(recipeBotas);
    }

    public static NamespacedKey getKey(JavaPlugin plugin) {
        if (key == null) {
            key = new NamespacedKey(plugin, "armadura_lumina");
        }
        return key;
    }

    @EventHandler
    public void onPlayerArmorChange(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        checkArmorSet(player);
    }

    private void checkArmorSet(Player player) {
        boolean hasFullSet = hasFullLuminaSet(player);
        AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if (healthAttribute != null) {
            double baseHealth = 20.0;

            if (hasFullSet) {
                if (healthAttribute.getBaseValue() == baseHealth) {
                    healthAttribute.setBaseValue(baseHealth + EXTRA_HEALTH);
                    applyPotionEffects(player);
                    player.sendMessage(ChatColor.GREEN + "¡Has completado el set de armadura Lumina!");
                }
            } else {
                if (healthAttribute.getBaseValue() > baseHealth) {
                    healthAttribute.setBaseValue(baseHealth);
                    removePotionEffects(player);
                    player.sendMessage(ChatColor.RED + "¡Set de armadura Lumina incompleto!");
                }
            }
        }
    }

    private boolean hasFullLuminaSet(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        if (armor == null) return false;

        for (ItemStack item : armor) {
            if (item == null || !item.hasItemMeta()) return false;
            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(this.key, PersistentDataType.INTEGER)) {
                return false;
            }
        }
        return true;
    }

    private void applyPotionEffects(Player player) {
        // Se usa false en el primer booleano para ocultar las partículas y false en el segundo para mostrar el ∞
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 0, false, false));
    }

    private void removePotionEffects(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.STRENGTH);
    }
}