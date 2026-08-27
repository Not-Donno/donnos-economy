package com.donno.economy;

import com.donno.economy.command.EconomyCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.donno.economy.item.ModItems;

public class DonnoSEconomy implements ModInitializer {

    public static final String MOD_ID = "donnos-economy";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.registerModItems();

        LOGGER.info("Donno's Economy is starting!");

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        EconomyCommands.register(dispatcher)
        );
    }
}