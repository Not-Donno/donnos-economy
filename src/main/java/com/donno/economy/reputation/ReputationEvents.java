package com.donno.economy.reputation;

import com.donno.economy.economy.EconomyData;
import com.donno.economy.economy.PlayerEconomy;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.npc.villager.Villager;

public final class ReputationEvents {

    private ReputationEvents() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (entity, source, baseDamageTaken, damageTaken, blocked) -> {
                    if (damageTaken <= 0 || blocked) {
                        return;
                    }

                    if (!(source.getEntity() instanceof ServerPlayer attacker)) {
                        return;
                    }

                    if (entity instanceof Villager) {
                        ReputationManager.changeReputation(attacker.level().getServer(), attacker, ReputationManager.VILLAGER_HIT_REPUTATION);
                    } else if (entity instanceof IronGolem) {
                        ReputationManager.changeReputation(attacker.level().getServer(), attacker, ReputationManager.IRON_GOLEM_HIT_REPUTATION);
                    }
                }
        );

        ServerLivingEntityEvents.AFTER_DEATH.register(
                (entity, source) -> handleDeath(entity, source)
        );
    }

    private static void handleDeath(LivingEntity victim, DamageSource source) {
        // Player bounties must reset on every player death, regardless of how
        // the player died (PvP, mob, environmental damage, or self-death).
        if (victim instanceof ServerPlayer target) {
            MinecraftServer server = target.level().getServer();
            Entity sourceEntity = source.getEntity();
            ServerPlayer killer = sourceEntity instanceof ServerPlayer player ? player : null;

            // Capture the values BEFORE resetting them. This prevents a death from
            // erasing the information needed to determine the PvP penalty/payout.
            PlayerEconomy targetEconomy = EconomyData.get(server).getPlayer(target.getUUID());
            int reputationBeforeDeath = targetEconomy.getReputation();
            long bounty = ReputationManager.resetBounty(server, target);
            ReputationManager.resetReputation(server, target);

            // No payout or PvP penalty for self-death/non-player deaths.
            if (killer == null || killer.getUUID().equals(target.getUUID())) {
                return;
            }

            // Killing a player with reputation -20 or lower is considered a legitimate kill.
            // Otherwise the killer receives the normal player-kill penalty (-10 reputation).
            if (reputationBeforeDeath > -20) {
                ReputationManager.changeReputation(
                        server,
                        killer,
                        ReputationManager.PLAYER_KILL_REPUTATION
                );
            }

            if (bounty > 0) {
                PlayerEconomy killerEconomy = EconomyData.get(server).getPlayer(killer.getUUID());
                killerEconomy.addBank(Math.multiplyExact(bounty, 10L));
                EconomyData.get(server).updatePlayer(killer.getUUID(), killerEconomy);

                killer.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal(
                                "You claimed "
                                        + com.donno.economy.economy.CurrencyFormatter.format(Math.multiplyExact(bounty, 10L))
                                        + " bounty for killing "
                                        + target.getName().getString()
                                        + "."
                        )
                );
            }

            return;
        }

        // Non-player entities keep their existing reputation rules.
        Entity sourceEntity = source.getEntity();
        if (!(sourceEntity instanceof ServerPlayer killer)) {
            return;
        }

        MinecraftServer server = killer.level().getServer();

        if (victim instanceof EnderDragon) {
            ReputationManager.changeReputation(
                    server, killer, ReputationManager.ENDER_DRAGON_KILL_REPUTATION
            );
        } else if (victim instanceof Zombie) {
            ReputationManager.changeReputation(
                    server, killer, ReputationManager.ZOMBIE_KILL_REPUTATION
            );
        } else if (victim instanceof Skeleton) {
            ReputationManager.changeReputation(
                    server, killer, ReputationManager.SKELETON_KILL_REPUTATION
            );
        } else if (victim instanceof Villager) {
            // AFTER_DAMAGE is not called when the hit is fatal, so count the killing blow here.
            ReputationManager.changeReputation(
                    server, killer, ReputationManager.VILLAGER_HIT_REPUTATION
            );
        } else if (victim instanceof IronGolem) {
            ReputationManager.changeReputation(
                    server, killer, ReputationManager.IRON_GOLEM_HIT_REPUTATION
            );
        }
    }

}
