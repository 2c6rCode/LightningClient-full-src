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

    private static String familyOf(String blockName) {
        for (String dye : DYE_COLORS) {
            if (blockName.equals(dye)) {
                return "";
            }
            if (!blockName.startsWith(dye + "_")) continue;
            return blockName.substring(dye.length() + 1);
        }
        return null;
    }

    private static List<Entry> buildEntries(WhitelistSetting whitelistSetting) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        Map<String, List<Entry>> families = new LinkedHashMap<>();
        if (whitelistSetting.getType() == WhitelistSetting.Type.BLOCKS) {
            BuiltInRegistries.BLOCK.entrySet().forEach(entry -> {
                Block block = (Block)entry.getValue();
                String id = ((ResourceKey)entry.getKey()).identifier().toString();
                Item item = block.asItem();
                ItemStack itemStack = item == Items.AIR ? ItemStack.EMPTY : new ItemStack((ItemLike)item);
                Entry entryObj = new Entry(id, id.replace("minecraft:", ""), itemStack, block);
                entries.add(entryObj);
                String family = WhitelistEditorScreen.familyOf(id.substring("minecraft:".length()));
                if (family != null && !family.isEmpty()) {
                    families.computeIfAbsent(family, key -> new ArrayList<>()).add(entryObj);
                }
            });
        } else if (whitelistSetting.getType() == WhitelistSetting.Type.POTIONS) {
            BuiltInRegistries.POTION.entrySet().forEach(entry -> {
                Potion potion = (Potion)entry.getValue();
                String id = ((ResourceKey)entry.getKey()).identifier().toString();
                Holder<Potion> holder = BuiltInRegistries.POTION.wrapAsHolder(potion);
                ItemStack itemStack = new ItemStack((ItemLike)Items.SPLASH_POTION);
                itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
                String label = "Splash Potion of " + potion.name().replace("_", " ");
                entries.add(new Entry(id, label, itemStack, potion));
            });
        } else {
            BuiltInRegistries.ITEM.entrySet().forEach(entry -> {
                Item item = (Item)entry.getValue();
                String id = ((ResourceKey)entry.getKey()).identifier().toString();
                Entry entryObj = new Entry(id, id.replace("minecraft:", ""), new ItemStack((ItemLike)item), item);
                entries.add(entryObj);
                String family = WhitelistEditorScreen.familyOf(id.substring("minecraft:".length()));
                if (family != null && !family.isEmpty()) {
                    families.computeIfAbsent(family, key -> new ArrayList<>()).add(entryObj);
                }
            });
        }
        for (Map.Entry<String, List<Entry>> familyEntry : families.entrySet()) {
            if (familyEntry.getValue().size() < 8) continue;
            Entry sample = familyEntry.getValue().get(0);
            String label = familyEntry.getKey().replace("_", " ");
            entries.add(new Entry(GROUP_PREFIX + familyEntry.getKey(), label + " (all colors)", sample.icon, null));
        }
        entries.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        return entries;
    }

    public void requestClose() {
        this.closing = true;
        this.slideAnim.setEasing(Easing.Method.EASE_IN_CUBIC);
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        Renderer2D.renderQuad(guiGraphicsExtractor, 0.0f, 0.0f, this.width, this.height, new Color(10, 10, 10, 150));
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
        if (this.lineTimer.hasTimeElapsed(400L)) {
            this.showLine = !this.showLine;
            this.lineTimer.reset();
        }
        float slideProgress = this.slideAnim.get(this.closing ? 0.0f : 1.0f);
        if (this.closing && slideProgress <= 0.001f) {
            Minecraft.getInstance().gui.setScreen(this.parent);
            return;
        }
        int totalWidth = 310;
        this.colX1 = (this.width - totalWidth) / 2;
        this.colX2 = this.colX1 + 150 + 10;
        this.colY = 20;
        this.colHeight = this.height - 40;
        guiGraphicsExtractor.pose().pushMatrix();
        guiGraphicsExtractor.pose().translate(0.0f, (1.0f - slideProgress) * -160.0f);
        List<String> whitelistedIds = this.setting.getWhitelistIds();
        List<Entry> whitelistedEntries = this.allEntries.stream().filter(entry -> whitelistedIds.contains(entry.id)).toList();
        List<Entry> availableEntries = this.allEntries.stream().filter(entry -> !whitelistedIds.contains(entry.id)).toList();
        List<Entry> filteredAvailable = this.query.isEmpty() ? availableEntries : this.filter(availableEntries, this.query);
        this.renderSearchColumn(guiGraphicsExtractor, mouseX, mouseY, this.colX1, filteredAvailable, whitelistedIds);
        this.renderColumn(guiGraphicsExtractor, mouseX, mouseY, this.colX2, "Whitelist (" + whitelistedEntries.size() + ")", whitelistedEntries, whitelistedIds, this.scrollWhite, 2);
        guiGraphicsExtractor.pose().popMatrix();
    }

    private List<Entry> filter(List<Entry> entries, String query) {
        String[] terms = query.toLowerCase().split(" ");
        return entries.stream().filter(entry -> {
            String entryName = entry.name.toLowerCase();
            for (String term : terms) {
                if (term.isEmpty() || entryName.contains(term)) continue;
                return false;
            }
            return true;
        }).toList();
    }

    private void renderColumn(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, int colX, String title, List<Entry> entries, List<String> whitelistedIds, float scroll, int columnId) {
        Renderer2D.renderQuad(guiGraphicsExtractor, colX, this.colY, colX + 150, this.colY + 13, new Color(21, 21, 21, 200));
        Renderer2D.renderQuad(guiGraphicsExtractor, colX, this.colY + 13 - 1, colX + 150, this.colY + 13, ClickGuiScreen.getButtonColor(this.colY, 200));
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, title, colX + 3, this.colY + 2, Color.WHITE);
        int bodyY = this.colY + 13;
        int bodyHeight = this.colHeight - 13;
        Renderer2D.renderQuad(guiGraphicsExtractor, colX, bodyY, colX + 150, bodyY + bodyHeight, new Color(16, 16, 16, 180));
        Renderer2D.renderOutline(guiGraphicsExtractor, colX, this.colY, colX + 150, bodyY + bodyHeight, ClickGuiScreen.getButtonColor(this.colY, 120));
        guiGraphicsExtractor.enableScissor(colX, bodyY, colX + 150, bodyY + bodyHeight);
        int visibleRows = bodyHeight / 18 + 2;
        int startIdx = (int)scroll;
        for (int i = 0; i < visibleRows; ++i) {
            int idx = startIdx + i;
            if (idx < 0 || idx >= entries.size()) continue;
            Entry entry = entries.get(idx);
            int rowY = bodyY + Math.round((float)(i * 18) - (scroll - (float)startIdx) * 18.0f);
            if (rowY + 18 < bodyY || rowY > bodyY + bodyHeight) continue;
            this.renderRow(guiGraphicsExtractor, mouseX, mouseY, colX, rowY, entry, whitelistedIds.contains(entry.id), columnId);
        }
        guiGraphicsExtractor.disableScissor();
    }

    private void renderSearchColumn(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, int colX, List<Entry> entries, List<String> whitelistedIds) {
        Renderer2D.renderQuad(guiGraphicsExtractor, colX, this.colY, colX + 150, this.colY + 13, new Color(21, 21, 21, 200));
        Renderer2D.renderQuad(guiGraphicsExtractor, colX, this.colY + 13 - 1, colX + 150, this.colY + 13, ClickGuiScreen.getButtonColor(this.colY, 200));
        if (this.query.isEmpty()) {
            String placeholder = "Items (search...)";
            int spaceWidth = Night.FONT_MANAGER.getWidth(" ");
            if (this.showLine) {
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, "|", colX + 3, this.colY + 2, Color.WHITE);
            }
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, placeholder, colX + 3 + spaceWidth, this.colY + 2, Color.GRAY);
        } else {
            String queryText = this.query + (this.showLine ? "|" : " ");
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, queryText, colX + 3, this.colY + 2, Color.WHITE);
        }
        int bodyY = this.colY + 13;
        int bodyHeight = this.colHeight - 13;
        Renderer2D.renderQuad(guiGraphicsExtractor, colX, bodyY, colX + 150, bodyY + bodyHeight, new Color(16, 16, 16, 180));
        Renderer2D.renderOutline(guiGraphicsExtractor, colX, this.colY, colX + 150, bodyY + bodyHeight, ClickGuiScreen.getButtonColor(this.colY, 120));
        guiGraphicsExtractor.enableScissor(colX, bodyY, colX + 150, bodyY + bodyHeight);
        long now = System.currentTimeMillis();
        int visibleRows = bodyHeight / 18 + 2;
        int startIdx = (int)this.scrollAll;
        for (int i = 0; i < visibleRows; ++i) {
            int idx = startIdx + i;
            if (idx < 0 || idx >= entries.size()) continue;
            Entry entry = entries.get(idx);
            int rowY = bodyY + Math.round((float)(i * 18) - (this.scrollAll - (float)startIdx) * 18.0f);
            if (rowY + 18 < bodyY || rowY > bodyY + bodyHeight) continue;
            float elapsed = now - this.queryChangeTime - (long)(idx * 40);
            float progress = Easing.ease(Mth.clamp((float)(elapsed / 220.0f), (float)0.0f, (float)1.0f), Easing.Method.EASE_OUT_CUBIC);
            int slideOffset = Math.round((1.0f - progress) * 40.0f);
            guiGraphicsExtractor.pose().pushMatrix();
            guiGraphicsExtractor.pose().translate((float)(-slideOffset), 0.0f);
            this.renderRow(guiGraphicsExtractor, mouseX, mouseY, colX, rowY, entry, whitelistedIds.contains(entry.id), 1);
            guiGraphicsExtractor.pose().popMatrix();
        }
        guiGraphicsExtractor.disableScissor();
    }

    private void renderRow(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, int colX, int rowY, Entry entry, boolean isWhitelisted, int columnId) {
        boolean isHovered = mouseX >= colX && mouseX < colX + 150 && mouseY >= rowY && mouseY < rowY + 18 - 1;
        Color bgColor = isHovered ? new Color(255, 255, 255, 15) : (isWhitelisted ? new Color(255, 255, 255, 8) : new Color(0, 0, 0, 0));
        if (bgColor.getAlpha() > 0) {
            Renderer2D.renderQuad(guiGraphicsExtractor, colX, rowY, colX + 150, rowY + 18 - 1, bgColor);
        }
        if (!entry.icon.isEmpty()) {
            guiGraphicsExtractor.item(entry.icon, colX + 2, rowY + 0);
        }
        Color textColor = isWhitelisted ? Color.YELLOW : Color.WHITE;
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, entry.name, colX + 16 + 6, rowY + 8 - Night.FONT_MANAGER.getHeight() / 2, textColor);
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        if (mouseButtonEvent.button() != 0) {
            return super.mouseClicked(mouseButtonEvent, doubleClick);
        }
        List<String> whitelistedIds = this.setting.getWhitelistIds();
        int bodyY = this.colY + 13;
        int bodyHeight = this.colHeight - 13;
        if (mouseY < (double)bodyY || mouseY >= (double)(bodyY + bodyHeight)) {
            return super.mouseClicked(mouseButtonEvent, doubleClick);
        }
        if (mouseX >= (double)this.colX1 && mouseX < (double)(this.colX1 + 150)) {
            List<Entry> availableEntries = this.allEntries.stream().filter(entry -> !whitelistedIds.contains(entry.id)).toList();
            List<Entry> filteredAvailable = this.query.isEmpty() ? availableEntries : this.filter(availableEntries, this.query);
            Entry clickedEntry = this.pickRow(mouseX, mouseY, bodyY, this.scrollAll, filteredAvailable);
            if (clickedEntry != null) {
                this.addToWhitelist(clickedEntry);
            }
            return true;
        }
        if (mouseX >= (double)this.colX2 && mouseX < (double)(this.colX2 + 150)) {
            List<Entry> whitelistedEntries = this.allEntries.stream().filter(entry -> whitelistedIds.contains(entry.id)).toList();
            Entry clickedEntry = this.pickRow(mouseX, mouseY, bodyY, this.scrollWhite, whitelistedEntries);
            if (clickedEntry != null) {
                this.removeFromWhitelist(clickedEntry);
            }
            return true;
        }
        return super.mouseClicked(mouseButtonEvent, doubleClick);
    }

    private Entry pickRow(double mouseX, double mouseY, int bodyY, float scroll, List<Entry> entries) {
        int relY = (int)(mouseY - (double)bodyY);
        int idx = (int)scroll + relY / 18;
        if (idx < 0 || idx >= entries.size()) {
            return null;
        }
        return entries.get(idx);
    }

    private void addToWhitelist(Entry targetEntry) {
        if (targetEntry.id.startsWith(GROUP_PREFIX)) {
            String family = targetEntry.id.substring(GROUP_PREFIX.length());
            if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
                BuiltInRegistries.BLOCK.entrySet().forEach(entry -> {
                    String blockPath = ((ResourceKey)entry.getKey()).identifier().toString().substring("minecraft:".length());
                    if (family.equals(WhitelistEditorScreen.familyOf(blockPath)) || blockPath.equals(family)) {
                        this.setting.add(entry.getValue());
                    }
                });
            } else {
                BuiltInRegistries.ITEM.entrySet().forEach(entry -> {
                    String itemPath = ((ResourceKey)entry.getKey()).identifier().toString().substring("minecraft:".length());
                    if (family.equals(WhitelistEditorScreen.familyOf(itemPath)) || itemPath.equals(family)) {
                        this.setting.add(entry.getValue());
                    }
                });
            }
            return;
        }
        if (this.setting.getType() == WhitelistSetting.Type.BLOCKS) {
            this.setting.add(IdentifierUtils.getBlock(targetEntry.id));
        } else if (this.setting.getType() == WhitelistSetting.Type.POTIONS) {
            this.setting.add(IdentifierUtils.getPotion(targetEntry.id));
        } else {
            this.setting.add(IdentifierUtils.getItem(targetEntry.id));
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

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= (double)this.colX1 && mouseX < (double)(this.colX1 + 150)) {
            List<String> whitelistedIds = this.setting.getWhitelistIds();
            List<Entry> availableEntries = this.allEntries.stream().filter(entry -> !whitelistedIds.contains(entry.id)).toList();
            List<Entry> filteredAvailable = this.query.isEmpty() ? availableEntries : this.filter(availableEntries, this.query);
            this.targetScrollAll = this.clampScroll(this.targetScrollAll - (float)verticalAmount, filteredAvailable.size());
        } else if (mouseX >= (double)this.colX2 && mouseX < (double)(this.colX2 + 150)) {
            this.targetScrollWhite = this.clampScroll(this.targetScrollWhite - (float)verticalAmount, this.setting.getWhitelistIds().size());
        }
        this.scrollAll += (this.targetScrollAll - this.scrollAll) * 0.5f;
        this.scrollWhite += (this.targetScrollWhite - this.scrollWhite) * 0.5f;
        return true;
    }

    private float clampScroll(float scroll, int totalItems) {
        int bodyHeight = this.colHeight - 13;
        float maxScroll = Math.max(0.0f, (float)totalItems - (float)bodyHeight / 18.0f);
        return Math.max(0.0f, Math.min(scroll, maxScroll));
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        int keyCode = keyEvent.key();
        if (keyCode == 256) {
            this.requestClose();
            return true;
        }
        if (keyCode == 259 && !this.query.isEmpty()) {
            this.setQuery(this.query.substring(0, this.query.length() - 1));
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    public boolean charTyped(CharacterEvent characterEvent) {
        char ch = (char)characterEvent.codepoint();
        if (!Character.isISOControl(ch)) {
            this.setQuery(this.query + ch);
            return true;
        }
        return super.charTyped(characterEvent);
    }

    private void setQuery(String newQuery) {
        if (!newQuery.equals(this.lastQuery)) {
            this.queryChangeTime = System.currentTimeMillis();
            this.lastQuery = newQuery;
            this.scrollAll = 0.0f;
            this.targetScrollAll = 0.0f;
        }
        this.query = newQuery;
    }

    public boolean isPauseScreen() {
        return false;
    }

    private static class Entry {
        final String id;
        final String name;
        final ItemStack icon;

        Entry(String id, String name, ItemStack icon, Object unused) {
            this.id = id;
            this.name = name;
            this.icon = icon;
        }
    }
}

