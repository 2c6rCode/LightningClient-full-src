/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package lightning.utils.minecraft;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class GrimUtils {
    private GrimUtils() {
    }

    public static double[] getPossibleEyeHeights(Player player) {
        double[] dArray;
        float f = (float)player.getAttributeValue(Attributes.SCALE);
        double d = 1.62 * (double)f;
        double d2 = 1.27 * (double)f;
        double d3 = 0.4 * (double)f;
        Pose pose = player.getPose();
        switch (pose) {
            case FALL_FLYING: 
            case SPIN_ATTACK: 
            case SWIMMING: {
                double[] dArray2 = new double[3];
                dArray2[0] = d3;
                dArray2[1] = d;
                dArray = dArray2;
                dArray2[2] = d2;
                break;
            }
            case CROUCHING: {
                double[] dArray3 = new double[3];
                dArray3[0] = d2;
                dArray3[1] = d;
                dArray = dArray3;
                dArray3[2] = d3;
                break;
            }
            default: {
                double[] dArray4 = new double[3];
                dArray4[0] = d;
                dArray4[1] = d2;
                dArray = dArray4;
                dArray4[2] = d3;
            }
        }
        return dArray;
    }

    public static Vec3[] getPossibleEyePositions(Player player) {
        Vec3 vec3 = player.position();
        double[] dArray = GrimUtils.getPossibleEyeHeights(player);
        Vec3[] vec3Array = new Vec3[dArray.length];
        for (int i = 0; i < dArray.length; ++i) {
            vec3Array[i] = vec3.add(0.0, dArray[i], 0.0);
        }
        return vec3Array;
    }
}

