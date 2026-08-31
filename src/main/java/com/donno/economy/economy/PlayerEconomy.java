package com.donno.economy.economy;

public class PlayerEconomy {

    private long bank;
    private long legacyWallet;
    private int reputation;
    private long bounty;

    public PlayerEconomy() {
        this(0, 0, 0, 0);
    }

    public PlayerEconomy(long bank) {
        this(bank, 0, 0, 0);
    }

    public PlayerEconomy(long bank, long legacyWallet) {
        this(bank, legacyWallet, 0, 0);
    }

    public PlayerEconomy(long bank, long legacyWallet, int reputation, long bounty) {
        this.bank = Math.max(0, bank);
        this.legacyWallet = Math.max(0, legacyWallet);
        this.reputation = reputation;
        this.bounty = Math.max(0, bounty);
    }

    public long getBank() {
        return bank;
    }

    public long getLegacyWallet() {
        return legacyWallet;
    }

    public void clearLegacyWallet() {
        legacyWallet = 0;
    }

    public int getReputation() {
        return reputation;
    }

    public long getBounty() {
        return bounty;
    }

    public void changeReputation(int amount) {
        reputation = Math.addExact(reputation, amount);
    }

    public void resetReputation() {
        reputation = 0;
    }

    public void addBounty(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bounty amount must be positive");
        }
        bounty = Math.addExact(bounty, amount);
    }

    public long claimBounty() {
        long claimed = bounty;
        bounty = 0;
        return claimed;
    }

    public long getTotal(long wallet) {
        return Math.addExact(wallet, bank);
    }

    public void addBank(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Bank amount must be positive");
        }
        bank = Math.addExact(bank, amount);
    }

    public boolean removeBank(long amount) {
        if (amount <= 0 || bank < amount) {
            return false;
        }

        bank -= amount;
        return true;
    }
}
