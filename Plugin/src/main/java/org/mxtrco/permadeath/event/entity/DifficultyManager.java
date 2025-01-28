package org.mxtrco.permadeath.event.entity;

import org.bukkit.World;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spider;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DifficultyManager implements Listener {

    private static final Random random = new Random();

    // Lista de efectos disponibles para las arañas
    private static final List<String> effectList = new ArrayList<>();

    static {
        effectList.add("SPEED;3");
        effectList.add("REGENERATION;4");
        effectList.add("STRENGTH;4");
        effectList.add("JUMP;4");
        effectList.add("GLOWING;0");
        effectList.add("INVISIBILITY;0");
        effectList.add("SLOW_FALLING;0");
        effectList.add("RESISTANCE;3");
    }

    private static void addMobEffects(LivingEntity entity, int force) {
        int appliedEffects = 0;  // Contador de efectos aplicados
        int times = force == 100 ? random.nextInt(3) + 1 : force;  // Número de efectos a aplicar

        // Usamos un Set para evitar duplicados de efectos
        List<String> appliedEffectsList = new ArrayList<>();

        while (appliedEffects < times) {
            // Seleccionar un efecto aleatorio de la lista
            String effect = effectList.get(random.nextInt(effectList.size()));

            // Comprobar que el efecto no ha sido aplicado ya
            if (appliedEffectsList.contains(effect)) {
                continue;  // Si ya ha sido aplicado, seleccionamos otro
            }

            // Agregar el efecto a la lista de aplicados
            appliedEffectsList.add(effect);

            String[] s = effect.split(";");

            PotionEffectType type = PotionEffectType.getByName(s[0]);
            if (type == null) {
                // Si el tipo de efecto es null, saltamos este efecto
                continue;
            }

            int level = Integer.parseInt(s[1]);

            // Aplicar el efecto a la entidad
            entity.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, level));

            // Imprimir el efecto aplicado en la consola
            //Bukkit.getLogger().info("Efecto aplicado a la araña: " + type + " Nivel: " + level);
            appliedEffects++;
        }
    }

    // Manejar el evento cuando una criatura (araña) es generada
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();

        // Comprobar si el mundo es el Overworld
        if (entity.getWorld().getName().equalsIgnoreCase("gulag")) {
            return; // Salir si el mundo es "gulag"
        }

        // Comprobar si la entidad es una araña
        if (entity instanceof Spider) {
            // Aplicar efectos aleatorios a la araña
            addMobEffects((LivingEntity) entity, random.nextInt(3) + 1);
        }
    }
}