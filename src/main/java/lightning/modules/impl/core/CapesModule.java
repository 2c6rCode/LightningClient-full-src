/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.resources.Identifier
 */
package lightning.modules.impl.core;

import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lombok.Generated;
import net.minecraft.resources.Identifier;

@RegisterModule(name="Capes", description="Applies the Lightning cape to yourself and to other users.", category=Module.Category.CORE, toggled=true, drawn=false)
public class CapesModule
extends Module {
    private final Identifier capeTexture = Identifier.fromNamespaceAndPath((String)"lightning", (String)"cape");

    @Generated
    public Identifier getCapeTexture() {
        return this.capeTexture;
    }
}

