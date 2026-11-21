/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.core.ClickGuiModule
 *  lightning.utils.graphics.NeekeriFill
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractButton
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package lightning.mixins;

import lightning.Night;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.utils.graphics.NeekeriFill;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={AbstractButton.class})
public abstract class ButtonWidgetMixin {
    @Inject(method={"extractDefaultSprite"}, at={@At(value="TAIL")})
    private void night$neekeriFill(GuiGraphicsExtractor guiGraphicsExtractor, CallbackInfo callbackInfo) {
        try {
            ClickGuiModule clickGuiModule = (ClickGuiModule)Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
            if (clickGuiModule.fillMode.getValue().equalsIgnoreCase("Default")) {
                return;
            }
            AbstractButton abstractButton = (AbstractButton)(Object)this;
            int n = Math.round(clickGuiModule.neekeriOpacity.getValue().floatValue() / 100.0f * 255.0f);
            NeekeriFill.fill((GuiGraphicsExtractor)guiGraphicsExtractor, (int)abstractButton.getX(), (int)abstractButton.getY(), (int)abstractButton.getWidth(), (int)abstractButton.getHeight(), (int)n);
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

