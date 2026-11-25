/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 */
package lightning.modules.impl.player;

import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.visuals.BlockHighlightModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.InventoryUtils;
import lightning.utils.minecraft.WorldUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@RegisterModule(name="AirPlace", description="Lets you place blocks in the air in servers that allow it.", category=Module.Category.PLAYER)
public class AirPlaceModule
extends Module {
    public BooleanSetting grim = new BooleanSetting("Grim", "Swaps the block into your offhand for just the instant of each placement -- GrimAC's whole scaffolding check group (AirLiquidPlace, RotationPlace, PositionPlace...) only runs for MAIN_HAND placements, verified against its own decompiled source.", false);
    private HitResult hitResult;

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        BlockHitResult blockHitResult;
        block8: {
            block7: {
                if (this.getNull()) {
                    return;
                }
                this.hitResult = mc.getCameraEntity().pick(AirPlaceModule.mc.player.blockInteractionRange(), 0.0f, false);
                HitResult hitResult = this.hitResult;
                if (!(hitResult instanceof BlockHitResult)) break block7;
                blockHitResult = (BlockHitResult)hitResult;
                if (AirPlaceModule.mc.player.getMainHandItem().getItem() instanceof BlockItem) break block8;
            }
            return;
        }
        if (AirPlaceModule.mc.options.keyUse.isDown() && AirPlaceModule.mc.level.getBlockState(blockHitResult.getBlockPos()).isAir()) {
            if (this.grim.getValue()) {
                int selectedSlot = AirPlaceModule.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.swapEquipment(selectedSlot, 45);
                WorldUtils.airPlaceBlock(blockHitResult.getBlockPos(), InteractionHand.OFF_HAND, "None", true);
                InventoryUtils.swapEquipment(selectedSlot, 45);
            } else {
                WorldUtils.airPlaceBlock(blockHitResult.getBlockPos(), InteractionHand.MAIN_HAND, "None", true);
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        BlockHitResult blockHitResult;
        HitResult hitResult;
        BlockHighlightModule blockHighlightModule;
        block6: {
            block5: {
                if (this.getNull()) {
                    return;
                }
                blockHighlightModule = Night.MODULE_MANAGER.getModule(BlockHighlightModule.class);
                hitResult = this.hitResult;
                if (!(hitResult instanceof BlockHitResult)) break block5;
                blockHitResult = (BlockHitResult)hitResult;
                if (AirPlaceModule.mc.player.getMainHandItem().getItem() instanceof BlockItem) break block6;
            }
            return;
        }
        if (AirPlaceModule.mc.level.getBlockState(blockHitResult.getBlockPos()).isAir()) {
            AABB aabb = new AABB(blockHitResult.getBlockPos());
            Renderer3D.renderBox(renderWorldEvent.getMatrices(), aabb, blockHighlightModule.fillColor.getColor());
            Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aabb, blockHighlightModule.outlineColor.getColor());
        }
    }
}

