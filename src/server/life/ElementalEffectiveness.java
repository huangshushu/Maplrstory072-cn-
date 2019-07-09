package server.life;

public enum ElementalEffectiveness {
    NORMAL(1.0), IMMUNE(0.0), STRONG(0.5), WEAK(1.5);

    private double value;
    
    private ElementalEffectiveness(double val) {
	this.value = val;
    }

    public double getValue() {
	return value;
    }

    public static ElementalEffectiveness getByNumber(int num) {
        switch (num) {
            case 1:
                return IMMUNE;
            case 2:
                return STRONG;
            case 3:
                return WEAK;
            default:
                throw new IllegalArgumentException("Unkown effectiveness: " + num);
        }
    }
}
