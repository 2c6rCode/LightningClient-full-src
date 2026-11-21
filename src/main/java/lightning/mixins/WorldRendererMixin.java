/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  lightning.Night
 *  lightning.modules.impl.visuals.AtmosphereModule
 *  lightning.modules.impl.visuals.BlockHighlightModule
 *  lightning.modules.impl.visuals.ShadersModule
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.ShaderManager
 *  net.minecraft.resources.Identifier
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package lightning.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderTarget;
import java.util.Set;
import lightning.Night;
import lightning.modules.impl.visuals.AtmosphereModule;
import lightning.modules.impl.visuals.BlockHighlightModule;
import lightning.modules.impl.visuals.ShadersModule;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={LevelRenderer.class})
public abstract class WorldRendererMixin {
    @Shadow
    @Final
    private RenderTarget entityOutlineTarget;

    @ModifyReturnValue(method={"entityOutlineTarget"}, at={@At(value="RETURN")})
    private RenderTarget night$outlineTargetOutsideFrameGraph(RenderTarget renderTarget) {
        return renderTarget != null ? renderTarget : this.entityOutlineTarget;
    }

    @ModifyVariable(method={"render"}, at={@At(value="HEAD")}, argsOnly=true, ordinal=0)
    private boolean renderOutline(boolean bl) {
        try {
            if (Night.MODULE_MANAGER != null && ((BlockHighlightModule)Night.MODULE_MANAGER.getModule(BlockHighlightModule.class)).isToggled()) {
                return false;
            }
            return bl;
        }
        catch (NullPointerException nullPointerException) {
            return bl;
        }
    }

    @ModifyVariable(method={"render"}, at={@At(value="HEAD")}, argsOnly=true, ordinal=1)
    private boolean night$forceShouldRenderSky(boolean bl) {
        try {
            AtmosphereModule atmosphereModule = Night.MODULE_MANAGER != null ? (AtmosphereModule)Night.MODULE_MANAGER.getModule(AtmosphereModule.class) : null;
            AtmosphereModule atmosphereModule2 = atmosphereModule;
            if (atmosphereModule != null && atmosphereModule.isToggled() && atmosphereModule.modifySky.getValue()) {
                return true;
            }
            return bl;
        }
        catch (NullPointerException nullPointerException) {
            return bl;
        }
    }

    @Redirect(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;")})
    private PostChain night$interceptEntityOutlineChain(ShaderManager shaderManager, Identifier identifier, Set<Identifier> set) {
        if (identifier.equals((Object)Identifier.withDefaultNamespace((String)"entity_outline")) && ShadersModule.pickActiveOutlineChain() != null) {
            return null;
        }
        return shaderManager.getPostChain(identifier, set);
    }
}

