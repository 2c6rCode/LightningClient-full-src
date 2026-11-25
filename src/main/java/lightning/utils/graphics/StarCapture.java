/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.GpuFormat
 *  com.mojang.blaze3d.pipeline.TextureTarget
 */
package lightning.utils.graphics;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.TextureTarget;
import lightning.utils.IMinecraft;

public class StarCapture
implements IMinecraft {
    private static TextureTarget target;

    public static TextureTarget ensure() {
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        if (target == null || StarCapture.target.width != width || StarCapture.target.height != height) {
            if (target != null) {
                target.destroyBuffers();
            }
            target = new TextureTarget("night_star_capture", width, height, true, GpuFormat.RGBA8_UNORM);
        }
        return target;
    }

    public static TextureTarget get() {
        return target;
    }
}

