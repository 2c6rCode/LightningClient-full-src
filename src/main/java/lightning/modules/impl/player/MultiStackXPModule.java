/*
 * Decompiled with CFR 0.152.
 */
package lightning.modules.impl.player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerUpdateEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BindSetting;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.utils.input.KeyboardUtils;

@RegisterModule(name="MultiStack XP", description="One bind press throws a full XP stack in a single packet burst with optional click rotation and AltSwap inventory selection.", category=Module.Category.PLAYER)
public final class MultiStackXPModule
extends Module {
    private final BindSetting throwBind = new BindSetting("ThrowBind", "Hold the bind to keep throwing XP stacks; each stack is sent in one packet burst.", 0);
    private final ModeSetting switchMode = new ModeSetting("Switch", "How to select the XP-bottle slot.", "Silent", new String[]{"Silent", "Normal", "AltSwap"});
    private final BooleanSetting clickRotate = new BooleanSetting("Click Rotate", "Uses the same silent straight-down rotation as XP in KeyAction.", true);
    private final NumberSetting delayTicks = new NumberSetting("Packet Delay (ticks)", "Ticks between individual XP use packets; each packet throws one bottle only.", 3, 1, 10);
    private boolean wasDown;
    private boolean active;
    private int remaining;
    private int xpSlot = -1;
    private int oldSlot = -1;
    private boolean didSwitch;
    private String runMode = "Silent";
    private boolean errorReported;
    private int ticksUntilNextPacket;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent playerUpdateEvent) {
        boolean bl;
        int n = this.throwBind.getValue();
        boolean bl2 = bl = n != 0 && KeyboardUtils.isBindDown(n);
        if (this.getNull()) {
            if (this.active) {
                this.finish(true);
            }
            return;
        }
        if (bl && !this.active) {
            this.startOneStack();
        }
        if (this.active) {
            this.sendNextPacedPacket();
        }
    }

    private void startOneStack() {
        try {
            Object object = MultiStackXPModule.staticField("lightning.utils.IMinecraft", "mc");
            Object object2 = MultiStackXPModule.field(object, "player");
            if (object2 == null) {
                return;
            }
            Object object3 = MultiStackXPModule.noArgs(object2, "getInventory");
            this.oldSlot = ((Number)MultiStackXPModule.noArgs(object3, "getSelectedSlot")).intValue();
            Object object4 = MultiStackXPModule.staticField("net.minecraft.world.item.Items", "EXPERIENCE_BOTTLE");
            this.runMode = this.switchMode.getValue();
            int n = this.runMode.equalsIgnoreCase("AltSwap") ? 35 : 8;
            this.xpSlot = MultiStackXPModule.findItemSlot(object3, object4, n);
            if (this.xpSlot < 0 || this.xpSlot > 8 && !this.runMode.equalsIgnoreCase("AltSwap")) {
                this.notifyUser(this.runMode.equalsIgnoreCase("AltSwap") ? "No XP bottles found in the inventory." : "No XP bottles found in the hotbar.");
                this.resetOperation();
                return;
            }
            Object object5 = MultiStackXPModule.intArg(object3, "getItem", this.xpSlot);
            int n2 = ((Number)MultiStackXPModule.noArgs(object5, "getCount")).intValue();
            this.remaining = Math.min(2, n2);
            if (this.remaining <= 0) {
                this.resetOperation();
                return;
            }
            boolean bl = this.runMode.equalsIgnoreCase("AltSwap");
            boolean bl2 = this.didSwitch = !false && this.xpSlot != this.oldSlot;
            if (this.didSwitch && !MultiStackXPModule.switchSlot(this.runMode, this.xpSlot, this.oldSlot)) {
                this.notifyUser("Could not select the XP bottle slot.");
                this.resetOperation();
                return;
            }
            this.active = true;
            this.errorReported = false;
            this.ticksUntilNextPacket = 0;
        }
        catch (Throwable throwable) {
            this.notifyFailure(throwable);
            this.finish(true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void sendNextPacedPacket() {
        if (this.remaining <= 0) {
            this.finish(true);
            return;
        }
        try {
            Object object = MultiStackXPModule.staticField("lightning.utils.IMinecraft", "mc");
            Object object2 = MultiStackXPModule.field(object, "player");
            if (object2 == null) {
                this.finish(false);
                return;
            }
            float f = ((Number)MultiStackXPModule.noArgs(object2, "getYRot")).floatValue();
            float f2 = ((Number)MultiStackXPModule.noArgs(object2, "getXRot")).floatValue();
            Class<?> clazz = Class.forName("lightning.utils.minecraft.NetworkUtils$SequencedPacketCreator");
            Class<?> clazz2 = Class.forName("lightning.utils.minecraft.NetworkUtils");
            Method method = clazz2.getMethod("sendSequencedPacket", clazz);
            if (this.clickRotate.getValue()) {
                float[] fArray = MultiStackXPModule.keyActionFeetRotation(object2);
                f = fArray[0];
                f2 = fArray[1];
                MultiStackXPModule.silentRotateLikeKeyAction(f, f2);
            }
            while (this.remaining > 0) {
                boolean bl = this.runMode.equalsIgnoreCase("AltSwap") && this.xpSlot != this.oldSlot;
                boolean bl2 = false;
                try {
                    if (false && !(bl2 = MultiStackXPModule.switchSlot(this.runMode, this.xpSlot, this.oldSlot))) {
                        throw new IllegalStateException("Could not AltSwap XP bottle for this packet.");
                    }
                    Object object3 = MultiStackXPModule.packetCreator(clazz, f, f2);
                    method.invoke(null, object3);
                    --this.remaining;
                }
                finally {
                    if (!bl2) continue;
                    MultiStackXPModule.switchBack(this.runMode, this.xpSlot, this.oldSlot);
                }
            }
            this.errorReported = false;
            this.finish(true);
        }
        catch (Throwable throwable) {
            this.notifyFailure(throwable);
            this.finish(true);
        }
    }

    private static int findItemSlot(Object object, Object object2, int n) throws Exception {
        for (int i = n; i >= 0; --i) {
            Object object3 = MultiStackXPModule.intArg(object, "getItem", i);
            Object object4 = MultiStackXPModule.noArgs(object3, "getItem");
            if (object4 != object2) continue;
            return i;
        }
        return -1;
    }

    private static boolean switchSlot(String string, int n, int n2) throws Exception {
        Class<?> clazz = Class.forName("lightning.utils.minecraft.InventoryUtils");
        Method method = clazz.getMethod("switchSlot", String.class, Integer.TYPE, Integer.TYPE);
        return Boolean.TRUE.equals(method.invoke(null, string, n, n2));
    }

    private static void switchBack(String string, int n, int n2) throws Exception {
        Class<?> clazz = Class.forName("lightning.utils.minecraft.InventoryUtils");
        Method method = clazz.getMethod("switchBack", String.class, Integer.TYPE, Integer.TYPE);
        method.invoke(null, string, n, n2);
    }

    private static float[] keyActionFeetRotation(Object object) throws Exception {
        Object object2;
        Object object3;
        Object object4;
        float f = ((Number)MultiStackXPModule.noArgs(object, "getYRot")).floatValue();
        try {
            object4 = MultiStackXPModule.staticField("lightning.Night", "MODULE_MANAGER");
            if (object4 != null) {
                Object object5;
                object3 = Class.forName("lightning.modules.impl.movement.SprintModule");
                object2 = object4.getClass().getMethod("getModule", Class.class);
                Object object6 = ((Method)object2).invoke(object4, object3);
                if (object6 != null && Boolean.TRUE.equals(MultiStackXPModule.noArgs(object6, "isToggled")) && Boolean.TRUE.equals(MultiStackXPModule.noArgs(object6, "isGrimCompensating")) && (object5 = MultiStackXPModule.noArgs(object6, "getPendingYaw")) instanceof Float) {
                    f = ((Float)object5).floatValue();
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            object4 = MultiStackXPModule.staticField("lightning.Night", "ROTATION_MANAGER");
            if (object4 != null && (object3 = MultiStackXPModule.noArgs(object4, "getRotation")) != null && (object2 = MultiStackXPModule.noArgs(object3, "getYaw")) instanceof Number) {
                f = ((Number)object2).floatValue();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return new float[]{f, 90.0f};
    }

    private static void silentRotateLikeKeyAction(float f, float f2) {
        try {
            Object object = MultiStackXPModule.staticField("lightning.Night", "ROTATION_MANAGER");
            if (object != null) {
                Method method = object.getClass().getMethod("silentRotate", Float.TYPE, Float.TYPE);
                method.invoke(object, Float.valueOf(f), Float.valueOf(f2));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Object packetCreator(Class<?> clazz, float f, float f2) {
        InvocationHandler invocationHandler = (object, method, objectArray) -> {
            String string = method.getName();
            if ("predict".equals(string) && objectArray != null && objectArray.length == 1) {
                return MultiStackXPModule.useItemPacket(((Number)objectArray[0]).intValue(), f, f2);
            }
            if ("toString".equals(string)) {
                return "MultiStack XP AltSwap packet creator";
            }
            if ("hashCode".equals(string)) {
                return System.identityHashCode(object);
            }
            if ("equals".equals(string)) {
                return object == (objectArray == null || objectArray.length == 0 ? null : objectArray[0]);
            }
            return null;
        };
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, invocationHandler);
    }

    private static Object useItemPacket(int n, float f, float f2) throws Exception {
        Class<?> clazz = Class.forName("net.minecraft.world.InteractionHand");
        Enum enum_ = Enum.valueOf(clazz.asSubclass(Enum.class), "MAIN_HAND");
        Class<?> clazz2 = Class.forName("net.minecraft.network.protocol.game.ServerboundUseItemPacket");
        Constructor<?> constructor = clazz2.getConstructor(clazz, Integer.TYPE, Float.TYPE, Float.TYPE);
        return constructor.newInstance(enum_, n, Float.valueOf(f), Float.valueOf(f2));
    }

    private void finish(boolean bl) {
        try {
            if (bl && this.didSwitch && this.xpSlot >= 0 && this.oldSlot >= 0) {
                MultiStackXPModule.switchBack(this.runMode, this.xpSlot, this.oldSlot);
            }
        }
        catch (Throwable throwable) {
            this.notifyFailure(throwable);
        }
        finally {
            this.resetOperation();
        }
    }

    private void resetOperation() {
        this.active = false;
        this.remaining = 0;
        this.xpSlot = -1;
        this.oldSlot = -1;
        this.didSwitch = false;
        this.ticksUntilNextPacket = 0;
    }

    @Override
    public void onDisable() {
        if (this.active) {
            this.finish(true);
        }
        this.wasDown = false;
        super.onDisable();
    }

    private static Object staticField(String string, String string2) throws Exception {
        return Class.forName(string).getField(string2).get(null);
    }

    private static Object field(Object object, String string) throws Exception {
        for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(string);
                field.setAccessible(true);
                return field.get(object);
            }
            catch (NoSuchFieldException noSuchFieldException) {
                continue;
            }
        }
        throw new NoSuchFieldException(string);
    }

    private static Object noArgs(Object object, String string) throws Exception {
        Method method = MultiStackXPModule.findMethod(object.getClass(), string, 0, null);
        method.setAccessible(true);
        return method.invoke(object, new Object[0]);
    }

    private static Object intArg(Object object, String string, int n) throws Exception {
        Method method = MultiStackXPModule.findMethod(object.getClass(), string, 1, Integer.TYPE);
        method.setAccessible(true);
        return method.invoke(object, n);
    }

    private static Method findMethod(Class<?> clazz, String string, int n, Class<?> clazz2) throws NoSuchMethodException {
        for (Class<?> clazz3 = clazz; clazz3 != null; clazz3 = clazz3.getSuperclass()) {
            for (Method method : clazz3.getDeclaredMethods()) {
                if (!method.getName().equals(string) || method.getParameterCount() != n || n != 0 && method.getParameterTypes()[0] != clazz2) continue;
                return method;
            }
        }
        throw new NoSuchMethodException(string);
    }

    private void notifyFailure(Throwable throwable) {
        Throwable throwable2;
        if (this.errorReported) {
            return;
        }
        this.errorReported = true;
        for (throwable2 = throwable; throwable2.getCause() != null && throwable2.getCause() != throwable2; throwable2 = throwable2.getCause()) {
        }
        String string = throwable2.getMessage() == null ? "" : ": " + throwable2.getMessage();
        this.notifyUser(throwable2.getClass().getSimpleName() + string);
    }

    private void notifyUser(String string) {
        try {
            Object object = MultiStackXPModule.staticField("lightning.Night", "CHAT_MANAGER");
            Method method = object.getClass().getMethod("tagged", String.class, String.class);
            method.invoke(object, string, this.getName());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

