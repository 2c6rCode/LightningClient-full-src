/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderPass$RenderArea
 *  com.mojang.blaze3d.systems.RenderPassBackend
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassBackend;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={RenderPass.class})
public abstract class RenderPassMixin {
    @Shadow
    @Final
    private RenderPass.RenderArea renderArea;
    @Shadow
    @Final
    private RenderPassBackend backend;

    @Shadow
    public abstract void disableScissor();

    @Inject(method={"enableScissor"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$clampScissorToBounds(int n, int n2, int n3, int n4, CallbackInfo callbackInfo) {
        if (this.renderArea == null) {
            return;
        }
        int n5 = this.renderArea.x();
        int n6 = this.renderArea.y();
        int n7 = this.renderArea.width();
        int n8 = this.renderArea.height();
        int n9 = n;
        int n10 = n2;
        int n11 = n + n3;
        int n12 = n2 + n4;
        if (n9 < n5) {
            n9 = n5;
        }
        if (n10 < n6) {
            n10 = n6;
        }
        if (n11 > n5 + n7) {
            n11 = n5 + n7;
        }
        if (n12 > n6 + n8) {
            n12 = n6 + n8;
        }
        int n13 = n11 - n9;
        int n14 = n12 - n10;
        if (n13 <= 0 || n14 <= 0) {
            this.disableScissor();
            callbackInfo.cancel();
            return;
        }
        if (n9 != n || n10 != n2 || n13 != n3 || n14 != n4) {
            this.backend.enableScissor(n9, n10, n13, n14);
            callbackInfo.cancel();
        }
    }
}

