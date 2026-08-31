package com.donno.economy.command;

import com.donno.economy.economy.CurrencyFormatter;
import com.donno.economy.economy.EconomyManager;
import com.donno.economy.economy.IncomeManager;
import com.donno.economy.reputation.ReputationManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.CommandBuildContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class EconomyCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
                Commands.literal("rep")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            showReputation(context.getSource().getServer(), player, player);
                            return 1;
                        })
                        .then(
                                Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer viewer = context.getSource().getPlayerOrException();
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            showReputation(context.getSource().getServer(), viewer, target);
                                            return 1;
                                        })
                        )
        );

        dispatcher.register(
                Commands.literal("bounty")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            showBounty(context.getSource().getServer(), player, player);
                            return 1;
                        })
                        .then(
                                Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer viewer = context.getSource().getPlayerOrException();
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            showBounty(context.getSource().getServer(), viewer, target);
                                            return 1;
                                        })
                        )
        );

        dispatcher.register(
                Commands.literal("balance")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            MinecraftServer server = context.getSource().getServer();

                            long wallet = EconomyManager.getWallet(server, player);
                            long bank = EconomyManager.getBank(server, player);
                            long total = EconomyManager.getTotal(server, player);

                            player.sendSystemMessage(
                                    Component.literal(
                                            "Wallet: " + CurrencyFormatter.format(wallet)
                                                    + " | Bank: " + CurrencyFormatter.format(bank)
                                                    + " | Total: " + CurrencyFormatter.format(total)
                                    )
                            );

                            return 1;
                        })
        );

        dispatcher.register(
                Commands.literal("sell")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            long earned = IncomeManager.sellAll(player);

                            if (earned == 0) {
                                player.sendSystemMessage(Component.literal(
                                        "You have no sellable resources. Use /sell <item> <amount> to sell a specific amount."
                                ));
                                return 0;
                            }

                            player.sendSystemMessage(Component.literal(
                                    "Sold your sellable resources for " + CurrencyFormatter.format(earned) + "."
                            ));
                            return 1;
                        })
                        .then(Commands.literal("prices")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    player.sendSystemMessage(Component.literal(
                                            "Use /price <item> to check a price. /sell <item> sells all of that item; /sell <item> <amount> sells only that amount."
                                    ));
                                    return 1;
                                })
                        )
                        .then(Commands.argument("item", ItemArgument.item(registryAccess))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ItemInput input = ItemArgument.getItem(context, "item");
                                    long earned = IncomeManager.sellMaxPayable(player, input.item().value());
                                    if (earned == 0) {
                                        player.sendSystemMessage(Component.literal("You do not have any of that sellable item."));
                                        return 0;
                                    }
                                    player.sendSystemMessage(Component.literal("Sold that item for " + CurrencyFormatter.format(earned) + "."));
                                    return 1;
                                })
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            ItemInput input = ItemArgument.getItem(context, "item");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            int owned = IncomeManager.countItem(player, input.item().value());
                                            if (owned == 0) {
                                                player.sendSystemMessage(Component.literal("You do not have any of that sellable item."));
                                                return 0;
                                            }
                                            if (owned < amount) {
                                                player.sendSystemMessage(Component.literal("You only have " + owned + " of that item. You need " + amount + " to sell that amount."));
                                                return 0;
                                            }
                                            long earned = IncomeManager.sellItem(player, input.item().value(), amount);
                                            if (earned == 0) {
                                                int minimum = IncomeManager.minimumPayableAmount(input.item().value());
                                                player.sendSystemMessage(Component.literal(
                                                        "That amount cannot be paid in cash. You need a quantity whose total is a multiple of NRs 10. For this item, start with " + minimum + " item(s)."
                                                ));
                                                return 0;
                                            }
                                            player.sendSystemMessage(Component.literal(
                                                    "Sold " + amount + " item(s) for " + CurrencyFormatter.format(earned) + "."
                                            ));
                                            return 1;
                                        })
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("price")
                        .then(Commands.argument("item", ItemArgument.item(registryAccess))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ItemInput input = ItemArgument.getItem(context, "item");
                                    long price = IncomeManager.getPrice(input.item().value());
                                    if (price <= 0) {
                                        player.sendSystemMessage(Component.literal("That item is not currently sellable by the economy."));
                                        return 0;
                                    }
                                    player.sendSystemMessage(Component.literal(
                                            "Sell price: " + CurrencyFormatter.format(price) + " each | You have: " + IncomeManager.countItem(player, input.item().value())
                                    ));
                                    return 1;
                                })
                        )
        );

        dispatcher.register(
                Commands.literal("bank")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            MinecraftServer server = context.getSource().getServer();

                            long bank = EconomyManager.getBank(server, player);

                            player.sendSystemMessage(
                                    Component.literal(
                                            "Bank balance: " + CurrencyFormatter.format(bank)
                                    )
                            );

                            return 1;
                        })
                        .then(
                                Commands.literal("deposit")
                                        .then(
                                                Commands.argument("amount", LongArgumentType.longArg(1))
                                                        .executes(context -> {
                                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                                            MinecraftServer server = context.getSource().getServer();
                                                            long amount = LongArgumentType.getLong(context, "amount");

                                                            long money = Math.multiplyExact(amount, 10L);

                                                            if (!EconomyManager.deposit(server, player, money)) {
                                                                player.sendSystemMessage(
                                                                        Component.literal("You don't have enough Cash.")
                                                                );
                                                                return 0;
                                                            }

                                                            player.sendSystemMessage(
                                                                    Component.literal(
                                                                            "Deposited " + CurrencyFormatter.format(money) + " into your bank."
                                                                    )
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )
                        .then(
                                Commands.literal("withdraw")
                                        .then(
                                                Commands.argument("amount", LongArgumentType.longArg(1))
                                                        .executes(context -> {
                                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                                            MinecraftServer server = context.getSource().getServer();
                                                            long amount = LongArgumentType.getLong(context, "amount");

                                                            long money = Math.multiplyExact(amount, 10L);

                                                            if (EconomyManager.getBank(server, player) < money) {
                                                                player.sendSystemMessage(
                                                                        Component.literal("You don't have enough money in your bank.")
                                                                );
                                                                return 0;
                                                            }

                                                            if (!EconomyManager.canFitCash(player, money)) {
                                                                player.sendSystemMessage(
                                                                        Component.literal("You don't have enough inventory space for that Cash.")
                                                                );
                                                                return 0;
                                                            }

                                                            EconomyManager.withdraw(server, player, money);

                                                            player.sendSystemMessage(
                                                                    Component.literal(
                                                                            "Withdrew " + CurrencyFormatter.format(money) + " from your bank."
                                                                    )
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )
        );
    }
    private static void showReputation(MinecraftServer server, ServerPlayer viewer, ServerPlayer target) {
        int reputation = ReputationManager.getReputation(server, target);
        String owner = target.getUUID().equals(viewer.getUUID()) ? "Your" : target.getName().getString() + "'s";
        viewer.sendSystemMessage(
                Component.literal(
                        owner + " reputation: " + (reputation > 0 ? "+" : "") + reputation
                )
        );
    }

    private static void showBounty(MinecraftServer server, ServerPlayer viewer, ServerPlayer target) {
        long bounty = ReputationManager.getBounty(server, target);
        String owner = target.getUUID().equals(viewer.getUUID()) ? "Your" : target.getName().getString() + "'s";
        viewer.sendSystemMessage(
                Component.literal(
                        owner + " bounty: " + CurrencyFormatter.format(Math.multiplyExact(bounty, 10L))
                )
        );
    }

}
