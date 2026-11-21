/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  lightning.Night
 *  lightning.modules.impl.core.CapesModule
 *  lightning.utils.IMinecraft
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.core.ClientAsset$ResourceTexture
 *  net.minecraft.core.ClientAsset$Texture
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.player.PlayerSkin
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import com.mojang.authlib.GameProfile;
import lightning.Night;
import lightning.modules.impl.core.CapesModule;
import lightning.utils.IMinecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerInfo.class})
public class PlayerListEntryMixin
implements IMinecraft {
    @Shadow
    @Final
    private GameProfile profile;

    @Inject(method={"getSkin"}, at={@At(value="TAIL")}, cancellable=true)
    private void getSkinTextures(CallbackInfoReturnable<PlayerSkin> callbackInfoReturnable) {
        try {
            if (this.profile.name().equals(PlayerListEntryMixin.mc.player.getGameProfile().name()) && this.profile.id().equals(PlayerListEntryMixin.mc.player.getGameProfile().id()) && ((CapesModule)Night.MODULE_MANAGER.getModule(CapesModule.class)).isToggled() && ((CapesModule)Night.MODULE_MANAGER.getModule(CapesModule.class)).getCapeTexture() != null) {
                Identifier identifier = ((CapesModule)Night.MODULE_MANAGER.getModule(CapesModule.class)).getCapeTexture();
                PlayerSkin playerSkin = (PlayerSkin)callbackInfoReturnable.getReturnValue();
                callbackInfoReturnable.setReturnValue(new PlayerSkin(playerSkin.body(), (ClientAsset.Texture)new ClientAsset.ResourceTexture(identifier), playerSkin.elytra(), playerSkin.model(), playerSkin.secure()));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

