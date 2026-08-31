package com.donno.economy.reputation;

import com.donno.economy.economy.EconomyData;
import com.donno.economy.economy.PlayerEconomy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ReputationManager {

    public static final int PLAYER_KILL_REPUTATION = -10;
    public static final int VILLAGER_HIT_REPUTATION = -2;
    public static final int IRON_GOLEM_HIT_REPUTATION = -2;
    public static final int ENDER_DRAGON_KILL_REPUTATION = -50;
    public static final int ZOMBIE_KILL_REPUTATION = 1;
    public static final int SKELETON_KILL_REPUTATION = 1;

    public static final long BOUNTY_PER_NEGATIVE_REPUTATION = 25;
    public static final long MAX_BOUNTY = 1_000;

    private ReputationManager() {
    }

    public static PlayerEconomy get(MinecraftServer server, ServerPlayer player) {
        return EconomyData.get(server).getPlayer(player.getUUID());
    }

    public static int getReputation(MinecraftServer server, ServerPlayer player) {
        return get(server, player).getReputation();
    }

    public static long getBounty(MinecraftServer server, ServerPlayer player) {
        return get(server, player).getBounty();
    }

    public static void changeReputation(MinecraftServer server, ServerPlayer player, int amount) {
        if (amount == 0) {
            return;
        }

        PlayerEconomy economy = get(server, player);
        economy.changeReputation(amount);

        // Every point of negative reputation adds to the criminal's outstanding bounty.
        if (amount < 0) {
            long bountyIncrease = Math.multiplyExact((long) -amount, BOUNTY_PER_NEGATIVE_REPUTATION);
            economy.addBounty(bountyIncrease);
        }

        EconomyData.get(server).updatePlayer(player.getUUID(), economy);
    }

    public static void resetReputation(MinecraftServer server, ServerPlayer player) {
        PlayerEconomy economy = get(server, player);
        economy.resetReputation();
        EconomyData.get(server).updatePlayer(player.getUUID(), economy);
    }

    public static long claimBounty(MinecraftServer server, ServerPlayer target) {
        PlayerEconomy economy = get(server, target);
        long bounty = economy.claimBounty();
        EconomyData.get(server).updatePlayer(target.getUUID(), economy);
        return bounty;
    }

    /**
     * Clears a player's outstanding bounty without changing their reputation.
     * A bounty is a death-reset value, so it must be cleared for every kind of
     * player death (PvP, mobs, environmental damage, or self-inflicted death).
     */
    public static long resetBounty(MinecraftServer server, ServerPlayer player) {
        PlayerEconomy economy = get(server, player);
        long bounty = economy.claimBounty();
        EconomyData.get(server).updatePlayer(player.getUUID(), economy);
        return bounty;
    }

}
