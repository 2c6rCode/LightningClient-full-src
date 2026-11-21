/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.GpuFormat
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.texture.AbstractTexture
 */
package lightning.utils.graphics;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import lightning.Night;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;

public class CustomImageTexture
extends AbstractTexture {
    public static final CustomImageTexture INSTANCE = new CustomImageTexture();
    private NativeImage currentPixels;

    public void update(NativeImage nativeImage) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && !minecraft.isSameThread()) {
            minecraft.execute(() -> this.updateDirect(nativeImage));
        } else {
            this.updateDirect(nativeImage);
        }
    }

    private synchronized void updateDirect(NativeImage nativeImage) {
        if (this.currentPixels != null && this.currentPixels != nativeImage) {
            try {
                this.currentPixels.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        this.currentPixels = nativeImage;
        if (this.texture != null) {
            try {
                this.texture.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.texture = null;
        }
        if (nativeImage == null) {
            this.textureView = null;
            return;
        }
        try {
            GpuDevice gpuDevice = RenderSystem.getDevice();
            if (gpuDevice == null) {
                return;
            }
            this.texture = gpuDevice.createTexture(() -> "lightning/custom_image", 5, GpuFormat.RGBA8_UNORM, nativeImage.getWidth(), nativeImage.getHeight(), 1, 1);
            this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR);
            this.textureView = gpuDevice.createTextureView(this.texture);
            gpuDevice.createCommandEncoder().writeToTexture(this.texture, nativeImage);
        }
        catch (Throwable throwable) {
            Night.LOGGER.error("Failed to upload custom image texture to GPU", throwable);
            if (this.texture != null) {
                try {
                    this.texture.close();
                }
                catch (Throwable throwable2) {
                    // empty catch block
                }
                this.texture = null;
            }
            this.textureView = null;
        }
    }

    public GpuTextureView getTextureView() {
        if (this.textureView == null) {
            String string;
            if (Night.IMAGE_MANAGER != null && (string = Night.IMAGE_MANAGER.getCurrentImageName()) != null && !string.equalsIgnoreCase("None") && !string.isEmpty()) {
                Night.IMAGE_MANAGER.setCurrentActiveImage(string);
            }
            if (this.textureView == null) {
                try {
                    NativeImage nativeImage = new NativeImage(1, 1, false);
                    nativeImage.setPixel(0, 0, 0);
                    this.updateDirect(nativeImage);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        return super.getTextureView();
    }

    public synchronized void close() {
        if (this.currentPixels != null) {
            try {
                this.currentPixels.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.currentPixels = null;
        }
        if (this.texture != null) {
            try {
                this.texture.close();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.texture = null;
        }
        this.textureView = null;
    }
}

