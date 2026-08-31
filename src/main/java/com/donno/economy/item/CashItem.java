package com.donno.economy.item;

import net.minecraft.world.item.Item;

public class CashItem extends Item {
    private final long value;

    public CashItem(Properties properties, long value) {
        super(properties);
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
