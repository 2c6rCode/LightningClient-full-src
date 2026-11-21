/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.ClickEvent$OpenFile
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Style
 *  net.minecraft.util.Util
 */
package lightning.modules.impl.miscellaneous;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import lightning.Night;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.utils.chat.ChatUtils;
import lightning.utils.system.ImageTransferable;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Util;

@RegisterModule(name="Screenshot", description="Automatically copies screenshots to clipboard when pressing F2.", category=Module.Category.MISCELLANEOUS, toggled=true)
public class ScreenshotModule
extends Module {
    public BooleanSetting clipboard = new BooleanSetting("Clipboard", "Copies screenshot image directly to your clipboard.", true);
    public BooleanSetting copyFile = new BooleanSetting("CopyFile", "Also includes file flavor for pasting into file explorers.", true);
    public BooleanSetting notify = new BooleanSetting("Notify", "Sends a confirmation message in chat when copied.", true);
    public BooleanSetting openFolder = new BooleanSetting("OpenFolder", "Automatically opens the screenshots folder.", false);

    public void onScreenshotCaptured(Component component) {
        if (!this.isToggled()) {
            return;
        }
        File file = this.extractScreenshotFile(component);
        if (file == null || !file.exists()) {
            file = this.findLatestScreenshotFile();
        }
        if (file == null || !file.exists()) {
            return;
        }
        File file2 = file;
        CompletableFuture.runAsync(() -> {
            try {
                BufferedImage bufferedImage;
                boolean bl = false;
                if (this.clipboard.getValue() && (bufferedImage = ImageIO.read(file2)) != null) {
                    ImageTransferable imageTransferable = new ImageTransferable(bufferedImage, file2, this.copyFile.getValue());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imageTransferable, null);
                    bl = true;
                }
                if (!bl && this.openFolder.getValue()) {
                    Util.getPlatform().openFile(file2.getParentFile());
                }
                if (this.notify.getValue()) {
                    Night.CHAT_MANAGER.message("Screenshot copied to clipboard: " + String.valueOf(ChatUtils.getPrimary()) + file2.getName(), "screenshot-copied");
                }
            }
            catch (Throwable throwable) {
                Night.LOGGER.error("Failed to copy screenshot to clipboard", throwable);
            }
        });
    }

    private File extractScreenshotFile(Component component) {
        ClickEvent clickEvent;
        if (component == null) {
            return null;
        }
        Style style = component.getStyle();
        if (style != null && (clickEvent = style.getClickEvent()) instanceof ClickEvent.OpenFile) {
            ClickEvent.OpenFile openFile = (ClickEvent.OpenFile)clickEvent;
            return openFile.file();
        }
        for (Component component2 : component.getSiblings()) {
            File file = this.extractScreenshotFile(component2);
            if (file == null) continue;
            return file;
        }
        return null;
    }

    private File findLatestScreenshotFile() {
        try {
            File file2 = new File(ScreenshotModule.mc.gameDirectory, "screenshots");
            if (!file2.exists() || !file2.isDirectory()) {
                return null;
            }
            File[] fileArray = file2.listFiles((file, string) -> string.toLowerCase().endsWith(".png"));
            if (fileArray == null || fileArray.length == 0) {
                return null;
            }
            return Arrays.stream(fileArray).max(Comparator.comparingLong(File::lastModified)).orElse(null);
        }
        catch (Throwable throwable) {
            return null;
        }
    }
}

