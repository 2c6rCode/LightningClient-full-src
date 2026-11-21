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

    public AccountManagerScreen(Screen screen) {
        super((Component)Component.literal((String)"Account Manager"));
        this.parent = screen;
    }

    protected void init() {
        int n = this.width / 2;
        int n2 = this.height - 55;
        this.loginButton = Button.builder((Component)Component.literal((String)"Login"), button -> this.loginSelected()).bounds(n - 160, n2, 102, 20).build();
        this.addRenderableWidget(this.loginButton);
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Add Microsoft"), button -> Minecraft.getInstance().gui.setScreen((Screen)new MicrosoftLoginScreen(this))).bounds(n - 52, n2, 104, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Add Offline"), button -> Minecraft.getInstance().gui.setScreen((Screen)new AddOfflineAccountScreen(this))).bounds(n + 58, n2, 102, 20).build());
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Random Offline"), button -> {
            Account account;
            String[] stringArray = new String[]{"Shadow", "Lightning", "Ghost", "Viper", "Phantom", "Raven", "Frost", "Nova", "Dark", "Storm"};
            String string = stringArray[new Random().nextInt(stringArray.length)] + "_" + (100 + new Random().nextInt(900));
            this.selectedAccount = account = Night.ACCOUNT_MANAGER.loginOffline(string);
            this.setStatus("Logged in as " + string);
        }).bounds(n - 160, n2 + 25, 102, 20).build());
        this.deleteButton = Button.builder((Component)Component.literal((String)"Delete"), button -> {
            if (this.selectedAccount != null) {
                Night.ACCOUNT_MANAGER.removeAccount(this.selectedAccount);
                this.selectedAccount = null;
                this.setStatus("Account deleted");
            }
        }).bounds(n - 52, n2 + 25, 104, 20).build();
        this.addRenderableWidget(this.deleteButton);
        this.addRenderableWidget(Button.builder((Component)Component.literal((String)"Back"), button -> this.onClose()).bounds(n + 58, n2 + 25, 102, 20).build());
        this.updateButtonStates();
    }

    private void loginSelected() {
        if (this.selectedAccount != null) {
            this.setStatus("Logging in to " + this.selectedAccount.getName() + "...");
            CompletableFuture.runAsync(() -> {
                boolean bl = Night.ACCOUNT_MANAGER.login(this.selectedAccount);
                if (bl) {
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
            boolean bl = this.loginButton.active;
        }
        if (this.deleteButton != null) {
            this.deleteButton.active = this.selectedAccount != null;
        }
    }

    private void setStatus(String string) {
        this.statusMessage = string;
        this.statusMessageTime = System.currentTimeMillis();
    }

    public void extractBackground(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        Renderer2D.renderGradient(guiGraphicsExtractor, 0.0f, 0.0f, this.width, this.height, new Color(24, 24, 24), new Color(12, 12, 12));
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int n, int n2, float f) {
        int n3 = this.width / 2;
        String string = "Account Manager";
        Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string, n3 - Night.FONT_MANAGER.getWidth(string) / 2, 12, Color.WHITE);
        String string2 = Night.ACCOUNT_MANAGER.getCurrentName();
        if (System.currentTimeMillis() - this.statusMessageTime < 5000L && !this.statusMessage.isEmpty()) {
            Color statusColor = this.statusMessage.startsWith("\u00a7c") ? new Color(255, 100, 100) : (this.statusMessage.startsWith("Logging") ? new Color(200, 200, 200) : new Color(255, 255, 100));
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, this.statusMessage, n3 - Night.FONT_MANAGER.getWidth(this.statusMessage) / 2, 28, statusColor);
        } else {
            String loggedInText = "Logged in as: \u00a7f" + string2;
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, loggedInText, n3 - Night.FONT_MANAGER.getWidth(loggedInText) / 2, 28, new Color(180, 180, 180));
        }
        int n4 = n3 - 160;
        int n5 = 55;
        int n6 = 320;
        int n7 = this.height - 120;
        Renderer2D.renderQuad(guiGraphicsExtractor, n4, n5, n4 + n6, n5 + n7, new Color(24, 24, 24, 230));
        Renderer2D.renderOutline(guiGraphicsExtractor, n4, n5, n4 + n6, n5 + n7, new Color(60, 60, 60, 160));
        this.scrollOffset += (this.targetScroll - this.scrollOffset) * 0.3f;
        List<Account> list = Night.ACCOUNT_MANAGER.getAccounts();
        int n8 = 32;
        int n9 = Math.max(0, list.size() * n8 - n7);
        this.targetScroll = Mth.clamp((float)this.targetScroll, (float)(-n9), (float)0.0f);
        guiGraphicsExtractor.enableScissor(n4, n5, n4 + n6, n5 + n7);
        float f2 = (float)n5 + this.scrollOffset;
        for (Account account : list) {
            if (f2 + (float)n8 >= (float)n5 && f2 <= (float)(n5 + n7)) {
                Object object;
                Object object2;
                float f3;
                float f4;
                boolean bl = account == this.selectedAccount;
                boolean bl2 = account.getName().equalsIgnoreCase(string2);
                boolean bl3 = n >= n4 + 4 && n <= n4 + n6 - 4 && (float)n2 >= f2 + 2.0f && (float)n2 <= f2 + (float)n8 - 2.0f && n2 >= n5 && n2 <= n5 + n7;
                float f5 = bl3 ? 1.0f : 0.0f;
                float f6 = this.hoverAnimations.getOrDefault(account, Float.valueOf(0.0f)).floatValue();
                f6 += (f5 - f6) * 0.22f;
                if (Math.abs(f5 - f6) < 0.005f) {
                    f6 = f5;
                }
                this.hoverAnimations.put(account, Float.valueOf(f6));
                Color color = bl ? new Color(50, 50, 50, 220) : (bl2 ? new Color(40, 40, 40, 180) : new Color(30, 30, 30, 150));
                Renderer2D.renderQuad(guiGraphicsExtractor, n4 + 4, f2 + 2.0f, n4 + n6 - 4, f2 + (float)n8 - 2.0f, color);
                Color color2 = this.getAccentColor();
                if (f6 > 0.001f) {
                    f3 = (float)(n6 - 8) * f6;
                    Color color3 = new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), (int)(90.0f * f6));
                    object2 = new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), (int)(20.0f * f6));
                    Renderer2D.renderSidewaysGradient(guiGraphicsExtractor, n4 + 4, f2 + 2.0f, (float)(n4 + 4) + f3, f2 + (float)n8 - 2.0f, color3, (Color)object2);
                    object = new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), (int)(200.0f * f6));
                    Renderer2D.renderQuad(guiGraphicsExtractor, (float)(n4 + 3) + f3, f2 + 2.0f, (float)(n4 + 5) + f3, f2 + (float)n8 - 2.0f, (Color)object);
                }
                float f7 = bl ? 1.0f : f6;
                f3 = f7;
                if (f7 > 0.001f) {
                    float f8 = bl ? 3.5f : 3.0f * f6;
                    object2 = new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), (int)(255.0f * f7));
                    Renderer2D.renderQuad(guiGraphicsExtractor, n4 + 4, f2 + 2.0f, (float)(n4 + 4) + f8, f2 + (float)n8 - 2.0f, (Color)object2);
                }
                float f9 = 3.0f * f6;
                object2 = (bl2 ? "\u00a7a\u2713 " : "") + account.getName();
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, (String)object2, (int)((float)(n4 + 14) + f9), (int)(f2 + 6.0f), bl2 ? Color.GREEN : Color.WHITE);
                object = "[" + account.getType().getDisplayName() + "]";
                Color color4 = switch (account.getType()) {
                    default -> throw new MatchException(null, null);
                    case AccountType.MICROSOFT -> new Color(0, 220, 255);
                    case AccountType.OFFLINE -> new Color(255, 200, 80);
                    case AccountType.SESSION -> new Color(220, 120, 255);
                };
                Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, (String)object, (int)((float)(n4 + 14) + f9), (int)(f2 + 18.0f), color4);
                if (bl2) {
                    String string3 = "\u00a7aACTIVE";
                    Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string3, n4 + n6 - Night.FONT_MANAGER.getWidth(string3) - 12, (int)(f2 + 11.0f), Color.GREEN);
                }
            }
            f2 += (float)n8;
        }
        guiGraphicsExtractor.disableScissor();
        if (list.isEmpty()) {
            String string4 = "No saved accounts. Click 'Add Offline' or 'Add Microsoft' below.";
            Night.FONT_MANAGER.drawTextWithShadow(guiGraphicsExtractor, string4, n3 - Night.FONT_MANAGER.getWidth(string4) / 2, n5 + n7 / 2 - 5, new Color(140, 140, 160));
        }
        this.updateButtonStates();
        super.extractRenderState(guiGraphicsExtractor, n, n2, f);
    }

    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        double d = mouseButtonEvent.x();
        double d2 = mouseButtonEvent.y();
        int n = mouseButtonEvent.button();
        int n2 = this.width / 2;
        int n3 = n2 - 160;
        int n4 = 55;
        int n5 = 320;
        int n6 = this.height - 120;
        if (n == 0 && d >= (double)n3 && d <= (double)(n3 + n5) && d2 >= (double)n4 && d2 <= (double)(n4 + n6)) {
            List<Account> list = Night.ACCOUNT_MANAGER.getAccounts();
            float f = (float)(d2 - (double)n4 - (double)this.scrollOffset);
            int n7 = 32;
            int n8 = (int)(f / (float)n7);
            if (n8 >= 0 && n8 < list.size()) {
                Account account = list.get(n8);
                long l = System.currentTimeMillis();
                if (account == this.lastClickedAccount && (l - this.lastClickTime < 350L || bl)) {
                    this.selectedAccount = account;
                    this.loginSelected();
                } else {
                    this.selectedAccount = account;
                }
                this.lastClickedAccount = account;
                this.lastClickTime = l;
                this.updateButtonStates();
                return true;
            }
        }
        return super.mouseClicked(mouseButtonEvent, bl);
    }

    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        List<Account> list = Night.ACCOUNT_MANAGER.getAccounts();
        int n = this.height - 120;
        int n2 = 32;
        int n3 = Math.max(0, list.size() * n2 - n);
        this.targetScroll += (float)(d4 * 24.0);
        this.targetScroll = Mth.clamp((float)this.targetScroll, (float)(-n3), (float)0.0f);
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

