package com.donno.economy.item;

import net.minecraft.world.item.Item;

public class CashItem extends Item {

    public static final long VALUE = 100;

    public CashItem(Properties properties) {
        super(properties);
    }

    public long getValue() {
        return VALUE;
    }
}