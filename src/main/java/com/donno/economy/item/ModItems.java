package com.donno.economy.item;

import com.donno.economy.DonnoSEconomy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final ResourceKey<Item> CASH_KEY =
            ResourceKey.create(
                    BuiltInRegistries.ITEM.key(),
                    Identifier.fromNamespaceAndPath(
                            DonnoSEconomy.MOD_ID,
                            "cash"
                    )
            );

    public static final Item CASH = Registry.register(
            BuiltInRegistries.ITEM,
            CASH_KEY,
            new Item(
                    new Item.Properties()
                            .setId(CASH_KEY)
            )
    );

    public static void registerModItems() {

        DonnoSEconomy.LOGGER.info(
                "Registering Donno's Economy items"
        );
    }
}