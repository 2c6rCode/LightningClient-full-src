/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.core.FontModule
 *  net.minecraft.client.renderer.state.gui.GuiRenderState
 *  net.minecraft.client.renderer.state.gui.GuiTextRenderState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.mixins.accessors.GuiTextRenderStateAccessor;
import lightning.modules.impl.core.FontModule;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GuiRenderState.class})
public class GuiRenderStateMixin {
    @Shadow
    private int firstStratumAfterBlur;

    @Inject(method={"blurBeforeThisStratum"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$noopIfAlreadyBlurredThisFrame(CallbackInfo callbackInfo) {
        if (this.firstStratumAfterBlur != Integer.MAX_VALUE) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"addText"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$globalFont(GuiTextRenderState guiTextRenderState, CallbackInfo callbackInfo) {
        try {
            FontModule fontModule;
            if (Night.MODULE_MANAGER != null && (fontModule = (FontModule)Night.MODULE_MANAGER.getModule(FontModule.class)) != null && fontModule.isToggled() && fontModule.customFont.getValue() && fontModule.global.getValue() && Night.FONT_MANAGER != null && Night.FONT_MANAGER.getFontRenderer() != null) {
                GuiTextRenderStateAccessor guiTextRenderStateAccessor = (GuiTextRenderStateAccessor)(Object)guiTextRenderState;
                GuiRenderState guiRenderState = (GuiRenderState)(Object)this;
                if (guiTextRenderStateAccessor.isDropShadow()) {
                    float shadowOffset = 1.0f;
                    Night.FONT_MANAGER.getFontRenderer().submitGlyphs(guiRenderState, guiTextRenderStateAccessor.getPose(), guiTextRenderStateAccessor.getScissor(), guiTextRenderStateAccessor.getText(), (float)guiTextRenderStateAccessor.getX() + shadowOffset, (float)guiTextRenderStateAccessor.getY() + shadowOffset, guiTextRenderStateAccessor.getColor(), true);
                }
                Night.FONT_MANAGER.getFontRenderer().submitGlyphs(guiRenderState, guiTextRenderStateAccessor.getPose(), guiTextRenderStateAccessor.getScissor(), guiTextRenderStateAccessor.getText(), (float)guiTextRenderStateAccessor.getX(), (float)guiTextRenderStateAccessor.getY(), guiTextRenderStateAccessor.getColor(), false);
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

