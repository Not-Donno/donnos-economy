package com.donno.economy.economy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Sell prices for every vanilla Minecraft 26.2 item, stored in tenths of NRs. */
public final class IncomeManager {

    // Values are tenths of one rupee: 1 = NRs 0.1, 5 = NRs 0.5, 10 = NRs 1, etc.
    private static final Map<Item, Long> SELL_PRICES = new HashMap<>();

    // Compact price groups. Values are stored in tenths of NRs.
    // The economy stays intentionally cheap, but common blocks are not all worth
    // the absolute floor price. Expensive resources and finished gear scale up.
    private static final Map<Long, Set<String>> PRICE_GROUPS = Map.ofEntries(
            Map.entry(1L, Set.of(
                    "dirt", "coarse_dirt", "rooted_dirt", "grass_block", "sand", "red_sand", "gravel",
                    "clay_ball", "snowball", "netherrack", "end_stone", "tuff", "calcite", "dripstone_block",
                    "pointed_dripstone", "cobblestone", "stone", "deepslate", "cobbled_deepslate", "andesite",
                    "diorite", "granite", "blackstone", "basalt", "soul_sand", "soul_soil", "moss_block",
                    "moss_carpet", "sculk", "sculk_vein", "sculk_catalyst", "sculk_shrieker", "cactus",
                    "sugar_cane", "kelp", "seagrass", "short_grass", "fern", "dead_bush", "vine"
            )),
            Map.entry(5L, Set.of(
                    "oak_log", "spruce_log", "birch_log", "jungle_log", "acacia_log", "dark_oak_log", "mangrove_log",
                    "cherry_log", "pale_oak_log", "stripped_oak_log", "stripped_spruce_log", "stripped_birch_log",
                    "stripped_jungle_log", "stripped_acacia_log", "stripped_dark_oak_log", "stripped_mangrove_log",
                    "stripped_cherry_log", "stripped_pale_oak_log", "oak_wood", "spruce_wood", "birch_wood", "jungle_wood",
                    "acacia_wood", "dark_oak_wood", "mangrove_wood", "cherry_wood", "pale_oak_wood", "stripped_oak_wood",
                    "stripped_spruce_wood", "stripped_birch_wood", "stripped_jungle_wood", "stripped_acacia_wood",
                    "stripped_dark_oak_wood", "stripped_mangrove_wood", "stripped_cherry_wood", "stripped_pale_oak_wood",
                    "oak_planks", "spruce_planks", "birch_planks", "jungle_planks", "acacia_planks", "dark_oak_planks",
                    "mangrove_planks", "cherry_planks", "pale_oak_planks", "stick", "bamboo",
                    "oak_sapling", "spruce_sapling", "birch_sapling", "jungle_sapling", "acacia_sapling", "dark_oak_sapling",
                    "mangrove_propagule", "cherry_sapling", "pale_oak_sapling", "wheat_seeds", "beetroot_seeds", "melon_seeds",
                    "pumpkin_seeds", "torch", "leather", "feather", "egg"
            )),
            Map.entry(10L, Set.of(
                    "wheat", "carrot", "potato", "beetroot", "melon_slice", "pumpkin", "apple", "bread", "cookie",
                    "sweet_berries", "glow_berries", "cocoa_beans", "sugar", "kelp", "cod", "salmon", "pufferfish",
                    "tropical_fish", "rotten_flesh", "bone", "string", "spider_eye", "gunpowder", "slime_ball",
                    "coal", "raw_copper", "redstone", "glowstone_dust", "quartz", "lapis_lazuli", "prismarine_shard",
                    "prismarine_crystals", "amethyst_shard", "honeycomb", "ink_sac", "glow_ink_sac", "flint",
                    "brick", "nether_brick", "glass", "terracotta", "white_wool", "black_wool", "orange_wool", "magenta_wool",
                    "light_blue_wool", "yellow_wool", "lime_wool", "pink_wool", "gray_wool", "light_gray_wool", "cyan_wool",
                    "purple_wool", "blue_wool", "brown_wool", "green_wool", "red_wool"
            )),
            Map.entry(20L, Set.of(
                    "iron_ingot", "copper_ingot", "gold_nugget", "iron_nugget", "emerald", "ender_pearl", "blaze_rod",
                    "magma_cream", "phantom_membrane", "crying_obsidian", "obsidian", "saddle", "name_tag", "nautilus_shell",
                    "heart_of_the_sea", "shulker_shell", "enchanted_golden_apple", "totem_of_undying"
            )),
            Map.entry(50L, Set.of(
                    "gold_ingot", "diamond", "netherite_scrap", "dragon_breath", "ghast_tear", "rabbit_foot",
                    "scute", "turtle_egg", "conduit", "sponge", "wet_sponge"
            )),
            Map.entry(100L, Set.of(
                    "ancient_debris", "elytra", "netherite_ingot", "beacon", "enchanted_book"
            )),
            Map.entry(250L, Set.of("nether_star"))
    );

    private IncomeManager() {}

    static {
        // Every vanilla item is sellable. This means new vanilla items in 26.2
        // cannot accidentally become unsellable; they start at the floor price.
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null && "minecraft".equals(id.getNamespace())) {
                SELL_PRICES.put(item, 1L); // NRs 0.1
            }
        }

        for (Map.Entry<Long, Set<String>> group : PRICE_GROUPS.entrySet()) {
            for (String path : group.getValue()) {
                Identifier id = Identifier.fromNamespaceAndPath("minecraft", path);
                Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                if (item != null) SELL_PRICES.put(item, group.getKey());
            }
        }

        // Finished tools, weapons and armor are worth more than their raw floor
        // price. Keep these values low, but make material progression matter.
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || !"minecraft".equals(id.getNamespace())) continue;
            String path = id.getPath();
            long gearPrice = gearPrice(path);
            if (gearPrice > 0) SELL_PRICES.put(item, gearPrice);
        }
    }

    private static long gearPrice(String path) {
        boolean gear = path.endsWith("_sword") || path.endsWith("_pickaxe") || path.endsWith("_axe")
                || path.endsWith("_shovel") || path.endsWith("_hoe") || path.endsWith("_helmet")
                || path.endsWith("_chestplate") || path.endsWith("_leggings") || path.endsWith("_boots")
                || path.equals("bow") || path.equals("crossbow") || path.equals("trident") || path.equals("shield")
                || path.equals("fishing_rod") || path.equals("flint_and_steel") || path.equals("shears");
        if (!gear) return 0;
        if (path.startsWith("netherite_")) return 200L;
        if (path.startsWith("diamond_")) return 100L;
        if (path.startsWith("iron_")) return 40L;
        if (path.startsWith("golden_")) return 25L;
        if (path.startsWith("stone_")) return 10L;
        if (path.startsWith("wooden_")) return 5L;
        if (path.startsWith("leather_")) return 15L;
        return 10L;
    }

    public static long sellAll(ServerPlayer player) {
        long total = 0;

        // Process each item type independently. This prevents a collection of
        // unrelated fractional-value items from being combined into a payout.
        Map<Item, Integer> counts = new HashMap<>();
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.isEmpty() || !SELL_PRICES.containsKey(stack.getItem())) continue;
            counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }

        for (Map.Entry<Item, Integer> entry : counts.entrySet()) {
            Item item = entry.getKey();
            long price = SELL_PRICES.get(item);
            int owned = entry.getValue();
            // Choose the largest quantity <= owned whose
            // total is a multiple of NRs 10.
            long step = 100L / gcd(price, 100L);
            int payableAmount = (int) (owned - (owned % step));
            if (payableAmount <= 0) continue;

            long itemTotal = Math.multiplyExact((long) payableAmount, price);
            int remaining = payableAmount;
            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                if (remaining == 0) break;
                if (stack.isEmpty() || stack.getItem() != item) continue;
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
            total = Math.addExact(total, itemTotal);
        }

        if (total > 0) EconomyManager.giveCash(player, total);
        player.containerMenu.broadcastChanges();
        return total;
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long remainder = a % b;
            a = b;
            b = remainder;
        }
        return Math.abs(a);
    }

    /**
     * Sells exactly amount, but only when the resulting value can be paid using
     * the available physical currency. The smallest note is NRs 10, so every
     * sale must be worth a whole multiple of NRs 10. Nothing is sold on failure.
     */
    public static long sellItem(ServerPlayer player, Item item, int amount) {
        Long price = SELL_PRICES.get(item);
        if (price == null || amount <= 0) return 0;
        if (countItem(player, item) < amount) return 0;

        long total = Math.multiplyExact((long) amount, price);
        if (!EconomyManager.isCashPayable(total)) return 0;

        int remaining = amount;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (remaining == 0) break;
            if (stack.isEmpty() || stack.getItem() != item) continue;
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
        }

        EconomyManager.giveCash(player, total);
        player.containerMenu.broadcastChanges();
        return total;
    }

    /**
     * Sells the largest possible quantity of this item that can be paid using
     * NRs 10-and-up notes. Any leftover items remain in the player's inventory.
     */
    public static long sellMaxPayable(ServerPlayer player, Item item) {
        Long price = SELL_PRICES.get(item);
        if (price == null || price <= 0) return 0;
        int owned = countItem(player, item);
        if (owned <= 0) return 0;

        long step = 100L / gcd(price, 100L);
        int amount = (int) (owned - (owned % step));
        if (amount <= 0) return 0;
        return sellItem(player, item, amount);
    }

    /** Returns the minimum quantity that makes a sale payable in NRs 10 notes. */
    public static int minimumPayableAmount(Item item) {
        Long price = SELL_PRICES.get(item);
        if (price == null || price <= 0) return 0;
        long step = 100L / gcd(price, 100L);
        return (int) step;
    }

    public static int countItem(ServerPlayer player, Item item) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty() && stack.getItem() == item) total += stack.getCount();
        }
        return total;
    }

    public static long getPrice(Item item) { return SELL_PRICES.getOrDefault(item, 0L); }
    public static Map<Item, Long> getSellPrices() { return Map.copyOf(SELL_PRICES); }
}
