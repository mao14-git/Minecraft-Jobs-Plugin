package org.example;

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

    public int getWoodcuttingEXP() {
        return woodcuttingXP;
    }

    public void setWoodcuttingXP(int woodcuttingXP) {
        this.woodcuttingXP = woodcuttingXP;
    }


    // Helper method to handle Exp gain and check for level-ups
    public void addMiningEXP(int amount) {
        miningXP += amount;
        int xpNeeded = 100 * (miningLevel + 1);
        while (miningXP >= xpNeeded) {
            miningXP -= xpNeeded;
            miningLevel++;
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
