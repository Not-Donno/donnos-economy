package com.donno.economy;

import com.donno.economy.economy.CurrencyFormatter;
import com.donno.economy.economy.IncomeManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Adds the current Donno's Economy sell price to item tooltips. */
public class DonnoSEconomyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, type, tooltip) -> {
            long price = IncomeManager.getPrice(stack.getItem());
            if (price <= 0 || stack.isEmpty()) {
                return;
            }

            tooltip.add(Component.literal("Sell price: " + CurrencyFormatter.format(price) + " each"));

            long stackValue = price * stack.getCount();
            if (stack.getCount() > 1) {
                tooltip.add(Component.literal("Stack value: " + CurrencyFormatter.format(stackValue)));
            }
        });
    }
}
