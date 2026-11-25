/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.mixins.accessors.GuiGraphicsExtractorAccessor
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.renderer.item.ItemStackRenderState
 *  net.minecraft.client.renderer.item.TrackingItemStackRenderState
 *  net.minecraft.client.renderer.state.gui.GuiItemRenderState
 *  net.minecraft.client.renderer.state.gui.GuiRenderState
 *  net.minecraft.world.entity.ItemOwner
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 */
package lightning.utils.graphics;

import java.util.Objects;
import lightning.mixins.accessors.GuiGraphicsExtractorAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public class HotbarCache {
    private static final SlotEntry[] SLOTS = new SlotEntry[10];

    public static void clear() {
        for (SlotEntry slotEntry : SLOTS) {
            slotEntry.clear();
        }
    }

    public static void extractItem(GuiGraphicsExtractor guiGraphicsExtractor, LivingEntity livingEntity, ItemStack itemStack, int x, int y, int seed) {
        SlotEntry slotEntry;
        if (itemStack.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        GuiRenderState guiRenderState = ((GuiGraphicsExtractorAccessor)guiGraphicsExtractor).night$getGuiRenderState();
        int slotIndex = seed >= 1 && seed <= 10 ? seed - 1 : -1;
        TrackingItemStackRenderState trackingItemStackRenderState = null;
        if (slotIndex >= 0 && slotIndex < SLOTS.length) {
            slotEntry = SLOTS[slotIndex];
            if (slotEntry.matches(itemStack, seed)) {
                trackingItemStackRenderState = slotEntry.renderState;
            } else {
                trackingItemStackRenderState = new TrackingItemStackRenderState();
                if (minecraft.getItemModelResolver() != null && livingEntity != null && livingEntity.level() != null) {
                    minecraft.getItemModelResolver().updateForTopItem((ItemStackRenderState)trackingItemStackRenderState, itemStack, ItemDisplayContext.GUI, livingEntity.level(), (ItemOwner)livingEntity, seed);
                }
                slotEntry.set(itemStack, seed, trackingItemStackRenderState);
            }
        } else {
            trackingItemStackRenderState = new TrackingItemStackRenderState();
            if (minecraft.getItemModelResolver() != null && livingEntity != null && livingEntity.level() != null) {
                minecraft.getItemModelResolver().updateForTopItem((ItemStackRenderState)trackingItemStackRenderState, itemStack, ItemDisplayContext.GUI, livingEntity.level(), (ItemOwner)livingEntity, seed);
            }
        }
        if (guiRenderState != null && trackingItemStackRenderState != null) {
            Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc)guiGraphicsExtractor.pose());
            guiRenderState.addItem(new GuiItemRenderState(pose, trackingItemStackRenderState, x, y, (ScreenRectangle)null));
        }
    }

    static {
        for (int i = 0; i < SLOTS.length; ++i) {
            HotbarCache.SLOTS[i] = new SlotEntry();
        }
    }

    public static class SlotEntry {
        public Item item;
        public int count;
        public int damage;
        public Object components;
        public int seed;
        public TrackingItemStackRenderState renderState;

        public boolean matches(ItemStack itemStack, int seed) {
            if (itemStack == null || itemStack.isEmpty() || this.renderState == null) {
                return false;
            }
            if (this.renderState.isAnimated()) {
                return false;
            }
            return itemStack.getItem() == this.item && itemStack.getCount() == this.count && itemStack.getDamageValue() == this.damage && Objects.equals(itemStack.getComponents(), this.components) && this.seed == seed;
        }

        public void set(ItemStack itemStack, int seed, TrackingItemStackRenderState trackingItemStackRenderState) {
            this.item = itemStack.getItem();
            this.count = itemStack.getCount();
            this.damage = itemStack.getDamageValue();
            this.components = itemStack.getComponents();
            this.seed = seed;
            this.renderState = trackingItemStackRenderState;
        }

        public void clear() {
            this.item = null;
            this.count = 0;
            this.damage = 0;
            this.components = null;
            this.seed = 0;
            this.renderState = null;
        }
    }
}

