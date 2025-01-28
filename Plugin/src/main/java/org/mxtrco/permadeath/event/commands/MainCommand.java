package org.mxtrco.permadeath.event.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.mxtrco.permadeath.Main;
import org.mxtrco.permadeath.event.player.StormManager;

public class MainCommand implements CommandExecutor {

    // Método para convertir tiempo en formato <n>h, <n>m, <n>s a segundos
    private long parseTime(String time) throws IllegalArgumentException {
        long totalSeconds = 0;

        // Asegurarse de que no hay espacios en la cadena
        time = time.replaceAll("\\s+", "");

        // Patrón para buscar segmentos de tiempo como 2h, 30m, 45s
        String regex = "(\\d+)([hms])";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(time);

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1)); // Captura el número
            String unit = matcher.group(2);                // Captura la unidad (h, m, s)

            switch (unit) {
                case "h":
                    totalSeconds += value * 3600;
                    break;
                case "m":
                    totalSeconds += value * 60;
                    break;
                case "s":
                    totalSeconds += value;
                    break;
                default:
                    throw new IllegalArgumentException("Unidad de tiempo inválida: " + unit);
            }
        }

        // Verificar si no hubo coincidencias válidas
        if (totalSeconds == 0) {
            throw new IllegalArgumentException("Formato de tiempo inválido: " + time);
        }

        return totalSeconds;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("permadeath") && sender.hasPermission("miplugin.permadeath")) {
            if (args.length > 0) {
                // Comando /permadeath storm
                if (args[0].equalsIgnoreCase("storm")) {
                    if (args.length < 2) {
                        sender.sendMessage("§cUso: /permadeath storm <start <time>|stop|add <time>|remove <time>>.");
                        return true;
                    }

                    World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
                    if (world == null) {
                        sender.sendMessage("§cNo hay un mundo disponible para manejar la tormenta.");
                        return true;
                    }

                    StormManager stormManager = Main.getInstance().getStormManager();
                    String subCommand = args[1].toLowerCase();

                    switch (subCommand) {
                        case "start":
                            if (stormManager.isStormActive()) {
                                sender.sendMessage("§cYa hay una tormenta activa.");
                            } else if (args.length < 3) {
                                sender.sendMessage("§cUso: /permadeath storm start <tiempo>.");
                            } else {
                                try {
                                    long timeInSeconds = parseTime(args[2]);
                                    stormManager.startStorm(world, timeInSeconds);
                                    sender.sendMessage("§a¡La tormenta ha comenzado por " + args[2] + "!");
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage("§cFormato de tiempo inválido. Usa <n>h, <n>m o <n>s (ejemplo: 1h30m).");
                                }
                            }
                            break;

                        case "stop":
                            if (stormManager.isStormActive()) {
                                stormManager.stopStorm(world);
                                sender.sendMessage("§a¡La tormenta ha sido detenida!");
                            } else {
                                sender.sendMessage("§cNo hay ninguna tormenta activa en este momento.");
                            }
                            break;

                        case "add":
                        case "remove":
                            if (!stormManager.isStormActive()) {
                                sender.sendMessage("§cNo hay ninguna tormenta activa para ajustar el tiempo.");
                                break;
                            }

                            if (args.length < 3) {
                                sender.sendMessage("§cUso: /permadeath storm " + subCommand + " <tiempo>.");
                                break;
                            }

                            try {
                                long timeInSeconds = parseTime(args[2]);
                                if (subCommand.equals("add")) {
                                    stormManager.addTimeToStorm(world, timeInSeconds);
                                    sender.sendMessage("§a¡Se han añadido " + args[2] + " a la tormenta!");
                                } else {
                                    stormManager.removeTimeFromStorm(world, timeInSeconds);
                                    sender.sendMessage("§a¡Se han quitado " + args[2] + " de la tormenta!");

                                }
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage("§cFormato de tiempo inválido. Usa <n>h, <n>m o <n>s (ejemplo: 1h30m).");
                            }
                            break;

                        default:
                            sender.sendMessage("§cSubcomando desconocido. Uso: /permadeath storm <start <time>|stop|add <time>|remove <time>>.");
                            break;
                    }
                    return true;
                }
            }
                try {
                    int dificultad = Integer.parseInt(args[0]);
                    if (dificultad < 1 || dificultad > 14) {
                        sender.sendMessage("La dificultad debe estar entre 1 y 14.");
                        return false;
                    }

                    boolean difficultyActive = Main.getInstance().areDifficultyActive(dificultad);
                    if (difficultyActive) {
                        // Desactivar la dificultad
                        Main.getInstance().setDifficultyActive(false, dificultad);
                        sender.sendMessage("Dificultad " + dificultad + " desactivada.");
                    } else {
                        // Activar la dificultad
                        Main.getInstance().setDifficultyActive(true, dificultad);
                        sender.sendMessage("Dificultad " + dificultad + " activada.");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("Por favor, ingrese un número válido de dificultad entre 1 y 14.");
                }
                return true;
            } else {
            sender.sendMessage("Uso correcto: /permadeath <dificultad>.");
        }
        return true;
    }
}