package org.example;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class JobData {

    // Mining fields
    private int miningLevel;
    private int miningXP;

    // Woodcutting fields
    private int woodcuttingLevel;
    private int woodcuttingXP;

    public JobData(int miningLevel, int miningXP, int woodcuttingLevel, int woodcuttingXP) {
        this.miningLevel = miningLevel;
        this.miningXP = miningXP;
        this.woodcuttingLevel = woodcuttingLevel;
        this.woodcuttingXP = woodcuttingXP;
    }


    // Mining
    public int getMiningLevel() {
        return miningLevel;
    }

    public void setMiningLevel(int miningLevel) {
        this.miningLevel = miningLevel;
    }

    public int getMiningExp() {
        return miningXP;
    }

    public void setMiningXP(int miningXP) {
        this.miningXP = miningXP;
    }


    // Woodcutting
    public int getWoodcuttingLevel() {
        return woodcuttingLevel;
    }

    public void setWoodcuttingLevel(int woodcuttingLevel) {
        this.woodcuttingLevel = woodcuttingLevel;
    }

    public int getWoodcuttingExp() {
        return woodcuttingXP;
    }

    public void setWoodcuttingXP(int woodcuttingXP) {
        this.woodcuttingXP = woodcuttingXP;
    }


    // Helper method to handle Exp gain and check for level-ups
    public void addMiningEXP(int amount, Player player) {
        miningXP += amount;
        int xpNeeded = 100 * (miningLevel + 1);
        while (miningXP >= xpNeeded) {
            miningXP -= xpNeeded;
            miningLevel++;
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 12000, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 12000, 0));
            xpNeeded = 100 * (miningLevel + 1);
        }
    }

    public void addWoodcuttingEXP(int amount) {
        woodcuttingXP += amount;
        int xpNeeded = 100 * (woodcuttingLevel + 1);
        while (woodcuttingXP >= xpNeeded) {
            woodcuttingXP -= xpNeeded;
            woodcuttingLevel++;
            xpNeeded = 100 * (woodcuttingLevel + 1);
        }
    }
}
