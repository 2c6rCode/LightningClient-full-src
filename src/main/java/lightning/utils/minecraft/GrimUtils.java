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
        double[] eyeHeights;
        float scale = (float)player.getAttributeValue(Attributes.SCALE);
        double standingHeight = 1.62 * (double)scale;
        double crouchingHeight = 1.27 * (double)scale;
        double swimmingHeight = 0.4 * (double)scale;
        Pose pose = player.getPose();
        switch (pose) {
            case FALL_FLYING: 
            case SPIN_ATTACK: 
            case SWIMMING: {
                eyeHeights = new double[]{swimmingHeight, standingHeight, crouchingHeight};
                break;
            }
            case CROUCHING: {
                eyeHeights = new double[]{crouchingHeight, standingHeight, swimmingHeight};
                break;
            }
            default: {
                eyeHeights = new double[]{standingHeight, crouchingHeight, swimmingHeight};
            }
        }
        return eyeHeights;
    }

    public static Vec3[] getPossibleEyePositions(Player player) {
        Vec3 pos = player.position();
        double[] eyeHeights = GrimUtils.getPossibleEyeHeights(player);
        Vec3[] eyePositions = new Vec3[eyeHeights.length];
        for (int i = 0; i < eyeHeights.length; ++i) {
            eyePositions[i] = pos.add(0.0, eyeHeights[i], 0.0);
        }
        return eyePositions;
    }
}

