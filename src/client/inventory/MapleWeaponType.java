package client.inventory;

public enum MapleWeaponType {
    NOT_A_WEAPON(1.43f, 20),//没有武器
    BOW(1.2f, 15),//弓
    CLAW(1.75f, 15),//圈套
    DAGGER(1.3f, 20),//短剑
    CROSSBOW(1.35f, 15),//弩
    AXE1H(1.2f, 20),//单手斧
    SWORD1H(1.2f, 20),//单手剑
    BLUNT1H(1.2f, 20),//单手钝器
    AXE2H(1.32f, 20),//双手斧
    SWORD2H(1.32f, 20),//双手剑
    BLUNT2H(1.32f, 20),//双手钝器
    POLE_ARM(1.49f, 20), //枪
    SPEAR(1.49f, 20),//矛
    STAFF(1.0f, 25),//长杖
    WAND(1.0f, 25),//短杖
    KNUCKLE(1.7f, 20),//指节
    GUN(1.5f, 15),//短枪
    CANNON(1.35f, 15),//手持火炮
    DUAL_BOW(2.0f, 15), //双弩枪
    MAGIC_ARROW(2.0f, 15),//特殊副手
    KATARA(1.3f, 20),//双刀副手
    ;
    
    private final float damageMultiplier;
    private final int baseMastery;

    private MapleWeaponType(final float maxDamageMultiplier, int baseMastery) {
        this.damageMultiplier = maxDamageMultiplier;
	this.baseMastery = baseMastery;
    }

    public final float getMaxDamageMultiplier() {
        return damageMultiplier;
    }

    public final int getBaseMastery() {
	return baseMastery;
    }
};
