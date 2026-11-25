/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.visuals;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerMineEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.animations.Easing;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.WorldUtils;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="BreakHighlight", description="Renders blocks that are being mined by other players.", category=Module.Category.VISUALS)
public class BreakHighlightModule
extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The rendering that will be applied to the mine esp.", "Outline", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(this.mode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(this.mode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());
    public BooleanSetting name = new BooleanSetting("Name", "Renders the name of the player mining the block.", true);
    public ColorSetting nameColor = new ColorSetting("NameColor", "The color used for the name text rendering.", new BooleanSetting.Visibility(this.name, true), new ColorSetting.Color(Color.WHITE, false, false));
    public BooleanSetting glow = new BooleanSetting("Glow", "Renders a glow shader effect around the breaker name text.", new BooleanSetting.Visibility(this.name, true), false);
    public NumberSetting scale = new NumberSetting("Scale", "The scale of the name text.", new BooleanSetting.Visibility(this.name, true), (Number)30, (Number)10, (Number)100);
    private final Map<Integer, Mine> mineMap = new HashMap<Integer, Mine>();

    @SubscribeEvent
    public void onPlayerMine(PlayerMineEvent playerMineEvent) {
        Block block;
        if (!this.getNull() && playerMineEvent.getActorID() != BreakHighlightModule.mc.player.getId() && BreakHighlightModule.mc.level != null && !(block = BreakHighlightModule.mc.level.getBlockState(playerMineEvent.getPosition()).getBlock()).equals(Blocks.AIR)) {
            String playerName;
            Entity entity = BreakHighlightModule.mc.level.getEntity(playerMineEvent.getActorID());
            if (entity instanceof Player player) {
                playerName = player.getName().getString();
            } else {
                playerName = "Player";
            }
            float computedBreakTime;
            if (entity instanceof Player player) {
                computedBreakTime = WorldUtils.getBreakTime(player, BreakHighlightModule.mc.level.getBlockState(playerMineEvent.getPosition()));
            } else {
                computedBreakTime = 1000.0f;
            }
            float breakTime = computedBreakTime;
            Mine mine = new Mine(playerMineEvent.getPosition(), block, breakTime, System.currentTimeMillis(), playerName);
            if (!this.mineMap.containsKey(playerMineEvent.getActorID())) {
                this.mineMap.put(playerMineEvent.getActorID(), mine);
            } else if (!this.mineMap.get(playerMineEvent.getActorID()).pos.equals(playerMineEvent.getPosition())) {
                this.mineMap.replace(playerMineEvent.getActorID(), mine);
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent renderWorldEvent) {
        if (this.getNull() || this.mineMap.isEmpty()) {
            return;
        }
        this.mineMap.entrySet().removeIf(entry -> this.clearMine((Integer)entry.getKey(), (Mine)entry.getValue()));
        this.mineMap.forEach((actorId, mine) -> {
            float progress = Easing.toDelta(mine.time, (int)mine.breakTime);
            AABB aABB = new AABB(mine.pos).deflate(0.5).inflate((double)progress / 2.0);
            if (this.mode.getValue().equalsIgnoreCase("Fill") || this.mode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBox(renderWorldEvent.getMatrices(), aABB, this.fillColor.getColor());
            }
            if (this.mode.getValue().equalsIgnoreCase("Outline") || this.mode.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderBoxOutline(renderWorldEvent.getMatrices(), aABB, this.outlineColor.getColor());
            }
            if (this.name.getValue() && mine.playerName != null) {
                Vec3 center = Vec3.atCenterOf((Vec3i)mine.pos);
                int percent = Math.min(100, Math.max(0, (int)(progress * 100.0f)));
                String label = mine.playerName + "\n" + percent + "%";
                Renderer3D.renderCenteredScaledText(renderWorldEvent.getMatrices(), label, center.x, center.y, center.z, this.scale.getValue().intValue(), false, this.nameColor.getColor(), this.glow.getValue());
            }
        });
    }

    private boolean clearMine(int actorId, Mine mine) {
        if (BreakHighlightModule.mc.level == null) {
            return true;
        }
        if (!BreakHighlightModule.mc.level.getBlockState(mine.pos).getBlock().equals(mine.block)) {
            return true;
        }
        if ((float)(System.currentTimeMillis() - mine.time) > mine.breakTime + 3000.0f) {
            return true;
        }
        Entity entity = BreakHighlightModule.mc.level.getEntity(actorId);
        return entity != null && Math.sqrt(entity.distanceToSqr(Vec3.atCenterOf((Vec3i)mine.pos))) > 8.0;
    }

    private static class Mine {
        private final BlockPos pos;
        private final Block block;
        private final float breakTime;
        private final long time;
        private final String playerName;

        @Generated
        public Mine(BlockPos blockPos, Block block, float breakTime, long time, String playerName) {
            this.pos = blockPos;
            this.block = block;
            this.breakTime = breakTime;
            this.time = time;
            this.playerName = playerName;
        }
    }
}

