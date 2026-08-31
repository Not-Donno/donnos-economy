package com.donno.economy;

import com.donno.economy.command.EconomyCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import com.donno.economy.economy.EconomyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.donno.economy.item.ModItems;
import com.donno.economy.reputation.ReputationEvents;

public class DonnoSEconomy implements ModInitializer {

    public static final String MOD_ID = "donnos-economy";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        ReputationEvents.register();

        LOGGER.info("Donno's Economy is starting!");

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        EconomyCommands.register(dispatcher, registryAccess)
        );

        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {
                    EconomyManager.initializePlayer(server, handler.getPlayer());
                }
        );
    }
}