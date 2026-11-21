/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  lightning.Night
 *  lightning.modules.impl.core.NoMiddleClickModule
 *  lightning.modules.impl.miscellaneous.ShulkerInfoModule
 *  lightning.modules.impl.movement.ElytraFlyModule
 *  lightning.modules.impl.movement.InventoryControlModule
 *  lightning.modules.impl.player.ChestStealerModule
 *  lightning.utils.IMinecraft
 *  lightning.utils.minecraft.InventoryUtils
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.CraftingScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ContainerInput
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package lightning.mixins;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.List;
import lightning.Night;
import lightning.modules.impl.core.NoMiddleClickModule;
import lightning.modules.impl.miscellaneous.ShulkerInfoModule;
import lightning.modules.impl.movement.ElytraFlyModule;
import lightning.modules.impl.movement.InventoryControlModule;
import lightning.modules.impl.player.ChestStealerModule;
import lightning.utils.IMinecraft;
import lightning.utils.minecraft.InventoryUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={AbstractContainerScreen.class})
public abstract class HandledScreenMixin
extends Screen
implements IMinecraft {
    @Shadow
    @Nullable
    protected Slot hoveredSlot;
    @Shadow
    protected AbstractContainerMenu menu;
    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    @Shadow
    protected int imageWidth;
    private Slot night$lastDragSlot = null;

    @Shadow
    protected abstract void slotClicked(Slot var1, int var2, int var3, ContainerInput var4);

    protected HandledScreenMixin(Component component) {
        super(component);
    }

    @Inject(method={"getTooltipFromContainerItem"}, at={@At(value="RETURN")}, cancellable=true)
    private void night$trimShulkerName(ItemStack itemStack, CallbackInfoReturnable<List<Component>> callbackInfoReturnable) {
        try {
            ShulkerInfoModule shulkerInfoModule = (ShulkerInfoModule)Night.MODULE_MANAGER.getModule(ShulkerInfoModule.class);
            if (shulkerInfoModule == null || !shulkerInfoModule.isToggled() || !shulkerInfoModule.hasItems(itemStack)) {
                return;
            }
            List list = (List)callbackInfoReturnable.getReturnValue();
            if (list == null || list.size() <= 1) {
                return;
            }
            callbackInfoReturnable.setReturnValue(new ArrayList(list.subList(1, list.size())));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @ModifyArgs(method={"extractTooltip"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V")})
    private void night$pushVanillaTooltipBelowShulkerInfo(Args args) {
        try {
            ShulkerInfoModule shulkerInfoModule = (ShulkerInfoModule)Night.MODULE_MANAGER.getModule(ShulkerInfoModule.class);
            if (shulkerInfoModule == null || !shulkerInfoModule.isToggled()) {
                return;
            }
            if (this.hoveredSlot == null || this.hoveredSlot.getItem().isEmpty() || !shulkerInfoModule.hasItems(this.hoveredSlot.getItem())) {
                return;
            }
            int n = (Integer)args.get(4);
            args.set(4, (Object)(n + 20));
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"mouseDragged"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$dragClick(MouseButtonEvent mouseButtonEvent, double d, double d2, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        try {
            InventoryControlModule inventoryControlModule = (InventoryControlModule)Night.MODULE_MANAGER.getModule(InventoryControlModule.class);
            if (!inventoryControlModule.isToggled() || !inventoryControlModule.dragClick.getValue()) {
                return;
            }
            boolean bl = InputConstants.isKeyDown((Window)mc.getWindow(), (int)340) || InputConstants.isKeyDown((Window)mc.getWindow(), (int)344);
            boolean bl2 = bl;
            if (mouseButtonEvent.button() != 0 || !bl) {
                return;
            }
            if (this.hoveredSlot == null || this.hoveredSlot.getItem().isEmpty() || this.hoveredSlot == this.night$lastDragSlot) {
                return;
            }
            this.night$lastDragSlot = this.hoveredSlot;
            this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ContainerInput.QUICK_MOVE);
            callbackInfoReturnable.setReturnValue(true);
            callbackInfoReturnable.cancel();
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Inject(method={"mouseReleased"}, at={@At(value="HEAD")})
    private void night$dragClickReset(MouseButtonEvent mouseButtonEvent, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        this.night$lastDragSlot = null;
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void night$chestStealerButtons(CallbackInfo callbackInfo) {
        try {
            ChestStealerModule chestStealerModule;
            if (Night.MODULE_MANAGER != null && (chestStealerModule = (ChestStealerModule)Night.MODULE_MANAGER.getModule(ChestStealerModule.class)) != null && chestStealerModule.isToggled() && chestStealerModule.buttonMode.getValue()) {
                boolean bl = (Object)this instanceof InventoryScreen;
                boolean bl2 = (Object)this instanceof CraftingScreen;
                if (bl) {
                    int n = this.leftPos + this.imageWidth - 60;
                    int n2 = this.topPos - 22;
                    this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Drop"), button -> chestStealerModule.triggerDrop()).bounds(n, n2, 60, 18).build());
                } else if (bl2) {
                    int n = 46;
                    int n3 = 18;
                    int n4 = 3;
                    int n5 = this.leftPos + this.imageWidth - n;
                    int n6 = this.topPos - 22;
                    this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Drop"), button -> chestStealerModule.triggerDrop()).bounds(n5, n6, n, n3).build());
                    this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Dump"), button -> chestStealerModule.triggerDump()).bounds(n5 -= n + n4, n6, n, n3).build());
                    this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Steal"), button -> chestStealerModule.triggerSteal()).bounds(n5 -= n + n4, n6, n, n3).build());
                } else {
                    int n = this.leftPos + this.imageWidth + 4;
                    int n7 = this.topPos;
                    this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Steal"), button -> chestStealerModule.triggerSteal()).bounds(n, n7, 60, 20).build());
                    this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Dump"), button -> chestStealerModule.triggerDump()).bounds(n, n7 + 22, 60, 20).build());
                    this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Drop"), button -> chestStealerModule.triggerDrop()).bounds(n, n7 + 44, 60, 20).build());
                }
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }

    @Redirect(method={"extractSlot"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/inventory/Slot;getItem()Lnet/minecraft/world/item/ItemStack;")})
    private ItemStack night$grimSwapVisual(Slot slot) {
        ItemStack itemStack;
        ItemStack itemStack2 = slot.getItem();
        if (Night.MODULE_MANAGER == null) {
            return itemStack2;
        }
        ElytraFlyModule elytraFlyModule = (ElytraFlyModule)Night.MODULE_MANAGER.getModule(ElytraFlyModule.class);
        int n = elytraFlyModule.getGrimParkedSlot();
        if (n == -1) {
            return itemStack2;
        }
        if (slot.index == 6 && (itemStack = elytraFlyModule.getGrimHiddenElytra()) != null) {
            return itemStack;
        }
        if (slot.index == InventoryUtils.indexToSlot((int)n)) {
            return elytraFlyModule.getGrimParkedDisplaced();
        }
        return itemStack2;
    }

    @Inject(method={"slotClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void night$onSlotClicked(Slot slot, int n, int n2, ContainerInput containerInput, CallbackInfo callbackInfo) {
        try {
            NoMiddleClickModule noMiddleClickModule;
            if (containerInput == ContainerInput.CLONE && Night.MODULE_MANAGER != null && (noMiddleClickModule = (NoMiddleClickModule)Night.MODULE_MANAGER.getModule(NoMiddleClickModule.class)) != null && noMiddleClickModule.isToggled()) {
                callbackInfo.cancel();
            }
            return;
        }
        catch (NullPointerException nullPointerException) {
            return;
        }
    }
}

