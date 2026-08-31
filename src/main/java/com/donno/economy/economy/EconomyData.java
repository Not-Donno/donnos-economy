package com.donno.economy.economy;

import com.donno.economy.DonnoSEconomy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyData extends SavedData {

    private final Map<UUID, PlayerEconomy> players;

    public EconomyData() {
        this.players = new HashMap<>();
    }

    public EconomyData(Map<UUID, PlayerEconomy> players) {
        this.players = new HashMap<>(players);
    }

    // Bank is persisted. Wallet is now physical Cash in the player's inventory.
    // wallet remains an optional legacy field so old development worlds can be read.
    private static final Codec<PlayerEconomy> PLAYER_CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.LONG.optionalFieldOf("bank_tenths", -1L).forGetter(PlayerEconomy::getBank),
                            Codec.LONG.optionalFieldOf("bank", 0L).forGetter(e -> 0L),
                            Codec.LONG.optionalFieldOf("wallet", 0L).forGetter(e -> 0L),
                            Codec.INT.optionalFieldOf("reputation", 0).forGetter(PlayerEconomy::getReputation),
                            Codec.LONG.optionalFieldOf("bounty", 0L).forGetter(PlayerEconomy::getBounty)
                    ).apply(instance, (bankTenths, oldBank, oldWallet, reputation, bounty) -> {
                        // Existing development saves stored money in whole NRs.
                        long bank = bankTenths >= 0 ? bankTenths : Math.multiplyExact(oldBank, 10L);
                        long legacyWallet = Math.multiplyExact(oldWallet, 10L);
                        return new PlayerEconomy(bank, legacyWallet, reputation, bounty);
                    })
            );

    public static final Codec<EconomyData> CODEC =
            Codec.unboundedMap(
                    Codec.STRING,
                    PLAYER_CODEC
            ).xmap(
                    savedMap -> {
                        Map<UUID, PlayerEconomy> players = new HashMap<>();

                        for (Map.Entry<String, PlayerEconomy> entry : savedMap.entrySet()) {
                            try {
                                UUID uuid = UUID.fromString(entry.getKey());
                                players.put(uuid, entry.getValue());
                            } catch (IllegalArgumentException ignored) {
                                // Ignore invalid UUIDs.
                            }
                        }

                        return new EconomyData(players);
                    },
                    data -> {
                        Map<String, PlayerEconomy> savedMap = new HashMap<>();

                        for (Map.Entry<UUID, PlayerEconomy> entry : data.players.entrySet()) {
                            savedMap.put(entry.getKey().toString(), entry.getValue());
                        }

                        return savedMap;
                    }
            );

    public static final SavedDataType<EconomyData> TYPE =
            new SavedDataType<>(
                    Identifier.fromNamespaceAndPath(
                            DonnoSEconomy.MOD_ID,
                            "economy"
                    ),
                    EconomyData::new,
                    CODEC,
                    null
            );

    public static EconomyData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);

        if (overworld == null) {
            throw new IllegalStateException("Overworld is not available");
        }

        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean hasPlayer(UUID uuid) {
        return players.containsKey(uuid);
    }

    public PlayerEconomy getPlayer(UUID uuid) {
        PlayerEconomy economy = players.get(uuid);

        if (economy == null) {
            economy = new PlayerEconomy();
            players.put(uuid, economy);
            setDirty();
        }

        return economy;
    }

    public void updatePlayer(UUID uuid, PlayerEconomy economy) {
        players.put(uuid, economy);
        setDirty();
    }
}
