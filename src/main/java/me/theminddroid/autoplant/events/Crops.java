package me.theminddroid.autoplant.events;

import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.theminddroid.autoplant.AutoPlant;
import me.theminddroid.autoplant.HexCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Crops implements Listener {

    private final Set<Material> cropList = EnumSet.of(Material.WHEAT, Material.POTATOES, Material.CARROTS, Material.COCOA, Material.BEETROOTS, Material.NETHER_WART);
    private final Set<Material> stackableCropList = EnumSet.of(Material.SUGAR_CANE, Material.CACTUS);
    private final Set<Material> soil = EnumSet.of(Material.DIRT, Material.WARPED_NYLIUM, Material.CRIMSON_NYLIUM);

    private final Map<Material, String> configFields = new ImmutableMap.Builder<Material, String>()
            .put(Material.SUGAR_CANE, "SUGAR CANE")
            .put(Material.CACTUS, "CACTUS")
            .put(Material.WHEAT, "WHEAT")
            .put(Material.POTATOES, "POTATOES")
            .put(Material.CARROTS, "CARROTS")
            .put(Material.COCOA, "COCOA BEANS")
            .put(Material.BEETROOTS, "BEETROOTS")
            .put(Material.NETHER_WART, "NETHER WARTS")
            .put(Material.OAK_SAPLING, "OAK")
            .put(Material.BIRCH_SAPLING, "BIRCH")
            .put(Material.SPRUCE_SAPLING, "SPRUCE")
            .put(Material.ACACIA_SAPLING, "ACACIA")
            .put(Material.DARK_OAK_SAPLING, "DARK OAK")
            .put(Material.JUNGLE_SAPLING, "JUNGLE")
            .put(Material.CRIMSON_FUNGUS, "CRIMSON")
            .put(Material.WARPED_FUNGUS, "WARPED")
            .build();

    private final Map<Material, Material> treeMaterials = new ImmutableMap.Builder<Material, Material>()
            .put(Material.OAK_LOG, Material.OAK_SAPLING)
            .put(Material.BIRCH_LOG, Material.BIRCH_SAPLING)
            .put(Material.SPRUCE_LOG, Material.SPRUCE_SAPLING)
            .put(Material.ACACIA_LOG, Material.ACACIA_SAPLING)
            .put(Material.DARK_OAK_LOG, Material.DARK_OAK_SAPLING)
            .put(Material.JUNGLE_LOG, Material.JUNGLE_SAPLING)
            .put(Material.CRIMSON_STEM, Material.CRIMSON_FUNGUS)
            .put(Material.WARPED_STEM, Material.WARPED_FUNGUS)
            .put(Material.STRIPPED_BIRCH_LOG, Material.BIRCH_SAPLING)
            .put(Material.STRIPPED_OAK_LOG, Material.OAK_SAPLING)
            .put(Material.STRIPPED_SPRUCE_LOG, Material.SPRUCE_SAPLING)
            .put(Material.STRIPPED_ACACIA_LOG, Material.ACACIA_SAPLING)
            .put(Material.STRIPPED_DARK_OAK_LOG, Material.DARK_OAK_SAPLING)
            .put(Material.STRIPPED_JUNGLE_LOG, Material.JUNGLE_SAPLING)
            .put(Material.STRIPPED_CRIMSON_STEM, Material.CRIMSON_FUNGUS)
            .put(Material.STRIPPED_WARPED_STEM, Material.WARPED_FUNGUS)
            .build();

    private final Set<Material> saplings = new HashSet<>(treeMaterials.values());

    @EventHandler
    public void cropBroken(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        final com.sk89q.worldedit.util.Location worldGuardLocation = BukkitAdapter.adapt(block.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get((World) worldGuardLocation.getExtent());

        if (regions == null) {
            Bukkit.getLogger().finer("WorldGuard failed to return region manager for world.");
            return;
        }

        ApplicableRegionSet set = regions.getApplicableRegions(worldGuardLocation.toVector().toBlockPoint());
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        if (!set.testState(localPlayer, AutoPlant.AUTO_PLANT)) {
            Bukkit.getLogger().finer("Player not in autoplant region.");
            return;
        }

        if (player.hasPermission("autoplant.bypass")) {
            Bukkit.getLogger().finer("Player has autoplant bypass.");
            return;
        }

        try {

            if (treeMaterials.containsKey(block.getType())) {
                if (!AutoPlant.getPlugin(AutoPlant.class).getConfig().getBoolean(configFields.get(treeMaterials.get(block.getType())) + ".enabled")) {
                    return;
                }
            }

            if (configFields.containsKey(block.getType())){
                if (!AutoPlant.getPlugin(AutoPlant.class).getConfig().getBoolean(configFields.get(block.getType()) + ".enabled")) {
                    return;
                }
            }

        } catch (Exception e) {
            Bukkit.getLogger().warning("[AutoPlant]: Unable to read toggled crops from config: " + e);
        }

        Bukkit.getLogger().finer("Crops triggered");

        handleCrop(event, player, block);
        handleTree(block);
        handleStackableCrops(event, player, block);
        handleSapling(event, player, block);
    }

    private void handleSapling(BlockBreakEvent event, Player player, Block block) {
        String saplingMessage = AutoPlant.getPlugin(AutoPlant.class).getConfig().getString("Sapling");
        Material material = block.getType();

        if (saplings.contains(material)) {
            event.setCancelled(true);
            assert saplingMessage != null;
            player.sendTitle(" ", HexCreator.generateHexMessage(saplingMessage), 10, 30,10);
        }
    }


    private void handleCrop(BlockBreakEvent event, Player player, Block block) {
        String cropMessage = AutoPlant.getPlugin(AutoPlant.class).getConfig().getString("Crop");
        BlockData blockData = block.getBlockData();
        Material material = block.getType();

        if (!(blockData instanceof Ageable)) {
            return;
        }

        if (!cropList.contains(material)) {
            return;
        }

        Bukkit.getLogger().finer("Handling crop.");

        Ageable age = (Ageable) blockData;

        if (age.getAge() != age.getMaximumAge()) {

            event.setCancelled(true);
            assert cropMessage != null;
            player.sendTitle(" ", HexCreator.generateHexMessage(cropMessage), 10, 30,10);
            return;
        }

        Bukkit.getScheduler().runTaskLater(AutoPlant.getInstance(), () -> {
            block.setType(material);

            BlockData newBlockData = block.getBlockData();
            if (blockData instanceof Directional && newBlockData instanceof Directional) {
                Directional newDirectional = (Directional) newBlockData;
                Directional oldDirectional = (Directional) blockData;

                newDirectional.setFacing(oldDirectional.getFacing());
            }
            block.setBlockData(newBlockData);
        }, 1);
    }


    private void handleTree(Block block) {
        Material material = block.getType();

        if (!(treeMaterials.containsKey(material))) {
            Bukkit.getLogger().finer("Tree not found " + material);
            return;
        }

        Bukkit.getLogger().finer("Handling tree.");

        if (!soil.contains((block.getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType()))) {
            Bukkit.getLogger().finer("Log not on dirt.");
            return;
        }
        Bukkit.getScheduler().runTaskLater(AutoPlant.getInstance(), () -> block.setType(treeMaterials.get(material)), 1);
    }


    private void handleStackableCrops(BlockBreakEvent event, Player player, Block block) {
        String stackableCropBaseMessage = AutoPlant.getPlugin(AutoPlant.class).getConfig().getString("StackableCropBase");
        String cropMessage = AutoPlant.getPlugin(AutoPlant.class).getConfig().getString("Crop");

        BlockData blockData = block.getBlockData();
        Material material = block.getType();

        if (!stackableCropList.contains(material)) {
            return;
        }

        Bukkit.getLogger().finer("Handling stackable crop.");

        if (block.getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType() == material) {
            cancelEvent(event, player, stackableCropBaseMessage);
            return;
        }

        if (block.getLocation().add(0.0, 2.0, 0.0).getBlock().getType() != material) {
            cancelEvent(event, player, cropMessage);
            return;
        }

        Bukkit.getScheduler().runTaskLater(AutoPlant.getInstance(), () -> block.setType(material), 10);
    }

    private void cancelEvent(BlockBreakEvent event, Player player, String message) {
        event.setCancelled(true);
        player.sendTitle(" ", HexCreator.generateHexMessage(message), 10, 30, 10);
    }
}