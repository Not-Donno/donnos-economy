# Donno's Economy

A Minecraft economy where players earn physical Cash by grinding normal gameplay resources.

## Income sources

Players can grind these activities and then use `/sell` to turn the resources they earned into Cash:

- **Mining:** coal, copper, iron, gold, redstone, lapis, diamonds, emeralds, quartz, ancient debris and netherite scraps.
- **Farming:** wheat, carrots, potatoes, beetroot, sugar cane, bamboo, cactus, cocoa beans, berries, melons, pumpkins and apples.
- **Fishing:** cod, salmon, tropical fish and pufferfish.
- **Woodcutting:** all supported overworld logs.
- **Mob grinding:** rotten flesh, bones, string, spider eyes, gunpowder, slime balls, ender pearls, blaze rods, ghast tears and magma cream.

Every vanilla Minecraft 26.2 item is sellable. The base price is NRs 0.1 per item, with compact price groups making wood NRs 0.5 and selected resources slightly more valuable. Prices are intentionally very low.

## Commands

- `/sell` — sells every supported grind resource in your inventory and gives you physical Cash.
- `/sell prices` — explains the sell system.
- `/balance` — shows wallet, bank and total wealth.
- `/bank` — shows bank balance.
- `/bank deposit <amount>` — deposits physical Cash.
- `/bank withdraw <amount>` — withdraws Cash from the bank.
- `/rep` — shows reputation.
- `/bounty` — shows bounty.

Cash uses a physical NRs 0.1 coin plus NRs 10, 20, 50, 100, 500, and 1,000 notes. Internally, money is stored in tenths of a rupee so prices such as NRs 0.1 and NRs 0.5 work exactly.

## Design goal

Money should come from player activity rather than being created for free. A player has to go mining, farming, fishing, woodcutting, or mob grinding, collect resources, and sell those resources before they can build up an economy balance.

## License

This template is available under the CC0 license.

## Physical Cash Notes
Cash is represented by six physical denominations: **NRs 10, 20, 50, 100, 500, and 1,000**. The economy automatically gives change using these notes, and `/balance`, `/bank deposit`, and `/bank withdraw` account for their face values.


## Selling controls

- `/sell` — sell all supported resources.
- `/sell <item>` — sell all of one specific item.
- `/sell <item> <amount>` — sell only the requested amount and keep the rest; the command fails if you do not own enough of the item.
- `/price <item>` — check the current sell price and how many you own.

Player kills cost **-10 reputation**. Player death resets both reputation and bounty to 0. Bounty is **NRs 25 per negative reputation point**, capped at **NRs 1,000**.
