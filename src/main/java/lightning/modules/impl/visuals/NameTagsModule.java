/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.ItemOwner
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 */
package lightning.modules.impl.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.RenderWorldEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.api.IFreecamModule;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import lightning.modules.impl.visuals.LogoutSpotModule;
import lightning.modules.impl.visuals.PopChamsModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ColorSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.color.ColorUtils;
import lightning.utils.graphics.Renderer3D;
import lightning.utils.minecraft.EntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

@RegisterModule(name="NameTags", description="Replaces the default Minecraft NameTag with a more visible and customizable one.", category=Module.Category.VISUALS)
public class NameTagsModule
extends Module {
    private static final Identifier LOGO_ID = Identifier.fromNamespaceAndPath((String)"lightning", (String)"textures/gui/nametag_bolt.png");
    public BooleanSetting gameMode = new BooleanSetting("GameMode", "Renders the player's gamemode.", false);
    public BooleanSetting ping = new BooleanSetting("Ping", "Renders the player's latency to the server.", true);
    public BooleanSetting entityId = new BooleanSetting("EntityID", "Renders the player's entity ID.", false);
    public BooleanSetting health = new BooleanSetting("Health", "Renders the player's health and absorption.", true);
    public BooleanSetting totemPops = new BooleanSetting("TotemPops", "Renders the amount of totems that the player has popped.", true);
    public BooleanSetting antiBot = new BooleanSetting("AntiBot", "Prevents bots from having nametags rendered for them.", false);
    public BooleanSetting nightCheck = new BooleanSetting("LightningCheck", "Adds an indicator next to the name of other Lightning users.", true);
    public BooleanSetting self = new BooleanSetting("Self", "Also renders a nametag above your own player.", false);
    public BooleanSetting items = new BooleanSetting("Items", "Renders the items that the player is wearing or holding.", true);
    public BooleanSetting enchantments = new BooleanSetting("Enchantments", "Renders the enchantments of the player's items.", false);
    public BooleanSetting durability = new BooleanSetting("Durability", "Renders the durability of the player's items.", true);
    public BooleanSetting itemName = new BooleanSetting("ItemName", "Renders the name of the item that the player is currently holding.", true);
    public BooleanSetting glow = new BooleanSetting("Glow", "Renders a glow shader effect around nametag text.", false);
    public NumberSetting scale = new NumberSetting("Scale", "The scaling that will be applied to the nametag rendering.", 30, 10, 100);
    public ModeSetting border = new ModeSetting("Border", "The border that will surround the text.", "Both", new String[]{"None", "Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color that will be used for the fill rendering.", new ModeSetting.Visibility(this.border, "Fill", "Both"), new ColorSetting.Color(new Color(0, 0, 0, 100), false, false));
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color that will be used for the outline rendering.", new ModeSetting.Visibility(this.border, "Outline", "Both"), new ColorSetting.Color(new Color(0, 0, 0, 100), false, false));
    private static final Map<ItemEnchantments, List<String>> ENCHANT_CACHE = new ConcurrentHashMap<ItemEnchantments, List<String>>();
    private static final Map<Holder<Enchantment>, String> NAME_CACHE = new ConcurrentHashMap<Holder<Enchantment>, String>();

    @SubscribeEvent
    public void onRenderWorld(RenderWorldEvent.Post post) {
        if (NameTagsModule.mc.level == null || NameTagsModule.mc.player == null) {
            return;
        }
        PoseStack poseStack = post.getMatrices();
        for (Player player : NameTagsModule.mc.level.players().stream().sorted(Comparator.comparing(abstractClientPlayer -> Float.valueOf(-NameTagsModule.mc.player.distanceTo((Entity)abstractClientPlayer)))).toList()) {
            boolean isFreecam = ((IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"))).isToggled();
            if (player == NameTagsModule.mc.player && (!this.self.getValue() || NameTagsModule.mc.options.getCameraType().isFirstPerson() && !isFreecam) || this.antiBot.getValue() && EntityUtils.isBot(player)) continue;
            if (player instanceof RemotePlayer) {
                RemotePlayer remotePlayer = (RemotePlayer)player;
                if (Night.MODULE_MANAGER.getModule(PopChamsModule.class) != null && Night.MODULE_MANAGER.getModule(PopChamsModule.class).isGhost((Entity)remotePlayer) || Night.MODULE_MANAGER.getModule(LogoutSpotModule.class) != null && Night.MODULE_MANAGER.getModule(LogoutSpotModule.class).isGhost((Entity)remotePlayer)) continue;
            }
            if (!Renderer3D.isFrustumVisible(player.getBoundingBox())) continue;
            double px = Mth.lerp((double)post.getTickDelta(), (double)player.xo, (double)player.getX());
            double py = Mth.lerp((double)post.getTickDelta(), (double)player.yo, (double)player.getY()) + (double)(player.isShiftKeyDown() ? 1.9f : 2.1f);
            double pz = Mth.lerp((double)post.getTickDelta(), (double)player.zo, (double)player.getZ());
            Vec3 relPos = new Vec3(px - NameTagsModule.mc.gameRenderer.mainCamera().position().x, py - NameTagsModule.mc.gameRenderer.mainCamera().position().y, pz - NameTagsModule.mc.gameRenderer.mainCamera().position().z);
            float distance = (float)Math.sqrt(NameTagsModule.mc.gameRenderer.mainCamera().position().distanceToSqr(px, py, pz));
            float tagScale = 0.0018f + (float)this.scale.getValue().intValue() / 10000.0f * distance;
            if ((double)distance <= 8.0) {
                tagScale = 0.0245f;
            }
            poseStack.pushPose();
            poseStack.translate(relPos.x, relPos.y, relPos.z);
            poseStack.mulPose((Quaternionfc)NameTagsModule.mc.gameRenderer.mainCamera().rotation());
            poseStack.scale(tagScale, -tagScale, tagScale);
            boolean isLightningUser = this.nightCheck.getValue() && Night.NIGHT_USER_MANAGER != null && Night.NIGHT_USER_MANAGER.isNightUser(player);
            String label = player.getName().getString();
            if (this.gameMode.getValue()) {
                label = label + " [" + EntityUtils.getGameModeName(EntityUtils.getGameMode(player)) + "]";
            }
            if (this.ping.getValue()) {
                label = label + " " + EntityUtils.getLatency(player) + "ms";
            }
            if (this.entityId.getValue()) {
                label = label + " " + player.getId();
            }
            if (this.health.getValue()) {
                int totalHealth = Math.round(player.getHealth() + player.getAbsorptionAmount());
                label = label + " " + String.valueOf(ColorUtils.getHealthColor(player.getHealth() + player.getAbsorptionAmount())) + totalHealth + String.valueOf(ChatFormatting.RESET);
            }
            int poppedTotems = Night.WORLD_MANAGER.getPoppedTotems().getOrDefault(player.getUUID(), 0);
            if (this.totemPops.getValue() && poppedTotems > 0) {
                label = label + " " + String.valueOf(ColorUtils.getTotemColor(poppedTotems)) + "-" + poppedTotems;
            }
            int textWidth = Night.FONT_MANAGER.getWidth(label);
            int textHeight = Night.FONT_MANAGER.getHeight();
            int logoWidth = isLightningUser ? textHeight - 1 : 0;
            int logoGap = isLightningUser ? 3 : 0;
            int totalWidth = textWidth + logoWidth + logoGap;
            if (this.border.getValue().equalsIgnoreCase("Fill") || this.border.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderQuad(poseStack, (float)(-totalWidth) / 2.0f - 2.0f, -textHeight - 1, (float)totalWidth / 2.0f + 2.0f, 1.0f, this.fillColor.getColor());
            }
            if (this.border.getValue().equalsIgnoreCase("Outline") || this.border.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderOutline(poseStack, (float)(-totalWidth) / 2.0f - 2.0f, -textHeight - 1, (float)totalWidth / 2.0f + 2.0f, 1.0f, this.outlineColor.getColor());
            }
            float textX = (float)(-totalWidth) / 2.0f;
            if (isLightningUser) {
                Renderer3D.renderTexture(poseStack, LOGO_ID, textX, (float)(-textHeight) + 0.5f, textX + (float)logoWidth, 0.5f, -1);
                textX += (float)(logoWidth + logoGap);
            }
            Color color = Night.MODULE_MANAGER.getModule(FakePlayerModule.class).isToggled() && Night.MODULE_MANAGER.getModule(FakePlayerModule.class).getPlayer() == player ? new Color(225, 0, 70) : (player.isShiftKeyDown() ? new Color(255, 170, 0) : (Night.FRIEND_MANAGER.contains(player.getName().getString()) ? Night.FRIEND_MANAGER.getDefaultFriendColor() : Color.WHITE));
            this.drawText(poseStack, label, (int)textX, -textHeight, color);
            float itemY = -textHeight - 2;
            if (this.items.getValue()) {
                ItemStack itemStack;
                int slotIdx;
                ItemStack[] playerItems = new ItemStack[6];
                List<String>[] enchantLists = (List<String>[]) new List[6];
                int maxEnchants = 0;
                boolean hasAnyItem = false;
                for (slotIdx = 0; slotIdx < 6; ++slotIdx) {
                    ItemStack stack = this.getItem(player, slotIdx);
                    playerItems[slotIdx] = stack;
                    if (stack.isEmpty()) continue;
                    hasAnyItem = true;
                    if (!this.enchantments.getValue()) continue;
                    List<String> enchants = this.getCachedEnchantments(stack);
                    enchantLists[slotIdx] = enchants;
                    if (enchants.size() <= maxEnchants) continue;
                    maxEnchants = enchants.size();
                }
                if (hasAnyItem) {
                    int slot;
                    float baseItemY;
                    float enchHeight = (float)maxEnchants * 4.2f;
                    float itemBlockHeight = Math.max(16.0f, enchHeight);
                    itemY = baseItemY = (float)(-textHeight) - 4.0f - itemBlockHeight;
                    for (slot = 0; slot < 6; ++slot) {
                        itemStack = playerItems[slot];
                        if (itemStack == null || itemStack.isEmpty()) continue;
                        int slotX = -54 + slot * 18 + 1;
                        poseStack.pushPose();
                        poseStack.translate((float)(slotX + 8), baseItemY + 8.0f, -0.5f);
                        poseStack.scale(16.0f, -16.0f, 1.0E-4f);
                        Renderer3D.renderItem(poseStack, itemStack, (ItemOwner)player);
                        poseStack.popPose();
                    }
                    for (slot = 0; slot < 6; ++slot) {
                        itemStack = playerItems[slot];
                        if (itemStack == null || itemStack.isEmpty()) continue;
                        int slotX = -54 + slot * 18 + 1;
                        if (this.durability.getValue() && itemStack.isDamageableItem()) {
                            float durabilityRatio = (float)(itemStack.getMaxDamage() - itemStack.getDamageValue()) / (float)itemStack.getMaxDamage();
                            float red = 1.0f - durabilityRatio;
                            String durabilityText = Math.round((float)(itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0f / (float)itemStack.getMaxDamage()) + "%";
                            float durabilityWidth = (float)Night.FONT_MANAGER.getWidth(durabilityText) * 0.5f;
                            poseStack.pushPose();
                            poseStack.translate((float)(slotX + 8) - durabilityWidth / 2.0f, baseItemY - 5.5f, 1.0f);
                            poseStack.scale(0.5f, 0.5f, 1.0f);
                            this.drawText(poseStack, durabilityText, 0.0f, 0.0f, new Color(red, durabilityRatio, 0.0f));
                            poseStack.popPose();
                        }
                        if (itemStack.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)) {
                            poseStack.pushPose();
                            poseStack.translate((float)(slotX + 1), baseItemY + 1.0f, 1.0f);
                            poseStack.scale(0.5f, 0.5f, 1.0f);
                            this.drawText(poseStack, "God", 0.0f, 0.0f, new Color(255, 125, 255));
                            poseStack.popPose();
                        }
                        if (itemStack.getCount() != 1) {
                            String countText = String.valueOf(itemStack.getCount());
                            float countWidth = Night.FONT_MANAGER.getWidth(countText);
                            poseStack.pushPose();
                            poseStack.translate((float)(slotX + 16) - countWidth, baseItemY + 7.0f, 1.0f);
                            poseStack.scale(1.0f, 1.0f, 1.0f);
                            this.drawText(poseStack, countText, 0.0f, 0.0f, Color.WHITE);
                            poseStack.popPose();
                        }
                        if (!this.enchantments.getValue() || enchantLists[slot] == null || enchantLists[slot].isEmpty()) continue;
                        List<String> enchants = enchantLists[slot];
                        float lineStep = 4.2f;
                        for (int i = 0; i < enchants.size(); ++i) {
                            String enchantText = enchants.get(i);
                            float enchantY = baseItemY + 8.0f + (float)i * lineStep;
                            poseStack.pushPose();
                            poseStack.translate((float)(slotX + 1), enchantY, 1.0f);
                            poseStack.scale(0.5f, 0.5f, 1.0f);
                            this.drawText(poseStack, enchantText, 0.0f, 0.0f, Color.WHITE);
                            poseStack.popPose();
                        }
                    }
                    if (this.durability.getValue()) {
                        itemY = baseItemY - 6.0f;
                    }
                }
            }
            if (this.itemName.getValue() && !player.getMainHandItem().isEmpty()) {
                String itemNameStr = player.getMainHandItem().getHoverName().getString();
                float itemNameWidth = (float)Night.FONT_MANAGER.getWidth(itemNameStr) * 0.5f;
                float itemNameY = itemY - (float)textHeight * 0.5f - 2.0f;
                poseStack.pushPose();
                poseStack.translate(-itemNameWidth / 2.0f, itemNameY, 1.0f);
                poseStack.scale(0.5f, 0.5f, 1.0f);
                this.drawText(poseStack, itemNameStr, 0.0f, 0.0f, Color.WHITE);
                poseStack.popPose();
            }
            poseStack.popPose();
            Renderer3D.draw(Renderer3D.QUADS, Renderer3D.DEBUG_LINES, false);
            Renderer3D.QUADS.clear();
            Renderer3D.DEBUG_LINES.clear();
        }
    }

    private void drawText(PoseStack poseStack, String text, float x, float y, Color color) {
        if (this.glow.getValue()) {
            Night.FONT_MANAGER.drawText(poseStack, text, x, y, color, true);
        } else {
            Night.FONT_MANAGER.drawTextWithShadow(poseStack, text, x, y, color, false);
        }
    }

    private List<String> getCachedEnchantments(ItemStack itemStack) {
        if (itemStack != null && !itemStack.isEmpty()) {
            ItemEnchantments itemEnchantments = (ItemEnchantments)itemStack.get(DataComponents.ENCHANTMENTS);
            return itemEnchantments != null && !itemEnchantments.isEmpty() ? ENCHANT_CACHE.computeIfAbsent(itemEnchantments, enchMap -> {
                ArrayList<String> formatted = new ArrayList<String>(enchMap.size());
                for (Object2IntMap.Entry entry : enchMap.entrySet()) {
                    String shortName = NAME_CACHE.computeIfAbsent((Holder<Enchantment>)((Holder)entry.getKey()), NameTagsModule::parseShortName);
                    int level = entry.getIntValue();
                    formatted.add(level > 1 ? shortName + " " + level : shortName);
                }
                return formatted;
            }) : Collections.emptyList();
        }
        return Collections.emptyList();
    }

    private static String parseShortName(Holder<Enchantment> holder) {
        String regName = holder.getRegisteredName();
        int colonIdx = regName.indexOf(58);
        String name = colonIdx != -1 ? regName.substring(colonIdx + 1) : regName;
        String abbrev = name.startsWith("protection") ? "Pro" : (name.startsWith("blast_protection") ? "Bla" : (name.startsWith("fire_protection") ? "Fir" : (name.startsWith("projectile_protection") ? "Proj" : (name.startsWith("unbreaking") ? "Unb" : (name.startsWith("mending") ? "Men" : (name.startsWith("thorns") ? "Tho" : (name.startsWith("respiration") ? "Res" : (name.startsWith("aqua_affinity") ? "Aqu" : (name.startsWith("depth_strider") ? "Dep" : (name.startsWith("feather_falling") ? "Fea" : (name.startsWith("soul_speed") ? "Soul" : (name.startsWith("swift_sneak") ? "Sne" : (name.startsWith("sharpness") ? "Sha" : (name.startsWith("smite") ? "Smi" : (name.startsWith("bane_of_arthropods") ? "Boa" : (name.startsWith("knockback") ? "Kb" : (name.startsWith("fire_aspect") ? "FA" : (name.startsWith("looting") ? "Loot" : (name.startsWith("sweeping_edge") ? "Swp" : (name.startsWith("efficiency") ? "Eff" : (name.startsWith("silk_touch") ? "Silk" : (name.startsWith("fortune") ? "Fort" : (name.startsWith("power") ? "Pow" : (name.startsWith("punch") ? "Pun" : (name.startsWith("flame") ? "Fla" : (name.startsWith("infinity") ? "Inf" : (name.startsWith("loyalty") ? "Loy" : (name.startsWith("impaling") ? "Imp" : (name.startsWith("riptide") ? "Rip" : (name.startsWith("channeling") ? "Chan" : (name.startsWith("multishot") ? "Multi" : (name.startsWith("quick_charge") ? "Quick" : (name.startsWith("piercing") ? "Pier" : (name.startsWith("density") ? "Den" : (name.startsWith("breach") ? "Bre" : (name.startsWith("wind_burst") ? "Wind" : (name.startsWith("vanishing_curse") ? "Van" : (name.startsWith("binding_curse") ? "Bind" : (name.length() > 3 ? name.substring(0, 3) : name)))))))))))))))))))))))))))))))))))))));
        return abbrev.substring(0, 1).toUpperCase() + abbrev.substring(1);
    }

    private ItemStack getItem(Player player, int slotIndex) {
        return switch (slotIndex) {
            case 0 -> player.getMainHandItem();
            case 1 -> player.getItemBySlot(EquipmentSlot.HEAD);
            case 2 -> player.getItemBySlot(EquipmentSlot.CHEST);
            case 3 -> player.getItemBySlot(EquipmentSlot.LEGS);
            case 4 -> player.getItemBySlot(EquipmentSlot.FEET);
            case 5 -> player.getOffhandItem();
            default -> ItemStack.EMPTY;
        };
    }
}

