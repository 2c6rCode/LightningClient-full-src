/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.Mth
 */
package lightning.gui.screens.account;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import lightning.Night;
import lightning.accounts.Account;
import lightning.accounts.AccountType;
import lightning.gui.screens.account.AddOfflineAccountScreen;
import lightning.gui.screens.account.MicrosoftLoginScreen;
import lightning.modules.impl.core.ClickGuiModule;
import lightning.utils.graphics.Renderer2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class AccountManagerScreen
extends Screen {
    private final Screen parent;
    private Account selectedAccount = null;
    private float scrollOffset = 0.0f;
    private float targetScroll = 0.0f;
    private String statusMessage = "";
    private long statusMessageTime = 0L;
    private long lastClickTime = 0L;
    private Account lastClickedAccount = null;
    private final Map<Account, Float> hoverAnimations = new HashMap<Account, Float>();
    private Button loginButton;
    private Button deleteButton;

    public AccountManagerScreen(Screen parent) {
        super((Component)Component.literal((String)"Account Manager"));
        this.parent = parent;
    }

    protected void init() {
        int centerX = this.width / 2;
        int buttonY = this.height - 55;
        this.loginButton = Button.builder((Component)Component.literal((String)"Login"), button -> this.loginSelected()).bounds(centerX - 160, buttonY, 102, 20).build();
        this.addRenderableWidget(this.loginButton);
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Add Microsoft"), button -> Minecraft.getInstance().gui.setScreen((Screen)new MicrosoftLoginScreen(this))).bounds(centerX - 52, buttonY, 104, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Add Offline"), button -> Minecraft.getInstance().gui.setScreen((Screen)new AddOfflineAccountScreen(this))).bounds(centerX + 58, buttonY, 102, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Random Offline"), button -> {
            String[] prefixes = new String[]{"Shadow", "Lightning", "Ghost", "Viper", "Phantom", "Raven", "Frost", "Nova", "Dark", "Storm"};
            String username = prefixes[new Random().nextInt(prefixes.length)] + "_" + (100 + new Random().nextInt(900));
            this.selectedAccount = Night.ACCOUNT_MANAGER.loginOffline(username);
            this.setStatus("Logged in as " + username);
        }).bounds(centerX - 160, buttonY + 25, 102, 20).build());
        this.deleteButton = Button.builder((Component)Component.literal((String)"Delete"), button -> {
            if (this.selectedAccount != null) {
                Night.ACCOUNT_MANAGER.removeAccount(this.selectedAccount);
                this.selectedAccount = null;
                this.setStatus("Account deleted");
            }
        }).bounds(centerX - 52, buttonY + 25, 104, 20).build();
        this.addRenderableWidget(this.deleteButton);
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Back"), button -> this.onClose()).bounds(centerX + 58, buttonY + 25, 102, 20).build());
        this.updateButtonStates();
    }

    private void loginSelected() {
        if (this.selectedAccount != null) {
            this.setStatus("Logging in to " + this.selectedAccount.getName() + "...");
            CompletableFuture.runAsync(() -> {
                boolean success = Night.ACCOUNT_MANAGER.login(this.selectedAccount);
                if (success) {
                    this.setStatus("Logged in as " + this.selectedAccount.getName());
                } else {
                    this.setStatus("\u00a7cFailed to login to " + this.selectedAccount.getName());
                }
            });
        }
    }

    private void updateButtonStates() {
        if (this.loginButton != null) {
            this.loginButton.active = this.selectedAccount != null;
        }
        if (this.deleteButton != null) {
            this.deleteButton.active = this.selectedAccount != null;
        }
    }

    private void setStatus(String message) {
        this.statusMessage = message;
        this.statusMessageTime = System.currentTimeMillis();
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, 0.0f, this.width, this.height, new Color(24, 24, 24), new Color(12, 12, 12));
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTicks) {
        int centerX = this.width / 2;
        String title = "Account Manager";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, title, centerX - Night.FONT_MANAGER.getWidth(title) / 2, 12, Color.WHITE);
        String currentName = Night.ACCOUNT_MANAGER.getCurrentName();
        if (System.currentTimeMillis() - this.statusMessageTime < 5000L && !this.statusMessage.isEmpty()) {
            Color statusColor = this.statusMessage.startsWith("\u00a7c") ? new Color(255, 100, 100) : (this.statusMessage.startsWith("Logging") ? new Color(200, 200, 200) : new Color(255, 255, 100));
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.statusMessage, centerX - Night.FONT_MANAGER.getWidth(this.statusMessage) / 2, 28, statusColor);
        } else {
            String loggedInText = "Logged in as: \u00a7f" + currentName;
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, loggedInText, centerX - Night.FONT_MANAGER.getWidth(loggedInText) / 2, 28, new Color(180, 180, 180));
        }
        int listX = centerX - 160;
        int listY = 55;
        int listWidth = 320;
        int listHeight = this.height - 120;
        Renderer2D.renderQuad(guiGraphicsExtractor, listX, listY, listX + listWidth, listY + listHeight, new Color(24, 24, 24, 230));
        Renderer2D.renderOutline(guiGraphicsExtractor, listX, listY, listX + listWidth, listY + listHeight, new Color(60, 60, 60, 160));
        this.scrollOffset += (this.targetScroll - this.scrollOffset) * 0.3f;
        List<Account> accounts = Night.ACCOUNT_MANAGER.getAccounts();
        int rowHeight = 32;
        int maxScroll = Math.max(0, accounts.size() * rowHeight - listHeight);
        this.targetScroll = Mth.clamp((float)this.targetScroll, (float)(-maxScroll), (float)0.0f);
        guiGraphicsExtractor.enableScissor(listX, listY, listX + listWidth, listY + listHeight);
        float entryY = (float)listY + this.scrollOffset;
        for (Account account : accounts) {
            if (entryY + (float)rowHeight >= (float)listY && entryY <= (float)(listY + listHeight)) {
                boolean isSelected = account == this.selectedAccount;
                boolean isActive = account.getName().equalsIgnoreCase(currentName);
                boolean isHovered = mouseX >= listX + 4 && mouseX <= listX + listWidth - 4 && (float)mouseY >= entryY + 2.0f && (float)mouseY <= entryY + (float)rowHeight - 2.0f && mouseY >= listY && mouseY <= listY + listHeight;
                float targetHover = isHovered ? 1.0f : 0.0f;
                float hoverAnim = this.hoverAnimations.getOrDefault(account, Float.valueOf(0.0f)).floatValue();
                hoverAnim += (targetHover - hoverAnim) * 0.22f;
                if (Math.abs(targetHover - hoverAnim) < 0.005f) {
                    hoverAnim = targetHover;
                }
                this.hoverAnimations.put(account, Float.valueOf(hoverAnim));
                Color cardColor = isSelected ? new Color(50, 50, 50, 220) : (isActive ? new Color(40, 40, 40, 180) : new Color(30, 30, 30, 150));
                Renderer2D.renderQuad(guiGraphicsExtractor, listX + 4, entryY + 2.0f, listX + listWidth - 4, entryY + (float)rowHeight - 2.0f, cardColor);
                Color accent = this.getAccentColor();
                if (hoverAnim > 0.001f) {
                    float fillWidth = (float)(listWidth - 8) * hoverAnim;
                    Color leftGlow = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(90.0f * hoverAnim));
                    Color rightGlow = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(20.0f * hoverAnim));
                    Renderer2D.renderSidewaysGradient(guiGraphicsExtractor, listX + 4, entryY + 2.0f, (float)(listX + 4) + fillWidth, entryY + (float)rowHeight - 2.0f, leftGlow, rightGlow);
                    Color edgeLine = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(200.0f * hoverAnim));
                    Renderer2D.renderQuad(guiGraphicsExtractor, (float)(listX + 3) + fillWidth, entryY + 2.0f, (float)(listX + 5) + fillWidth, entryY + (float)rowHeight - 2.0f, edgeLine);
                }
                float barStrength = isSelected ? 1.0f : hoverAnim;
                if (barStrength > 0.001f) {
                    float barWidth = isSelected ? 3.5f : 3.0f * hoverAnim;
                    Color barColor = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), (int)(255.0f * barStrength));
                    Renderer2D.renderQuad(guiGraphicsExtractor, listX + 4, entryY + 2.0f, (float)(listX + 4) + barWidth, entryY + (float)rowHeight - 2.0f, barColor);
                }
                float textOffset = 3.0f * hoverAnim;
                String nameText = (isActive ? "\u00a7a\u2713 " : "") + account.getName();
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, nameText, (int)((float)(listX + 14) + textOffset), (int)(entryY + 6.0f), isActive ? Color.GREEN : Color.WHITE);
                String typeTag = "[" + account.getType().getDisplayName() + "]";
                Color typeColor = switch (account.getType()) {
                    default -> throw new MatchException(null, null);
                    case AccountType.MICROSOFT -> new Color(0, 220, 255);
                    case AccountType.OFFLINE -> new Color(255, 200, 80);
                    case AccountType.SESSION -> new Color(220, 120, 255);
                };
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, typeTag, (int)((float)(listX + 14) + textOffset), (int)(entryY + 18.0f), typeColor);
                if (isActive) {
                    String activeText = "\u00a7aACTIVE";
                    Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, activeText, listX + listWidth - Night.FONT_MANAGER.getWidth(activeText) - 12, (int)(entryY + 11.0f), Color.GREEN);
                }
            }
            entryY += (float)rowHeight;
        }
        guiGraphicsExtractor.disableScissor();
        if (accounts.isEmpty()) {
            String emptyText = "No saved accounts. Click 'Add Offline' or 'Add Microsoft' below.";
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, emptyText, centerX - Night.FONT_MANAGER.getWidth(emptyText) / 2, listY + listHeight / 2 - 5, new Color(140, 140, 160));
        }
        this.updateButtonStates();
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTicks);
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        int button = mouseButtonEvent.button();
        int centerX = this.width / 2;
        int listX = centerX - 160;
        int listY = 55;
        int listWidth = 320;
        int listHeight = this.height - 120;
        if (button == 0 && mouseX >= (double)listX && mouseX <= (double)(listX + listWidth) && mouseY >= (double)listY && mouseY <= (double)(listY + listHeight)) {
            List<Account> accounts = Night.ACCOUNT_MANAGER.getAccounts();
            float relY = (float)(mouseY - (double)listY - (double)this.scrollOffset);
            int rowHeight = 32;
            int index = (int)(relY / (float)rowHeight);
            if (index >= 0 && index < accounts.size()) {
                Account account = accounts.get(index);
                long now = System.currentTimeMillis();
                if (account == this.lastClickedAccount && (now - this.lastClickTime < 350L || isDoubleClick)) {
                    this.selectedAccount = account;
                    this.loginSelected();
                } else {
                    this.selectedAccount = account;
                }
                this.lastClickedAccount = account;
                this.lastClickTime = now;
                this.updateButtonStates();
                return true;
            }
        }
        return super.mouseClicked(mouseButtonEvent, isDoubleClick);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        List<Account> accounts = Night.ACCOUNT_MANAGER.getAccounts();
        int listHeight = this.height - 120;
        int rowHeight = 32;
        int maxScroll = Math.max(0, accounts.size() * rowHeight - listHeight);
        this.targetScroll += (float)(verticalAmount * 24.0);
        this.targetScroll = Mth.clamp((float)this.targetScroll, (float)(-maxScroll), (float)0.0f);
        return true;
    }

    private Color getAccentColor() {
        ClickGuiModule clickGuiModule = Night.MODULE_MANAGER.getModule(ClickGuiModule.class);
        if (clickGuiModule != null) {
            return clickGuiModule.color.getColor();
        }
        return new Color(130, 90, 240);
    }

    public void onClose() {
        Minecraft.getInstance().gui.setScreen(this.parent);
    }
}

