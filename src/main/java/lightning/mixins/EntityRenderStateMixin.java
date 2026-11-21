/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lightning.utils.graphics.Glint
 *  lightning.utils.graphics.Glint$GlintParams
 *  net.minecraft.client.renderer.entity.state.EntityRenderState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package lightning.mixins;

import lightning.utils.graphics.Glint;
import lightning.utils.mixins.IChamsCapture;
import lightning.utils.mixins.ISelfState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={EntityRenderState.class})
public class EntityRenderStateMixin
implements IChamsCapture,
ISelfState {
    @Unique
    private boolean chamsFill = false;
    @Unique
    private int chamsFillColor = 0;
    @Unique
    private boolean chamsOutline = false;
    @Unique
    private int chamsOutlineColor = 0;
    @Unique
    private boolean chamsShine = false;
    @Unique
    private boolean chamsGlint = false;
    @Unique
    private int chamsGlintColor = -1;
    @Unique
    private Glint.GlintParams chamsGlintParams = Glint.ENTITY;
    @Unique
    private boolean chamsSuppressReal = false;
    @Unique
    private float chamsRealAlpha = 1.0f;
    @Unique
    private float chamsYOffset = 0.0f;
    @Unique
    private boolean isSelf = false;

    @Override
    public float night$chamsYOffset() {
        return this.chamsYOffset;
    }

    @Override
    public void night$setChamsYOffset(float f) {
        this.chamsYOffset = f;
    }

    @Override
    public boolean night$isSelf() {
        return this.isSelf;
    }

    @Override
    public void night$setSelf(boolean bl) {
        this.isSelf = bl;
    }

    @Override
    public boolean night$chamsFill() {
        return this.chamsFill;
    }

    @Override
    public int night$chamsFillColor() {
        return this.chamsFillColor;
    }

    @Override
    public boolean night$chamsOutline() {
        return this.chamsOutline;
    }

    @Override
    public int night$chamsOutlineColor() {
        return this.chamsOutlineColor;
    }

    @Override
    public boolean night$chamsShine() {
        return this.chamsShine;
    }

    @Override
    public boolean night$chamsGlint() {
        return this.chamsGlint;
    }

    @Override
    public int night$chamsGlintColor() {
        return this.chamsGlintColor;
    }

    @Override
    public Glint.GlintParams night$chamsGlintParams() {
        return this.chamsGlintParams;
    }

    @Override
    public boolean night$chamsSuppressReal() {
        return this.chamsSuppressReal;
    }

    @Override
    public float night$chamsRealAlpha() {
        return this.chamsRealAlpha;
    }

    @Override
    public void night$setChamsRealAlpha(float f) {
        this.chamsRealAlpha = f;
    }

    @Override
    public void night$setChamsGlint(boolean bl, int n) {
        this.chamsGlint = bl;
        this.chamsGlintColor = n;
        this.chamsGlintParams = Glint.ENTITY;
    }

    @Override
    public void night$setChamsGlint(boolean bl, int n, Glint.GlintParams glintParams) {
        this.chamsGlint = bl;
        this.chamsGlintColor = n;
        this.chamsGlintParams = glintParams != null ? glintParams : Glint.ENTITY;
    }

    @Override
    public void night$setChams(boolean bl, int n, boolean bl2, int n2, boolean bl3) {
        this.night$setChams(bl, n, bl2, n2, bl3, false);
    }

    @Override
    public void night$setChams(boolean bl, int n, boolean bl2, int n2, boolean bl3, boolean bl4) {
        this.chamsFill = bl;
        this.chamsFillColor = n;
        this.chamsOutline = bl2;
        this.chamsOutlineColor = n2;
        this.chamsShine = bl3;
        this.chamsSuppressReal = bl4;
        this.chamsRealAlpha = 1.0f;
        this.chamsGlint = false;
        this.chamsGlintColor = -1;
        this.chamsGlintParams = Glint.ENTITY;
    }
}

