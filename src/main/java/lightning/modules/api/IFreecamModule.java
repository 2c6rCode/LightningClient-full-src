/*
 * Decompiled with CFR 0.152.
 */
package lightning.modules.api;

public interface IFreecamModule {
    public boolean isToggled();

    public boolean isRotate();

    public void onMouseTurn(double deltaX, double deltaY);

    public float getFreeYaw();

    public float getFreePitch();

    public double getFreeX();

    public double getFreeY();

    public double getFreeZ();
}

