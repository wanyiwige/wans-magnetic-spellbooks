package cn.wanyiwige.magneticspellbooks.util;

/** Pure combat rules shared by spell entities, tooltips and deterministic tests. */
public final class MagneticCombatRules {
    public static final float DAMAGE_SCALE = 3.0F;
    private MagneticCombatRules() { }
    public static int level(int level, int max) { return Math.max(1, Math.min(max, level)); }
    public static float collapse(int level, int strength) { return DAMAGE_SCALE * (14 + 4 * (level(level, 3) - 1) + 3 * Math.max(0, Math.min(5, strength))); }
    public static float rail(int level) { return DAMAGE_SCALE * (10 + 3 * (level(level, 5) - 1)); }
    public static int railPierces(int actual, int effective) { return actual == 0 ? 0 : effective >= 4 ? 2 : 1; }
    public static float railDecay(int index) { return index == 0 ? 1 : index == 1 ? 0.5F : 0.25F; }
    public static float anvilPulse(int level) { return DAMAGE_SCALE * (3 + 0.75F * (level(level, 5) - 1)); }
    public static float anvilFinish(int level) { return DAMAGE_SCALE * (8 + 2 * (level(level, 5) - 1)); }
    public static float anvilRadius(int strength) { return 2 + 0.2F * Math.max(0, Math.min(5, strength) - 1); }
    public static float chain(int level, int strength) { return DAMAGE_SCALE * (12 + 4 * (level(level, 3) - 1) + 2 * Math.max(0, Math.min(5, strength) - 1)); }
    public static int chainTargets(int level) { return 2 + level(level, 3); }
    public static float reversal(int level) { return DAMAGE_SCALE * (6 + 2 * (level(level, 5) - 1)); }
    public static float collision(int level) { return DAMAGE_SCALE * (6 + 1.5F * (level(level, 5) - 1)); }
    public static float stormPulse(int level) { return DAMAGE_SCALE * (1.5F + 0.5F * (level(level, 3) - 1)); }
    public static float stormFinish(int level) { return DAMAGE_SCALE * (12 + 4 * (level(level, 3) - 1)); }
    public static float stormMultiplier(int strength) { return 1 + 0.05F * Math.max(0, Math.min(5, strength)); }
    public static double range(String id, int level) {
        return switch (id) {
            case "polarity_collapse", "ferrous_sandstorm" -> 18 + 4 * (level(level, 3) - 1);
            case "magnetic_rail_lance" -> 24 + 4 * (level(level, 5) - 1);
            case "dipole_anvil" -> 16 + 2 * (level(level, 5) - 1);
            case "magnetized_fracture_chain" -> 20 + 4 * (level(level, 3) - 1);
            default -> 12 + 2 * (level(level, 5) - 1);
        };
    }
}

