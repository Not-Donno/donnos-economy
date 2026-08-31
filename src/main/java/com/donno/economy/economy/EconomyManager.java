package com.donno.economy.economy;

import com.donno.economy.item.CashItem;
import com.donno.economy.item.ModItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EconomyManager {

    public static final long CASH_VALUE = 1; // one unit = NRs 0.1
    private static final List<CashItem> DENOMINATIONS = List.of(
            (CashItem) ModItems.NOTE_1000,
            (CashItem) ModItems.NOTE_500,
            (CashItem) ModItems.NOTE_100,
            (CashItem) ModItems.NOTE_50,
            (CashItem) ModItems.NOTE_20,
            (CashItem) ModItems.NOTE_10
    );

    private EconomyManager() {}

    public static PlayerEconomy getPlayerEconomy(MinecraftServer server, ServerPlayer player) {
        return EconomyData.get(server).getPlayer(player.getUUID());
    }

    public static long getWallet(MinecraftServer server, ServerPlayer player) { return getWallet(player); }

    public static long getWallet(ServerPlayer player) {
        long wallet = 0;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() instanceof CashItem cash) {
                wallet = Math.addExact(wallet, Math.multiplyExact(stack.getCount(), cash.getValue()));
            }
        }
        return wallet;
    }

    public static long getBank(MinecraftServer server, ServerPlayer player) { return getPlayerEconomy(server, player).getBank(); }
    public static long getTotal(MinecraftServer server, ServerPlayer player) { return Math.addExact(getWallet(player), getBank(server, player)); }

    public static boolean deposit(MinecraftServer server, ServerPlayer player, long amount) {
        validatePhysicalAmount(amount);
        if (!removeCash(player, amount)) return false;
        try { addBank(server, player, amount); return true; }
        catch (RuntimeException exception) { giveCash(player, amount); throw exception; }
    }

    public static boolean withdraw(MinecraftServer server, ServerPlayer player, long amount) {
        if (amount <= 0) return false;
        PlayerEconomy economy = getPlayerEconomy(server, player);
        if (economy.getBank() < amount || !canFitCash(player, amount)) return false;
        if (!economy.removeBank(amount)) return false;
        EconomyData.get(server).updatePlayer(player.getUUID(), economy);
        try { giveCash(player, amount); return true; }
        catch (RuntimeException exception) {
            economy.addBank(amount);
            EconomyData.get(server).updatePlayer(player.getUUID(), economy);
            throw exception;
        }
    }

    public static void addBank(MinecraftServer server, ServerPlayer player, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Bank amount must be positive");
        PlayerEconomy economy = getPlayerEconomy(server, player);
        economy.addBank(amount);
        EconomyData.get(server).updatePlayer(player.getUUID(), economy);
    }

    public static boolean removeBank(MinecraftServer server, ServerPlayer player, long amount) {
        if (amount <= 0) return false;
        PlayerEconomy economy = getPlayerEconomy(server, player);
        if (!economy.removeBank(amount)) return false;
        EconomyData.get(server).updatePlayer(player.getUUID(), economy);
        return true;
    }

    /** Physical cash currently exists only in NRs 10 and larger notes. */
    public static boolean isCashPayable(long amount) {
        return amount > 0 && amount % ModItems.NOTE_10_VALUE == 0;
    }

    public static boolean removeCash(ServerPlayer player, long amount) {
        if (amount <= 0 || getWallet(player) < amount) return false;
        long remaining = amount;
        // Remove from smallest denominations first, so the player keeps larger notes.
        for (CashItem denomination : List.of((CashItem) ModItems.NOTE_10, (CashItem) ModItems.NOTE_20,
                (CashItem) ModItems.NOTE_50, (CashItem) ModItems.NOTE_100,
                (CashItem) ModItems.NOTE_500, (CashItem) ModItems.NOTE_1000)) {
            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                if (!(stack.getItem() instanceof CashItem cash) || cash.getValue() != denomination.getValue()) continue;
                long take = Math.min(stack.getCount(), remaining / cash.getValue());
                if (take > 0) {
                    stack.shrink((int) take);
                    remaining -= take * cash.getValue();
                }
                if (remaining == 0) return true;
            }
        }
        return false;
    }

    public static void giveCash(ServerPlayer player, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Cash amount must be positive");
        long remaining = amount;
        for (CashItem denomination : DENOMINATIONS) {
            long count = remaining / denomination.getValue();
            remaining %= denomination.getValue();
            while (count > 0) {
                int stackSize = (int) Math.min(count, 64);
                ItemStack note = new ItemStack((Item) denomination, stackSize);
                if (!player.getInventory().add(note)) player.drop(note, false);
                count -= stackSize;
            }
        }
        player.containerMenu.broadcastChanges();
    }

    public static boolean canFitCash(ServerPlayer player, long amount) {
        if (amount <= 0) return false;
        long remaining = amount;
        for (CashItem denomination : DENOMINATIONS) {
            long count = remaining / denomination.getValue();
            remaining %= denomination.getValue();
            if (count == 0) continue;
            long freeCapacity = 0;
            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                if (stack.isEmpty()) freeCapacity += 64;
                else if (stack.getItem() == denomination) freeCapacity += 64 - stack.getCount();
            }
            if (freeCapacity < count) return false;
        }
        return true;
    }

    public static void initializePlayer(MinecraftServer server, ServerPlayer player) {
        EconomyData data = EconomyData.get(server);
        boolean existed = data.hasPlayer(player.getUUID());
        PlayerEconomy economy = data.getPlayer(player.getUUID());
        if (economy.getLegacyWallet() > 0) {
            long legacy = economy.getLegacyWallet();
            long cashAmount = legacy - (legacy % CASH_VALUE);
            long remainder = legacy % CASH_VALUE;
            if (cashAmount > 0) giveCash(player, cashAmount);
            if (remainder > 0) economy.addBank(remainder);
            economy.clearLegacyWallet();
            data.updatePlayer(player.getUUID(), economy);
            return;
        }
        if (!existed) giveCash(player, 1000);
    }

    private static void validatePhysicalAmount(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Physical cash amount must be positive");
    }
}
