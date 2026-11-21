/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.Night
 *  lightning.modules.impl.miscellaneous.ExtraTabModule
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.gui.components.PlayerTabOverlay
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.scores.PlayerTeam
 *  net.minecraft.world.scores.Team
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package lightning.mixins;

import java.util.ArrayList;
import lightning.Night;
import lightning.modules.impl.miscellaneous.ExtraTabModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerTabOverlay.class})
public abstract class PlayerListHudMixin {
    @Shadow
    private Component decorateName(PlayerInfo playerInfo, MutableComponent mutableComponent) {
        throw new UnsupportedOperationException();
    }

    @ModifyConstant(method={"getPlayerInfos"}, constant={@Constant(longValue=80L)}, require=0)
    private long extraTabLimit(long l) {
        try {
            ExtraTabModule extraTabModule = (ExtraTabModule)Night.MODULE_MANAGER.getModule(ExtraTabModule.class);
            return extraTabModule.isToggled() ? extraTabModule.limit.getValue().longValue() : l;
        }
        catch (NullPointerException nullPointerException) {
            return l;
        }
    }

    @Inject(method={"getNameForDisplay"}, at={@At(value="HEAD")}, cancellable=true)
    private void getPlayerName(PlayerInfo playerInfo, CallbackInfoReturnable<Component> callbackInfoReturnable) {
        try {
            if (((ExtraTabModule)Night.MODULE_MANAGER.getModule(ExtraTabModule.class)).isToggled() && ((ExtraTabModule)Night.MODULE_MANAGER.getModule(ExtraTabModule.class)).friends.getValue() && Night.FRIEND_MANAGER.contains(playerInfo.getProfile().name())) {
                if (playerInfo.getTabListDisplayName() != null) {
                    MutableComponent mutableComponent = Component.empty();
                    ArrayList<Component> arrayList = new ArrayList<Component>();
                    arrayList.add(playerInfo.getTabListDisplayName().plainCopy());
                    arrayList.addAll(playerInfo.getTabListDisplayName().getSiblings());
                    for (Component component : arrayList) {
                        if (component.getString().equals(playerInfo.getProfile().name())) {
                            mutableComponent.append((Component)Component.literal((String)playerInfo.getProfile().name()).withStyle(ChatFormatting.AQUA));
                            continue;
                        }
                        if (component.getString().equals("] " + playerInfo.getProfile().name())) {
                            mutableComponent.append((Component)Component.literal((String)"] ").withStyle(ChatFormatting.WHITE).append((Component)Component.literal((String)playerInfo.getProfile().name()).withStyle(ChatFormatting.AQUA)));
                            continue;
                        }
                        mutableComponent.append(component);
                    }
                    callbackInfoReturnable.setReturnValue(this.decorateName(playerInfo, mutableComponent));
                    return;
                }
                callbackInfoReturnable.setReturnValue(this.decorateName(playerInfo, PlayerTeam.formatNameForTeam((Team)playerInfo.getTeam(), Component.literal(playerInfo.getProfile().name()))));
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

