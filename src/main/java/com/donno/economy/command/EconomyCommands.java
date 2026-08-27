package com.donno.economy.command;

import com.donno.economy.economy.CurrencyFormatter;
import com.donno.economy.economy.EconomyManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class EconomyCommands {

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {

        // /balance
        dispatcher.register(
                Commands.literal("balance")
                        .executes(context -> {

                            ServerPlayer player =
                                    context.getSource()
                                            .getPlayerOrException();

                            MinecraftServer server =
                                    context.getSource().getServer();

                            long wallet =
                                    EconomyManager.getWallet(
                                            server, player
                                    );

                            long bank =
                                    EconomyManager.getBank(
                                            server, player
                                    );

                            long total =
                                    EconomyManager.getTotal(
                                            server, player
                                    );

                            player.sendSystemMessage(
                                    Component.literal(
                                            "Wallet: "
                                                    + CurrencyFormatter.format(wallet)
                                                    + " | Bank: "
                                                    + CurrencyFormatter.format(bank)
                                                    + " | Total: "
                                                    + CurrencyFormatter.format(total)
                                    )
                            );

                            return 1;
                        })
        );

        // /bank
        dispatcher.register(
                Commands.literal("bank")

                        // /bank
                        .executes(context -> {

                            ServerPlayer player =
                                    context.getSource()
                                            .getPlayerOrException();

                            MinecraftServer server =
                                    context.getSource().getServer();

                            long bank =
                                    EconomyManager.getBank(
                                            server, player
                                    );

                            player.sendSystemMessage(
                                    Component.literal(
                                            "Bank balance: "
                                                    + CurrencyFormatter.format(bank)
                                    )
                            );

                            return 1;
                        })

                        // /bank deposit <amount>
                        .then(
                                Commands.literal("deposit")
                                        .then(
                                                Commands.argument(
                                                        "amount",
                                                        LongArgumentType.longArg(1)
                                                )
                                                        .executes(context -> {

                                                            ServerPlayer player =
                                                                    context.getSource()
                                                                            .getPlayerOrException();

                                                            MinecraftServer server =
                                                                    context.getSource()
                                                                            .getServer();

                                                            long amount =
                                                                    LongArgumentType.getLong(
                                                                            context,
                                                                            "amount"
                                                                    );

                                                            if (!EconomyManager.removeWallet(
                                                                    server,
                                                                    player,
                                                                    amount
                                                            )) {

                                                                player.sendSystemMessage(
                                                                        Component.literal(
                                                                                "You don't have enough money."
                                                                        )
                                                                );

                                                                return 0;
                                                            }

                                                            EconomyManager.addBank(
                                                                    server,
                                                                    player,
                                                                    amount
                                                            );

                                                            player.sendSystemMessage(
                                                                    Component.literal(
                                                                            "Deposited "
                                                                                    + CurrencyFormatter.format(amount)
                                                                                    + " into your bank."
                                                                    )
                                                            );

                                                            return 1;
                                                        })
                                        )
                        )

                        // /bank withdraw <amount>
                        .then(
                                Commands.literal("withdraw")
                                        .then(
                                                Commands.argument(
                                                        "amount",
                                                        LongArgumentType.longArg(1)
                                                )
                                                        .executes(context -> {

                                                            ServerPlayer player =
                                                                    context.getSource()
                                                                            .getPlayerOrException();

                                                            MinecraftServer server =
                                                                    context.getSource()
                                                                            .getServer();

                                                            long amount =
                                                                    LongArgumentType.getLong(
                                                                            context,
                                                                            "amount"
                                                                    );

                                                            if (!EconomyManager.removeBank(
                                                                    server,
                                                                    player,
                                                                    amount
                                                            )) {

                                                                player.sendSystemMessage(
                                                                        Component.literal(
                                                                                "You don't have enough money in your bank."
                                                                        )
                                                                );

                                                                return 0;
                                                            }

                                                            EconomyManager.addWallet(
                                                                    server,
                                                                    player,
                                                                    amount
                                                            );

                                                            player.sendSystemMessage(
                                                                    Component.literal(
                                                                            "Withdrew "
                                                                                    + CurrencyFormatter.format(amount)
                                                                                    + " from your bank."
                                                                    )
                                                            );

                                                            return 1;
                                                        })
                                        )
                        )
        );
    }
}