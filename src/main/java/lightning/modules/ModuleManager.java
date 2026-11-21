/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
 *  lombok.Generated
 */
package lightning.modules;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lightning.Night;
import lightning.core.JarClassScanner;
import lightning.events.SubscribeEvent;
import lightning.events.impl.KeyInputEvent;
import lightning.events.impl.MouseInputEvent;
import lightning.events.impl.TickEvent;
import lightning.gui.api.Frame;
import lightning.gui.impl.ModuleButton;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.modules.impl.core.HUDModule;
import lightning.modules.impl.core.PingBypassModule;
import lightning.modules.impl.player.AutoShopModule;
import lightning.settings.Setting;
import lightning.settings.impl.CategorySetting;
import lightning.utils.IMinecraft;
import lightning.utils.input.KeyboardUtils;
import lombok.Generated;

public class ModuleManager
implements IMinecraft {
    private final List<Module> modules = new ArrayList<Module>();
    private final Map<Class<? extends Module>, Module> moduleClasses = new Reference2ReferenceOpenHashMap();

    public ModuleManager() {
        Night.EVENT_HANDLER.subscribe(this);
        for (Class<?> clazz : JarClassScanner.findSubtypesOf(Module.class, Night.class)) {
            try {
                this.registerIfAnnotated(clazz);
            } catch (Exception exception) {
                Night.LOGGER.warn("Failed to register module {}", clazz.getName(), exception);
            }
        }
        this.modules.sort(Comparator.comparing(Module::getName));
    }

    private void registerIfAnnotated(Class<?> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (clazz.getAnnotation(RegisterModule.class) == null) {
            return;
        }
        Module module = (Module)clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        this.register(module);
    }

    public void register(Module module) {
        if (module == null) {
            return;
        }
        try {
            for (Field field : module.getClass().getDeclaredFields()) {
                Setting setting;
                if (!Setting.class.isAssignableFrom(field.getType())) continue;
                if (!field.canAccess(module)) {
                    field.setAccessible(true);
                }
                if ((setting = (Setting)field.get(module)) == null) continue;
                if (module instanceof HUDModule) {
                    CategorySetting.Visibility visibility;
                    Setting.Visibility visibility2;
                    HUDModule hUDModule = (HUDModule)module;
                    if (!HUDModule.isMusicAddonLoaded() && (setting == hUDModule.musicHudCategory || setting == hUDModule.musicHudPosition || (visibility2 = setting.getVisibility()) instanceof CategorySetting.Visibility && (visibility = (CategorySetting.Visibility)visibility2).getValue() == hUDModule.musicHudCategory)) continue;
                }
                module.getSettings().add(setting);
            }
        }
        catch (IllegalAccessException illegalAccessException) {
            Night.LOGGER.error("Failed to register settings for module " + module.getName(), (Throwable)illegalAccessException);
        }
        module.getSettings().add(module.chatNotify);
        module.getSettings().add(module.drawn);
        if (!module.isPersistent() && !(module instanceof AutoShopModule)) {
            module.getSettings().add(module.bind);
        }
        if (!this.modules.contains(module)) {
            this.modules.add(module);
            this.moduleClasses.put(module.getClass(), module);
            this.modules.sort(Comparator.comparing(Module::getName));
            if (Night.CLICK_GUI != null) {
                for (Frame frame : Night.CLICK_GUI.getFrames()) {
                    if (frame.getCategory() != module.getCategory() || module instanceof PingBypassModule) continue;
                    frame.getButtons().add(new ModuleButton(module, frame, frame.getHeight()));
                }
            }
        }
    }

    public void unregister(Module module) {
        if (module == null) {
            return;
        }
        if (module.isToggled()) {
            module.setToggled(false);
        }
        this.modules.remove(module);
        this.moduleClasses.remove(module.getClass());
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent keyInputEvent) {
        if (ModuleManager.mc.gui != null && ModuleManager.mc.gui.screen() != null) {
            return;
        }
        this.modules.stream().filter(module -> module.getBind() == keyInputEvent.getKey() && module.bind.getMode().equals("Bind")).forEach(module -> module.setToggled(!module.isToggled()));
        AutoShopModule autoShopModule = this.getModule(AutoShopModule.class);
        if (autoShopModule != null) {
            autoShopModule.handleKey(keyInputEvent.getKey());
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent mouseInputEvent) {
        if (ModuleManager.mc.gui != null && ModuleManager.mc.gui.screen() != null) {
            return;
        }
        this.modules.stream().filter(module -> module.getBind() == -mouseInputEvent.getButton() - 1 && module.bind.getMode().equals("Bind")).forEach(module -> module.setToggled(!module.isToggled()));
        AutoShopModule autoShopModule = this.getModule(AutoShopModule.class);
        if (autoShopModule != null) {
            autoShopModule.handleKey(-mouseInputEvent.getButton() - 1);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (ModuleManager.mc.gui != null && ModuleManager.mc.gui.screen() != null) {
            return;
        }
        for (Module module : this.modules) {
            String string = module.bind.getMode();
            if (string.equals("Bind") || module.getBind() == 0) continue;
            boolean bl = KeyboardUtils.isBindDown(module.getBind());
            boolean bl2 = string.equals("Hold") == bl;
            boolean bl3 = bl2;
            if (module.isToggled() == bl2) continue;
            module.setToggled(bl2);
        }
    }

    public List<Module> getModules(Module.Category category) {
        return this.modules.stream().filter(module -> module.getCategory() == category).toList();
    }

    public Module getModule(String string) {
        return this.modules.stream().filter(module -> module.getName().equalsIgnoreCase(string)).findFirst().orElse(null);
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        return (T)this.moduleClasses.get(clazz);
    }

    @Generated
    public List<Module> getModules() {
        return this.modules;
    }

    @Generated
    public Map<Class<? extends Module>, Module> getModuleClasses() {
        return this.moduleClasses;
    }
}

