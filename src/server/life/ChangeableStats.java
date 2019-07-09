package server.life;

import constants.GameConstants;

public class ChangeableStats extends OverrideMonsterStats {
    public int watk, matk, acc, eva, PDRate, MDRate, pushed, level;

    public ChangeableStats(MapleMonsterStats stats, OverrideMonsterStats ostats) {
        hp = ostats.getHp();
        exp = ostats.getExp();
        mp = ostats.getMp();
        watk = stats.getPhysicalAttack();
        matk = stats.getMagicAttack();
        acc = stats.getAcc();
        eva = stats.getEva();
        PDRate = stats.getPDRate();
        MDRate = stats.getMDRate();
        pushed = stats.getPushed();
        level = stats.getLevel();
    }

    public ChangeableStats(MapleMonsterStats stats, int newLevel, boolean pqMob) {
        final double mod = (double) newLevel / (double) stats.getLevel();
        final double hpRatio = (double) stats.getHp() / (double) stats.getExp();
        final double pqMod = (pqMob ? 2.5 : 1.0);
        hp = (long) Math.round((!stats.isBoss() ? GameConstants.getMonsterHP(newLevel) : (stats.getHp() * mod)) * pqMod);
        exp = (int) Math.round((!stats.isBoss() ? (GameConstants.getMonsterHP(newLevel) / hpRatio) : (stats.getExp() * mod)) * mod * pqMod);
        mp = (int) Math.round(stats.getMp() * mod * pqMod);
        watk = (int) Math.round(stats.getPhysicalAttack() * mod);
        matk = (int) Math.round(stats.getMagicAttack() * mod);
        acc = (int) Math.round(stats.getAcc() + Math.max(0, newLevel - stats.getLevel()) * 2);
        eva = (int) Math.round(stats.getEva() + Math.max(0, newLevel - stats.getLevel()));
        PDRate = Math.min(stats.isBoss() ? 30 : 20, (int) Math.round(stats.getPDRate() * mod));
        MDRate = Math.min(stats.isBoss() ? 30 : 20, (int) Math.round(stats.getMDRate() * mod));
        pushed = (int) Math.round(stats.getPushed() * mod);
        level = newLevel;
    }
}
