package com.brico.onigokkopuraguinn.listener;

import io.papermc.paper.block.BlockPredicate;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAdventurePredicate;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.BlockTypeKeys;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * アドベンチャーモードでも鉄のツルハシでひび割れた石レンガだけ壊せるようにする。
 */
public class AdventurePickaxeListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        applyToInventory(event.getPlayer().getInventory());
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        applyCanBreak(event.getInventory().getResult());
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemStack result = event.getCurrentItem();
        if (result != null) {
            applyCanBreak(result);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        applyCanBreak(event.getItem().getItemStack());
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        applyCanBreak(item);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        applyCanBreak(event.getCurrentItem());
        applyCanBreak(event.getCursor());
        if (event.getWhoClicked() instanceof Player player) {
            applyToInventory(player.getInventory());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            applyToInventory(player.getInventory());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.getBlock().getType() != Material.CRACKED_STONE_BRICKS) return;
        if (event.getItemInHand().getType() != Material.IRON_PICKAXE) return;

        applyCanBreak(event.getItemInHand());
        event.setInstaBreak(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.ADVENTURE) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        boolean ironPickaxe = hand.getType() == Material.IRON_PICKAXE;
        boolean crackedBrick = event.getBlock().getType() == Material.CRACKED_STONE_BRICKS;

        if (ironPickaxe) {
            applyCanBreak(hand);
            // 鉄のツルハシではひび割れた石レンガ以外を壊せない
            if (!crackedBrick) {
                event.setCancelled(true);
            }
        }
    }

    public static void applyToInventory(PlayerInventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            applyCanBreak(item);
        }
        applyCanBreak(inventory.getItemInOffHand());
    }

    public static void applyToPlayer(Player player) {
        applyToInventory(player.getInventory());
    }

    /**
     * 鉄のツルハシに「ひび割れた石レンガのみ破壊可」を付与する。
     */
    public static void applyCanBreak(ItemStack item) {
        if (item == null || item.getType() != Material.IRON_PICKAXE) return;

        item.setData(
                DataComponentTypes.CAN_BREAK,
                ItemAdventurePredicate.itemAdventurePredicate()
                        .addPredicate(BlockPredicate.predicate()
                                .blocks(RegistrySet.keySet(RegistryKey.BLOCK, BlockTypeKeys.CRACKED_STONE_BRICKS))
                                .build())
                        .build()
        );
    }
}
