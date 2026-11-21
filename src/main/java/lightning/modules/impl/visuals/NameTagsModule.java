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
            Object object;
            int n;
            boolean bl = ((IFreecamModule)((Object)Night.MODULE_MANAGER.getModule("Freecam"))).isToggled();
            if (player == NameTagsModule.mc.player && (!this.self.getValue() || NameTagsModule.mc.options.getCameraType().isFirstPerson() && !bl) || this.antiBot.getValue() && EntityUtils.isBot(player)) continue;
            if (player instanceof RemotePlayer) {
                RemotePlayer remotePlayer = (RemotePlayer)player;
                if (Night.MODULE_MANAGER.getModule(PopChamsModule.class) != null && Night.MODULE_MANAGER.getModule(PopChamsModule.class).isGhost((Entity)remotePlayer) || Night.MODULE_MANAGER.getModule(LogoutSpotModule.class) != null && Night.MODULE_MANAGER.getModule(LogoutSpotModule.class).isGhost((Entity)remotePlayer)) continue;
            }
            if (!Renderer3D.isFrustumVisible(player.getBoundingBox())) continue;
            double d = Mth.lerp((double)post.getTickDelta(), (double)player.xo, (double)player.getX());
            double d2 = Mth.lerp((double)post.getTickDelta(), (double)player.yo, (double)player.getY()) + (double)(player.isShiftKeyDown() ? 1.9f : 2.1f);
            double d3 = Mth.lerp((double)post.getTickDelta(), (double)player.zo, (double)player.getZ());
            Vec3 vec3 = new Vec3(d - NameTagsModule.mc.gameRenderer.mainCamera().position().x, d2 - NameTagsModule.mc.gameRenderer.mainCamera().position().y, d3 - NameTagsModule.mc.gameRenderer.mainCamera().position().z);
            float f = (float)Math.sqrt(NameTagsModule.mc.gameRenderer.mainCamera().position().distanceToSqr(d, d2, d3));
            float f2 = 0.0018f + (float)this.scale.getValue().intValue() / 10000.0f * f;
            if ((double)f <= 8.0) {
                f2 = 0.0245f;
            }
            poseStack.pushPose();
            poseStack.translate(vec3.x, vec3.y, vec3.z);
            poseStack.mulPose((Quaternionfc)NameTagsModule.mc.gameRenderer.mainCamera().rotation());
            poseStack.scale(f2, -f2, f2);
            boolean bl2 = this.nightCheck.getValue() && Night.NIGHT_USER_MANAGER != null && Night.NIGHT_USER_MANAGER.isNightUser(player);
            Object object2 = player.getName().getString();
            if (this.gameMode.getValue()) {
                object2 = (String)object2 + " [" + EntityUtils.getGameModeName(EntityUtils.getGameMode(player)) + "]";
            }
            if (this.ping.getValue()) {
                object2 = (String)object2 + " " + EntityUtils.getLatency(player) + "ms";
            }
            if (this.entityId.getValue()) {
                object2 = (String)object2 + " " + player.getId();
            }
            if (this.health.getValue()) {
                n = Math.round(player.getHealth() + player.getAbsorptionAmount());
                object2 = (String)object2 + " " + String.valueOf(ColorUtils.getHealthColor(player.getHealth() + player.getAbsorptionAmount())) + n + String.valueOf(ChatFormatting.RESET);
            }
            n = Night.WORLD_MANAGER.getPoppedTotems().getOrDefault(player.getUUID(), 0);
            if (this.totemPops.getValue() && n > 0) {
                object2 = (String)object2 + " " + String.valueOf(ColorUtils.getTotemColor(n)) + "-" + n;
            }
            int n2 = Night.FONT_MANAGER.getWidth((String)object2);
            int n3 = Night.FONT_MANAGER.getHeight();
            int n4 = bl2 ? n3 - 1 : 0;
            int n5 = bl2 ? 3 : 0;
            int n6 = n2 + n4 + n5;
            if (this.border.getValue().equalsIgnoreCase("Fill") || this.border.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderQuad(poseStack, (float)(-n6) / 2.0f - 2.0f, -n3 - 1, (float)n6 / 2.0f + 2.0f, 1.0f, this.fillColor.getColor());
            }
            if (this.border.getValue().equalsIgnoreCase("Outline") || this.border.getValue().equalsIgnoreCase("Both")) {
                Renderer3D.renderOutline(poseStack, (float)(-n6) / 2.0f - 2.0f, -n3 - 1, (float)n6 / 2.0f + 2.0f, 1.0f, this.outlineColor.getColor());
            }
            float f3 = (float)(-n6) / 2.0f;
            if (bl2) {
                Renderer3D.renderTexture(poseStack, LOGO_ID, f3, (float)(-n3) + 0.5f, f3 + (float)n4, 0.5f, -1);
                f3 += (float)(n4 + n5);
            }
            Color color = Night.MODULE_MANAGER.getModule(FakePlayerModule.class).isToggled() && Night.MODULE_MANAGER.getModule(FakePlayerModule.class).getPlayer() == player ? new Color(225, 0, 70) : (player.isShiftKeyDown() ? new Color(255, 170, 0) : (Night.FRIEND_MANAGER.contains(player.getName().getString()) ? Night.FRIEND_MANAGER.getDefaultFriendColor() : Color.WHITE));
            this.drawText(poseStack, (String)object2, (int)f3, -n3, color);
            float f4 = -n3 - 2;
            if (this.items.getValue()) {
                ItemStack itemStack;
                int n7;
                ItemStack[] playerItems = new ItemStack[6];
                List<String>[] listArray = (List<String>[]) new List[6];
                int n8 = 0;
                boolean bl3 = false;
                for (n7 = 0; n7 < 6; ++n7) {
                    ItemStack itemStack2 = this.getItem(player, n7);
                    playerItems[n7] = itemStack2;
                    if (itemStack2.isEmpty()) continue;
                    bl3 = true;
                    if (!this.enchantments.getValue()) continue;
                    List<String> enchants = this.getCachedEnchantments(itemStack2);
                    listArray[n7] = enchants;
                    if (enchants.size() <= n8) continue;
                    n8 = enchants.size();
                }
                if (bl3) {
                    int n9;
                    float f5;
                    float f6 = (float)n8 * 4.2f;
                    float f7 = Math.max(16.0f, f6);
                    f4 = f5 = (float)(-n3) - 4.0f - f7;
                    for (n9 = 0; n9 < 6; ++n9) {
                        itemStack = playerItems[n9];
                        if (itemStack == null || itemStack.isEmpty()) continue;
                        n7 = -54 + n9 * 18 + 1;
                        poseStack.pushPose();
                        poseStack.translate((float)(n7 + 8), f5 + 8.0f, -0.5f);
                        poseStack.scale(16.0f, -16.0f, 1.0E-4f);
                        Renderer3D.renderItem(poseStack, itemStack, (ItemOwner)player);
                        poseStack.popPose();
                    }
                    for (n9 = 0; n9 < 6; ++n9) {
                        float f8;
                        itemStack = playerItems[n9];
                        if (itemStack == null || itemStack.isEmpty()) continue;
                        n7 = -54 + n9 * 18 + 1;
                        if (this.durability.getValue() && itemStack.isDamageableItem()) {
                            float f9 = (float)(itemStack.getMaxDamage() - itemStack.getDamageValue()) / (float)itemStack.getMaxDamage();
                            f8 = 1.0f - f9;
                            String string = Math.round((float)(itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0f / (float)itemStack.getMaxDamage()) + "%";
                            float f10 = (float)Night.FONT_MANAGER.getWidth(string) * 0.5f;
                            poseStack.pushPose();
                            poseStack.translate((float)(n7 + 8) - f10 / 2.0f, f5 - 5.5f, 1.0f);
                            poseStack.scale(0.5f, 0.5f, 1.0f);
                            this.drawText(poseStack, string, 0.0f, 0.0f, new Color(f8, f9, 0.0f));
                            poseStack.popPose();
                        }
                        if (itemStack.getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)) {
                            poseStack.pushPose();
                            poseStack.translate((float)(n7 + 1), f5 + 1.0f, 1.0f);
                            poseStack.scale(0.5f, 0.5f, 1.0f);
                            this.drawText(poseStack, "God", 0.0f, 0.0f, new Color(255, 125, 255));
                            poseStack.popPose();
                        }
                        if (itemStack.getCount() != 1) {
                            String string = String.valueOf(itemStack.getCount());
                            f8 = Night.FONT_MANAGER.getWidth(string);
                            poseStack.pushPose();
                            poseStack.translate((float)(n7 + 16) - f8, f5 + 7.0f, 1.0f);
                            poseStack.scale(1.0f, 1.0f, 1.0f);
                            this.drawText(poseStack, string, 0.0f, 0.0f, Color.WHITE);
                            poseStack.popPose();
                        }
                        if (!this.enchantments.getValue() || listArray[n9] == null || listArray[n9].isEmpty()) continue;
                        List list = listArray[n9];
                        f8 = 4.2f;
                        for (int i = 0; i < list.size(); ++i) {
                            String string = (String)list.get(i);
                            float f11 = f5 + 8.0f + (float)i * f8;
                            poseStack.pushPose();
                            poseStack.translate((float)(n7 + 1), f11, 1.0f);
                            poseStack.scale(0.5f, 0.5f, 1.0f);
                            this.drawText(poseStack, string, 0.0f, 0.0f, Color.WHITE);
                            poseStack.popPose();
                        }
                    }
                    if (this.durability.getValue()) {
                        f4 = f5 - 6.0f;
                    }
                }
            }
            if (this.itemName.getValue() && !player.getMainHandItem().isEmpty()) {
                String itemNameStr = player.getMainHandItem().getHoverName().getString();
                float f12 = (float)Night.FONT_MANAGER.getWidth(itemNameStr) * 0.5f;
                float f13 = f4 - (float)n3 * 0.5f - 2.0f;
                poseStack.pushPose();
                poseStack.translate(-f12 / 2.0f, f13, 1.0f);
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

    private void drawText(PoseStack poseStack, String string, float f, float f2, Color color) {
        if (this.glow.getValue()) {
            Night.FONT_MANAGER.drawText(poseStack, string, f, f2, color, true);
        } else {
            Night.FONT_MANAGER.drawTextWithShadow(poseStack, string, f, f2, color, false);
        }
    }

    private List<String> getCachedEnchantments(ItemStack itemStack) {
        if (itemStack != null && !itemStack.isEmpty()) {
            ItemEnchantments itemEnchantments2 = (ItemEnchantments)itemStack.get(DataComponents.ENCHANTMENTS);
            return itemEnchantments2 != null && !itemEnchantments2.isEmpty() ? ENCHANT_CACHE.computeIfAbsent(itemEnchantments2, itemEnchantments -> {
                ArrayList<String> arrayList = new ArrayList<String>(itemEnchantments.size());
                for (Object2IntMap.Entry entry : itemEnchantments.entrySet()) {
                    String string = NAME_CACHE.computeIfAbsent((Holder<Enchantment>)((Holder)entry.getKey()), NameTagsModule::parseShortName);
                    int n = entry.getIntValue();
                    arrayList.add(n > 1 ? string + " " + n : string);
                }
                return arrayList;
            }) : Collections.emptyList();
        }
        return Collections.emptyList();
    }

    private static String parseShortName(Holder<Enchantment> holder) {
        String string = holder.getRegisteredName();
        int n = string.indexOf(58);
        String string2 = n != -1 ? string.substring(n + 1) : string;
        String string3 = string2;
        String string4 = string2.startsWith("protection") ? "Pro" : (string2.startsWith("blast_protection") ? "Bla" : (string2.startsWith("fire_protection") ? "Fir" : (string2.startsWith("projectile_protection") ? "Proj" : (string2.startsWith("unbreaking") ? "Unb" : (string2.startsWith("mending") ? "Men" : (string2.startsWith("thorns") ? "Tho" : (string2.startsWith("respiration") ? "Res" : (string2.startsWith("aqua_affinity") ? "Aqu" : (string2.startsWith("depth_strider") ? "Dep" : (string2.startsWith("feather_falling") ? "Fea" : (string2.startsWith("soul_speed") ? "Soul" : (string2.startsWith("swift_sneak") ? "Sne" : (string2.startsWith("sharpness") ? "Sha" : (string2.startsWith("smite") ? "Smi" : (string2.startsWith("bane_of_arthropods") ? "Boa" : (string2.startsWith("knockback") ? "Kb" : (string2.startsWith("fire_aspect") ? "FA" : (string2.startsWith("looting") ? "Loot" : (string2.startsWith("sweeping_edge") ? "Swp" : (string2.startsWith("efficiency") ? "Eff" : (string2.startsWith("silk_touch") ? "Silk" : (string2.startsWith("fortune") ? "Fort" : (string2.startsWith("power") ? "Pow" : (string2.startsWith("punch") ? "Pun" : (string2.startsWith("flame") ? "Fla" : (string2.startsWith("infinity") ? "Inf" : (string2.startsWith("loyalty") ? "Loy" : (string2.startsWith("impaling") ? "Imp" : (string2.startsWith("riptide") ? "Rip" : (string2.startsWith("channeling") ? "Chan" : (string2.startsWith("multishot") ? "Multi" : (string2.startsWith("quick_charge") ? "Quick" : (string2.startsWith("piercing") ? "Pier" : (string2.startsWith("density") ? "Den" : (string2.startsWith("breach") ? "Bre" : (string2.startsWith("wind_burst") ? "Wind" : (string2.startsWith("vanishing_curse") ? "Van" : (string2.startsWith("binding_curse") ? "Bind" : (string2.length() > 3 ? string2.substring(0, 3) : string2)))))))))))))))))))))))))))))))))))))));
        return string4.substring(0, 1).toUpperCase() + string4.substring(1);
    }

    private ItemStack getItem(Player player, int n) {
        return switch (n) {
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

