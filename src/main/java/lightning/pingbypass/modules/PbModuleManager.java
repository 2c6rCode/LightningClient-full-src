/*
 * Decompiled with CFR 0.152.
 */
package lightning.pingbypass.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lightning.pingbypass.modules.PbModule;

public class PbModuleManager {
    public static final Set<String> MIGRATED_MODULE_NAMES = Set.of("AutoCrystal", "AutoTotem", "SpeedMine");
    private final List<PbModule> modules = new ArrayList<PbModule>();

    public void register(PbModule pbModule) {
        this.modules.add(pbModule);
        this.modules.sort(Comparator.comparing(PbModule::getName));
    }

    public PbModule getModule(String string) {
        return this.modules.stream().filter(pbModule -> pbModule.getName().equalsIgnoreCase(string)).findFirst().orElse(null);
    }

    public List<PbModule> getModules() {
        return this.modules;
    }

    public void tick() {
        for (PbModule pbModule : this.modules) {
            if (!pbModule.isToggled()) continue;
            pbModule.tick();
        }
    }
}

