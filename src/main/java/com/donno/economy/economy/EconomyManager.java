package com.donno.economy.economy;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class EconomyManager {

    public static PlayerEconomy getPlayerEconomy(
            MinecraftServer server,
            ServerPlayer player
    ) {
        EconomyData data = EconomyData.get(server);

        return data.getPlayer(player.getUUID());
    }

    public static long getWallet(
            MinecraftServer server,
            ServerPlayer player
    ) {
        return getPlayerEconomy(server, player).getWallet();
    }

    public static long getBank(
            MinecraftServer server,
            ServerPlayer player
    ) {
        return getPlayerEconomy(server, player).getBank();
    }

    public static long getTotal(
            MinecraftServer server,
            ServerPlayer player
    ) {
        return getPlayerEconomy(server, player).getTotal();
    }

    public static void addWallet(
            MinecraftServer server,
            ServerPlayer player,
            long amount
    ) {
        PlayerEconomy economy =
                getPlayerEconomy(server, player);

        economy.addWallet(amount);

        EconomyData.get(server)
                .updatePlayer(player.getUUID(), economy);
    }

    public static boolean removeWallet(
            MinecraftServer server,
            ServerPlayer player,
            long amount
    ) {
        PlayerEconomy economy =
                getPlayerEconomy(server, player);

        if (!economy.removeWallet(amount)) {
            return false;
        }

        EconomyData.get(server)
                .updatePlayer(player.getUUID(), economy);

        return true;
    }

    public static void addBank(
            MinecraftServer server,
            ServerPlayer player,
            long amount
    ) {
        PlayerEconomy economy =
                getPlayerEconomy(server, player);

        economy.addBank(amount);

        EconomyData.get(server)
                .updatePlayer(player.getUUID(), economy);
    }

    public static boolean removeBank(
            MinecraftServer server,
            ServerPlayer player,
            long amount
    ) {
        PlayerEconomy economy =
                getPlayerEconomy(server, player);

        if (!economy.removeBank(amount)) {
            return false;
        }

        EconomyData.get(server)
                .updatePlayer(player.getUUID(), economy);

        return true;
    }
}