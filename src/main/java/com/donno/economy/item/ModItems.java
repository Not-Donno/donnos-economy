package com.donno.economy.item;

import com.donno.economy.DonnoSEconomy;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final long NOTE_10_VALUE = 100;
    public static final Item NOTE_10 = registerNote("note_10", NOTE_10_VALUE);
    public static final Item NOTE_20 = registerNote("note_20", 200);
    public static final Item NOTE_50 = registerNote("note_50", 500);
    public static final Item NOTE_100 = registerNote("note_100", 1000);
    public static final Item NOTE_500 = registerNote("note_500", 5000);
    public static final Item NOTE_1000 = registerNote("note_1000", 10000);

    // Kept as an alias for compatibility with older code/data.
    public static final Item CASH = NOTE_100;

    private static Item registerNote(String id, long value) {
        ResourceKey<Item> key = ResourceKey.create(
                BuiltInRegistries.ITEM.key(),
                Identifier.fromNamespaceAndPath(DonnoSEconomy.MOD_ID, id)
        );
        return Registry.register(
                BuiltInRegistries.ITEM,
                key,
                new CashItem(new Item.Properties().setId(key), value)
        );
    }

    public static void registerModItems() {
        DonnoSEconomy.LOGGER.info("Registering Donno's Economy cash notes");
    }
}
