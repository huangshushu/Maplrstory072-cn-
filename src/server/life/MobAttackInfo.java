package server.life;

import java.awt.Point;

public class MobAttackInfo {
    private boolean isDeadlyAttack;
    private int mpBurn, mpCon;
    private int diseaseSkill, diseaseLevel;
    public int PADamage, MADamage, attackAfter, range = 0;
    public Point lt = null, rb = null;
    public boolean magic = false;

    public void setDeadlyAttack(boolean isDeadlyAttack) {
        this.isDeadlyAttack = isDeadlyAttack;
    }

    public boolean isDeadlyAttack() {
        return isDeadlyAttack;
    }

    public void setMpBurn(int mpBurn) {
        this.mpBurn = mpBurn;
    }

    public int getMpBurn() {
        return mpBurn;
    }

    public void setDiseaseSkill(int diseaseSkill) {
        this.diseaseSkill = diseaseSkill;
    }

    public int getDiseaseSkill() {
        return diseaseSkill;
    }

    public void setDiseaseLevel(int diseaseLevel) {
        this.diseaseLevel = diseaseLevel;
    }

    public int getDiseaseLevel() {
        return diseaseLevel;
    }

    public void setMpCon(int mpCon) {
        this.mpCon = mpCon;
    }

    public int getMpCon() {
        return mpCon;
    }

    public int getRange() {
	final int maxX = Math.max(Math.abs(lt == null ? 0 : lt.x), Math.abs(rb == null ? 0 : rb.x));
	final int maxY = Math.max(Math.abs(lt == null ? 0 : lt.y), Math.abs(rb == null ? 0 : rb.y));
	return Math.max((maxX * maxX) + (maxY * maxY), range);
    }
}
