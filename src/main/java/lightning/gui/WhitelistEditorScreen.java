/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.alchemy.Potion
 *  net.minecraft.world.item.alchemy.PotionContents
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 */
package lightning.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lightning.Night;
import lightning.gui.ClickGuiScreen;
import lightning.settings.impl.WhitelistSetting;
import lightning.utils.animations.Animation;
import lightning.utils.animations.Easing;
import lightning.utils.graphics.Renderer2D;
import lightning.utils.minecraft.IdentifierUtils;
import lightning.utils.system.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class WhitelistEditorScreen
extends Screen {
    private static final int COL_WIDTH = 150;
    private static final int COL_GAP = 10;
    private static final int HEADER_H = 13;
    private static final int ROW_H = 18;
    private static final int ICON_SIZE = 16;
    private static final int SLIDE_DURATION_MS = 320;
    private static final float SLIDE_DISTANCE = 160.0f;
    private static final int SEARCH_STAGGER_MS = 40;
    private static final int SEARCH_ROW_ANIM_MS = 220;
    private static final float SEARCH_SLIDE_DISTANCE = 40.0f;
    private final WhitelistSetting setting;
    private final Screen parent;
    private final List<Entry> allEntries;
    private String query = "";
    private String lastQuery = "";
    private long queryChangeTime = System.currentTimeMillis();
    private float scrollAll = 0.0f;
    private float targetScrollAll = 0.0f;
    private float scrollWhite = 0.0f;
    private float targetScrollWhite = 0.0f;
    private final Animation slideAnim = new Animation(320, Easing.Method.EASE_OUT_CUBIC);
    private boolean closing = false;
    private final Timer lineTimer = new Timer();
    private boolean showLine = false;
    private int colX1;
    private int colX2;
    private int colY;
    private int colHeight;
    private static final String GROUP_PREFIX = "group:";
    private static final String[] DYE_COLORS = new String[]{"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    private static final int MIN_FAMILY_SIZE = 8;

    public WhitelistEditorScreen(WhitelistSetting whitelistSetting, Screen screen) {
        super((Component)Component.literal((String)"night-whitelist-editor"));
        this.setting = whitelistSetting;
        this.parent = screen;
        this.allEntries = WhitelistEditorScreen.buildEntries(whitelistSetting);
    }

    private static String familyOf(String string) {
        for (String string2 : DYE_COLORS) {
            if (string.equals(string2)) {
                return "";
            }
            if (!string.startsWith(string2 + "_")) continue;
            return string.substring(string2.length() + 1);
        }
        return null;
    }

    private static List<Entry> buildEntries(WhitelistSetting whitelistSetting) {
        ArrayList<Entry> arrayList = new ArrayList<Entry>();
        Map<String, List<Entry>> linkedHashMap = new LinkedHashMap<>();
        if (whitelistSetting.getType() == WhitelistSetting.Type.BLOCKS) {
            BuiltInRegistries.BLOCK.entrySet().forEach(entry -> {
                Block block = (Block)entry.getValue();
                String string2 = ((ResourceKey)entry.getKey()).identifier().toString();
                Item item = block.asItem();
                ItemStack itemStack = item == Items.AIR ? ItemStack.EMPTY : new ItemStack((ItemLike)item);
                Entry entry2 = new Entry(string2, string2.replace("minecraft:", ""), itemStack, block);
                arrayList.add(entry2);
                String string3 = WhitelistEditorScreen.familyOf(string2.substring("minecraft:".length()));
                if (string3 != null && !string3.isEmpty()) {
                    linkedHashMap.computeIfAbsent(string3, string -> new ArrayList<>()).add(entry2);
                }
            });
        } else if (whitelistSetting.getType() == WhitelistSetting.Type.POTIONS) {
            BuiltInRegistries.POTION.entrySet().forEach(entry -> {
                Potion potion = (Potion)entry.getValue();
                String string = ((ResourceKey)entry.getKey()).identifier().toString();
                Holder<Potion> holder = BuiltInRegistries.POTION.wrapAsHolder(potion);
                ItemStack itemStack = new ItemStack((ItemLike)Items.SPLASH_POTION);
                itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
                String string2 = "Splash Potion of " + potion.name().replace("_", " ");
                arrayList.add(new Entry(string, string2, itemStack, potion));
            });
        } else {
            BuiltInRegistries.ITEM.entrySet().forEach(entry -> {
                Item item = (Item)entry.getValue();
                String string2 = ((ResourceKey)entry.getKey()).identifier().toString();
                Entry entry2 = new Entry(string2, string2.replace("minecraft:", ""), new ItemStack((ItemLike)item), item);
                arrayList.add(entry2);
                String string3 = WhitelistEditorScreen.familyOf(string2.substring("minecraft:".length()));
                if (string3 != null && !string3.isEmpty()) {
                    linkedHashMap.computeIfAbsent(string3, string -> new ArrayList<>()).add(entry2);
                }
            });
        }
        for (Map.Entry<String, List<Entry>> entry3 : linkedHashMap.entrySet()) {
            if (entry3.getValue().size() < 8) continue;
            Entry entry4 = entry3.getValue().get(0);
            String string = entry3.getKey().replace("_", " ");
            arrayList.add(new Entry(GROUP_PREFIX + entry3.getKey(), string + " (all colors)", entry4.icon, null));
        }
        arrayList.sort((entry, entry2) -> entry.name.compareToIgnoreCase(entry2.name));
        return arrayList;
    }

    public void requestClose() {
        this.closing = true;
        this.slideAnim.setEasing(Easing.Method.EASE_IN_CUBIC);
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, this.width, this.height, new Color(10, 10, 10, 150));
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        super.extractRenderState(guiGraphicsExtractor, n, n2, f);
        if (this.lineTimer.hasTimeElapsed(400L)) {
            this.showLine = !this.showLine;
            this.lineTimer.reset();
        }
        float f2 = this.slideAnim.get(this.closing ? 0.0f : 1.0f);
        if (this.closing && f2 <= 0.001f) {
            Minecraft.getInstance().gui.setScreen(this.parent);
            return;
        }
        int n3 = 310;
        this.colX1 = (this.width - n3) / 2;
        this.colX2 = this.colX1 + 150 + 10;
        this.colY = 20;
        this.colHeight = this.height - 40;
        guiGraphicsExtractor.pose().pushMatrix();
        guiGraphicsExtractor.pose().translate(0.0f, (1.0f - f2) * -160.0f);
        List<String> list = this.setting.getWhitelistIds();
        List<Entry> list2 = this.allEntries.stream().filter(entry -> list.contains(entry.id)).toList();
        List<Entry> list3 = this.allEntries.stream().filter(entry -> !list.contains(entry.id)).toList();
        List<Entry> list4 = this.query.isEmpty() ? list3 : this.filter(list3, this.query);
        this.renderSearchColumn(guiGraphicsExtractor, n, n2, this.colX1, list4, list);
        this.renderColumn(guiGraphicsExtractor, n, n2, this.colX2, "Whitelist (" + list2.size() + ")", list2, list, this.scrollWhite, 2);
        guiGraphicsExtractor.pose().popMatrix();
    }

    private List<Entry> filter(List<Entry> list, String query) {
        String[] terms = query.toLowerCase().split(" ");
        return list.stream().filter(entry -> {
            String entryName = entry.name.toLowerCase();
            for (String term : terms) {
                if (term.isEmpty() || entryName.contains(term)) continue;
                return false;
            }
            return true;
        }).toList();
    }

    private void renderColumn(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, int n3, String string, List<Entry> list, List<String> list2, float f, int n4) {
        Renderer2D.renderQuad(guiGraphicsExtractor, n3, this.colY, n3 + 150, this.colY + 13, new Color(21, 21, 21, 200));
        Renderer2D.renderQuad(guiGraphicsExtractor, n3, this.colY + 13 - 1, n3 + 150, this.colY + 13, ClickGuiScreen.getButtonColor(this.colY, 200));
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, n3 + 3, this.colY + 2, Color.WHITE);
        int n5 = this.colY + 13;
        int n6 = this.colHeight - 13;
        Renderer2D.renderQuad(guiGraphicsExtractor, n3, n5, n3 + 150, n5 + n6, new Color(16, 16, 16, 180));
        Renderer2D.renderOutline(guiGraphicsExtractor, n3, this.colY, n3 + 150, n5 + n6, ClickGuiScreen.getButtonColor(this.colY, 120));
        guiGraphicsExtractor.enableScissor(n3, n5, n3 + 150, n5 + n6);
        int n7 = n6 / 18 + 2;
        int n8 = (int)f;
        for (int i = 0; i < n7; ++i) {
            int n9 = n8 + i;
            if (n9 < 0 || n9 >= list.size()) continue;
            Entry entry = list.get(n9);
            int n10 = n5 + Math.round((float)(i * 18) - (f - (float)n8) * 18.0f);
            if (n10 + 18 < n5 || n10 > n5 + n6) continue;
            this.renderRow(guiGraphicsExtractor, n, n2, n3, n10, entry, list2.contains(entry.id), n4);
        }
        guiGraphicsExtractor.disableScissor();
    }

    private void renderSearchColumn(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, int n3, List<Entry> list, List<String> list2) {
        int n4;
        Renderer2D.renderQuad(guiGraphicsExtractor, n3, this.colY, n3 + 150, this.colY + 13, new Color(21, 21, 21, 200));
        Renderer2D.renderQuad(guiGraphicsExtractor, n3, this.colY + 13 - 1, n3 + 150, this.colY + 13, ClickGuiScreen.getButtonColor(this.colY, 200));
        if (this.query.isEmpty()) {
            String placeholder = "Items (search...)";
            n4 = Night.FONT_MANAGER.getWidth(" ");
            if (this.showLine) {
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "|", n3 + 3, this.colY + 2, Color.WHITE);
            }
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, placeholder, n3 + 3 + n4, this.colY + 2, Color.GRAY);
        } else {
            String queryText = this.query + (this.showLine ? "|" : " ");
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, queryText, n3 + 3, this.colY + 2, Color.WHITE);
        }
        int n5 = this.colY + 13;
        n4 = this.colHeight - 13;
        Renderer2D.renderQuad(guiGraphicsExtractor, n3, n5, n3 + 150, n5 + n4, new Color(16, 16, 16, 180));
        Renderer2D.renderOutline(guiGraphicsExtractor, n3, this.colY, n3 + 150, n5 + n4, ClickGuiScreen.getButtonColor(this.colY, 120));
        guiGraphicsExtractor.enableScissor(n3, n5, n3 + 150, n5 + n4);
        long l = System.currentTimeMillis();
        int n6 = n4 / 18 + 2;
        int n7 = (int)this.scrollAll;
        for (int i = 0; i < n6; ++i) {
            int n8 = n7 + i;
            if (n8 < 0 || n8 >= list.size()) continue;
            Entry entry = list.get(n8);
            int n9 = n5 + Math.round((float)(i * 18) - (this.scrollAll - (float)n7) * 18.0f);
            if (n9 + 18 < n5 || n9 > n5 + n4) continue;
            float f = l - this.queryChangeTime - (long)(n8 * 40);
            float f2 = Easing.ease(Mth.clamp((float)(f / 220.0f), (float)0.0f, (float)1.0f), Easing.Method.EASE_OUT_CUBIC);
            int n10 = Math.round((1.0f - f2) * 40.0f);
            guiGraphicsExtractor.pose().pushMatrix();
            guiGraphicsExtractor.pose().translate((float)(-n10), 0.0f);
            this.renderRow(guiGraphicsExtractor, n, n2, n3, n9, entry, list2.contains(entry.id), 1);
            guiGraphicsExtractor.pose().popMatrix();
        }
        guiGraphicsExtractor.disableScissor();
    }

    private void renderRow(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, int n3, int n4, Entry entry, boolean bl, int n5) {
        boolean bl2 = n >= n3 && n < n3 + 150 && n2 >= n4 && n2 < n4 + 18 - 1;
        Color color = bl2 ? new Color(255, 255, 255, 15) : (bl ? new Color(255, 255, 255, 8) : new Color(0, 0, 0, 0));
        if (color.getAlpha() > 0) {
            Renderer2D.renderQuad(guiGraphicsExtractor, n3, n4, n3 + 150, n4 + 18 - 1, color);
        }
        if (!entry.icon.isEmpty()) {
            guiGraphicsExtractor.item(entry.icon, n3 + 2, n4 + 0);
        }
        Color color3 = bl ? Color.YELLOW : Color.WHITE;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, entry.name, n3 + 16 + 6, n4 + 8 - Night.FONT_MANAGER.getHeight() / 2, color3);
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        double d = mouseButtonEvent.x();
        double d2 = mouseButtonEvent.y();
        if (mouseButtonEvent.button() != 0) {
            return super.mouseClicked(mouseButtonEvent, bl);
        }
        List<String> list = this.setting.getWhitelistIds();
        int n = this.colY + 13;
        int n2 = this.colHeight - 13;
        if (d2 < (double)n || d2 >= (double)(n + n2)) {
            return super.mouseClicked(mouseButtonEvent, bl);
        }
        if (d >= (double)this.colX1 && d < (double)(this.colX1 + 150)) {
            List<Entry> list2 = this.allEntries.stream().filter(entry -> !list.contains(entry.id)).toList();
            List<Entry> list3 = this.query.isEmpty() ? list2 : this.filter(list2, this.query);
            Entry entry2 = this.pickRow(d, d2, n, this.scrollAll, list3);
            if (entry2 != null) {
                this.addToWhitelist(entry2);
            }
            return true;
        }
        if (d >= (double)this.colX2 && d < (double)(this.colX2 + 150)) {
            List<Entry> list4 = this.allEntries.stream().filter(entry -> list.contains(entry.id)).toList();
            Entry entry3 = this.pickRow(d, d2, n, this.scrollWhite, list4);
            if (entry3 != null) {
                this.removeFromWhitelist(entry3);
            }
            return true;
        }
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    private Entry pickRow(double d, double d2, int n, float f, List<Entry> list) {
        int n2 = (int)(d2 - (double)n);
        int n3 = (int)f + n2 / 18;
        if (n3 < 0 || n3 >= list.size()) {
            return null;
        }
        return list.get(n3);
    }

    private void addToWhitelist(Entry entry2) {
        if (entry2.id.startsWith(GROUP_PREFIX)) {
            String string = entry2.id.substring(GROUP_PREFIX.length());
            if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
                BuiltInRegistries.BLOCK.entrySet().forEach(entry -> {
                    String string2 = ((ResourceKey)entry.getKey()).identifier().toString().substring("minecraft:".length());
                    if (string.equals(WhitelistEditorScreen.familyOf(string2)) || string2.equals(string)) {
                        this.setting.add(entry.getValue());
                    }
                });
            } else {
                BuiltInRegistries.ITEM.entrySet().forEach(entry -> {
                    String string2 = ((ResourceKey)entry.getKey()).identifier().toString().substring("minecraft:".length());
                    if (string.equals(WhitelistEditorScreen.familyOf(string2)) || string2.equals(string)) {
                        this.setting.add(entry.getValue());
                    }
                });
            }
            return;
        }
        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
            this.setting.add(IdentifierUtils.getBlock(entry2.id));
        } else if (this.setting.getType() == WhitelistSetting.Type.POTIONS) {
            this.setting.add(IdentifierUtils.getPotion(entry2.id));
        } else {
            this.setting.add(IdentifierUtils.getItem(entry2.id));
        }
    }

    private void removeFromWhitelist(Entry entry) {
        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
            this.setting.remove(IdentifierUtils.getBlock(entry.id));
        } else if (this.setting.getType() == WhitelistSetting.Type.POTIONS) {
            this.setting.remove(IdentifierUtils.getPotion(entry.id));
        } else {
            this.setting.remove(IdentifierUtils.getItem(entry.id));
        }
    }

    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (d >= (double)this.colX1 && d < (double)(this.colX1 + 150)) {
            List<String> list = this.setting.getWhitelistIds();
            List<Entry> list2 = this.allEntries.stream().filter(entry -> !list.contains(entry.id)).toList();
            List<Entry> list3 = this.query.isEmpty() ? list2 : this.filter(list2, this.query);
            this.targetScrollAll = this.clampScroll(this.targetScrollAll - (float)d4, list3.size());
        } else if (d >= (double)this.colX2 && d < (double)(this.colX2 + 150)) {
            this.targetScrollWhite = this.clampScroll(this.targetScrollWhite - (float)d4, this.setting.getWhitelistIds().size());
        }
        this.scrollAll += (this.targetScrollAll - this.scrollAll) * 0.5f;
        this.scrollWhite += (this.targetScrollWhite - this.scrollWhite) * 0.5f;
        return true;
    }

    private float clampScroll(float f, int n) {
        int n2 = this.colHeight - 13;
        float f2 = Math.max(0.0f, (float)n - (float)n2 / 18.0f);
        return Math.max(0.0f, Math.min(f, f2));
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        int n = keyEvent.key();
        if (n == 256) {
            this.requestClose();
            return true;
        }
        if (n == 259 && !this.query.isEmpty()) {
            this.setQuery(this.query.substring(0, this.query.length() - 1));
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    public boolean charTyped(CharacterEvent characterEvent) {
        char c = (char)characterEvent.codepoint();
        if (!Character.isISOControl(c)) {
            this.setQuery(this.query + c);
            return true;
        }
        return super.charTyped(characterEvent);
    }

    private void setQuery(String string) {
        if (!string.equals(this.lastQuery)) {
            this.queryChangeTime = System.currentTimeMillis();
            this.lastQuery = string;
            this.scrollAll = 0.0f;
            this.targetScrollAll = 0.0f;
        }
        this.query = string;
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static class Entry {
        final String id;
        final String name;
        final ItemStack icon;

        Entry(String string, String string2, ItemStack itemStack, Object object) {
            this.id = string;
            this.name = string2;
            this.icon = itemStack;
        }
    }
}

