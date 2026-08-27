package com.donno.economy.economy;

public class PlayerEconomy {

    private long wallet;
    private long bank;

    public PlayerEconomy() {
        this(100, 0);
    }

    public PlayerEconomy(long wallet, long bank) {
        this.wallet = wallet;
        this.bank = bank;
    }

    public long getWallet() {
        return wallet;
    }

    public long getBank() {
        return bank;
    }

    public long getTotal() {
        return wallet + bank;
    }

    public void addWallet(long amount) {
        wallet += amount;
    }

    public boolean removeWallet(long amount) {

        if (amount < 0 || wallet < amount) {
            return false;
        }

        wallet -= amount;
        return true;
    }

    public void addBank(long amount) {
        bank += amount;
    }

    public boolean removeBank(long amount) {

        if (amount < 0 || bank < amount) {
            return false;
        }

        bank -= amount;
        return true;
    }
}