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
        int bindKey = this.throwBind.getValue();
        boolean bindDown = bindKey != 0 && KeyboardUtils.isBindDown(bindKey);
        if (this.getNull()) {
            if (this.active) {
                this.finish(true);
            }
            return;
        }
        if (bindDown && !this.active) {
            this.startOneStack();
        }
        if (this.active) {
            this.sendNextPacedPacket();
        }
    }

    private void startOneStack() {
        try {
            Object mcInstance = MultiStackXPModule.staticField("lightning.utils.IMinecraft", "mc");
            Object player = MultiStackXPModule.field(mcInstance, "player");
            if (player == null) {
                return;
            }
            Object inventory = MultiStackXPModule.noArgs(player, "getInventory");
            this.oldSlot = ((Number)MultiStackXPModule.noArgs(inventory, "getSelectedSlot")).intValue();
            Object xpBottleItem = MultiStackXPModule.staticField("net.minecraft.world.item.Items", "EXPERIENCE_BOTTLE");
            this.runMode = this.switchMode.getValue();
            int maxSlot = this.runMode.equalsIgnoreCase("AltSwap") ? 35 : 8;
            this.xpSlot = MultiStackXPModule.findItemSlot(inventory, xpBottleItem, maxSlot);
            if (this.xpSlot < 0 || this.xpSlot > 8 && !this.runMode.equalsIgnoreCase("AltSwap")) {
                this.notifyUser(this.runMode.equalsIgnoreCase("AltSwap") ? "No XP bottles found in the inventory." : "No XP bottles found in the hotbar.");
                this.resetOperation();
                return;
            }
            Object xpStack = MultiStackXPModule.intArg(inventory, "getItem", this.xpSlot);
            int stackCount = ((Number)MultiStackXPModule.noArgs(xpStack, "getCount")).intValue();
            this.remaining = Math.min(2, stackCount);
            if (this.remaining <= 0) {
                this.resetOperation();
                return;
            }
            this.didSwitch = this.xpSlot != this.oldSlot;
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
            Object mcInstance = MultiStackXPModule.staticField("lightning.utils.IMinecraft", "mc");
            Object player = MultiStackXPModule.field(mcInstance, "player");
            if (player == null) {
                this.finish(false);
                return;
            }
            float yaw = ((Number)MultiStackXPModule.noArgs(player, "getYRot")).floatValue();
            float pitch = ((Number)MultiStackXPModule.noArgs(player, "getXRot")).floatValue();
            Class<?> creatorClass = Class.forName("lightning.utils.minecraft.NetworkUtils$SequencedPacketCreator");
            Class<?> networkUtilsClass = Class.forName("lightning.utils.minecraft.NetworkUtils");
            Method sendSequencedMethod = networkUtilsClass.getMethod("sendSequencedPacket", creatorClass);
            if (this.clickRotate.getValue()) {
                float[] rotations = MultiStackXPModule.keyActionFeetRotation(player);
                yaw = rotations[0];
                pitch = rotations[1];
                MultiStackXPModule.silentRotateLikeKeyAction(yaw, pitch);
            }
            while (this.remaining > 0) {
                boolean switched = false;
                try {
                    if (false && !(switched = MultiStackXPModule.switchSlot(this.runMode, this.xpSlot, this.oldSlot))) {
                        throw new IllegalStateException("Could not AltSwap XP bottle for this packet.");
                    }
                    Object creator = MultiStackXPModule.packetCreator(creatorClass, yaw, pitch);
                    sendSequencedMethod.invoke(null, creator);
                    --this.remaining;
                }
                finally {
                    if (!switched) continue;
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

    private static int findItemSlot(Object inventory, Object targetItem, int maxSlot) throws Exception {
        for (int i = maxSlot; i >= 0; --i) {
            Object stack = MultiStackXPModule.intArg(inventory, "getItem", i);
            Object item = MultiStackXPModule.noArgs(stack, "getItem");
            if (item != targetItem) continue;
            return i;
        }
        return -1;
    }

    private static boolean switchSlot(String mode, int slot, int oldSlot) throws Exception {
        Class<?> invUtilsClass = Class.forName("lightning.utils.minecraft.InventoryUtils");
        Method method = invUtilsClass.getMethod("switchSlot", String.class, Integer.TYPE, Integer.TYPE);
        return Boolean.TRUE.equals(method.invoke(null, mode, slot, oldSlot));
    }

    private static void switchBack(String mode, int slot, int oldSlot) throws Exception {
        Class<?> invUtilsClass = Class.forName("lightning.utils.minecraft.InventoryUtils");
        Method method = invUtilsClass.getMethod("switchBack", String.class, Integer.TYPE, Integer.TYPE);
        method.invoke(null, mode, slot, oldSlot);
    }

    private static float[] keyActionFeetRotation(Object player) throws Exception {
        Object rotManager;
        Object rotation;
        Object yawObj;
        float yaw = ((Number)MultiStackXPModule.noArgs(player, "getYRot")).floatValue();
        try {
            Object moduleManager = MultiStackXPModule.staticField("lightning.Night", "MODULE_MANAGER");
            if (moduleManager != null) {
                Object pendingYaw;
                Class<?> sprintClass = Class.forName("lightning.modules.impl.movement.SprintModule");
                Method getModuleMethod = moduleManager.getClass().getMethod("getModule", Class.class);
                Object sprintModule = getModuleMethod.invoke(moduleManager, sprintClass);
                if (sprintModule != null && Boolean.TRUE.equals(MultiStackXPModule.noArgs(sprintModule, "isToggled")) && Boolean.TRUE.equals(MultiStackXPModule.noArgs(sprintModule, "isGrimCompensating")) && (pendingYaw = MultiStackXPModule.noArgs(sprintModule, "getPendingYaw")) instanceof Float) {
                    yaw = ((Float)pendingYaw).floatValue();
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            rotManager = MultiStackXPModule.staticField("lightning.Night", "ROTATION_MANAGER");
            if (rotManager != null && (rotation = MultiStackXPModule.noArgs(rotManager, "getRotation")) != null && (yawObj = MultiStackXPModule.noArgs(rotation, "getYaw")) instanceof Number) {
                yaw = ((Number)yawObj).floatValue();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return new float[]{yaw, 90.0f};
    }

    private static void silentRotateLikeKeyAction(float yaw, float pitch) {
        try {
            Object rotManager = MultiStackXPModule.staticField("lightning.Night", "ROTATION_MANAGER");
            if (rotManager != null) {
                Method method = rotManager.getClass().getMethod("silentRotate", Float.TYPE, Float.TYPE);
                method.invoke(rotManager, Float.valueOf(yaw), Float.valueOf(pitch));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Object packetCreator(Class<?> creatorClass, float yaw, float pitch) {
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            String methodName = method.getName();
            if ("predict".equals(methodName) && args != null && args.length == 1) {
                return MultiStackXPModule.useItemPacket(((Number)args[0]).intValue(), yaw, pitch);
            }
            if ("toString".equals(methodName)) {
                return "MultiStack XP AltSwap packet creator";
            }
            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(methodName)) {
                return proxy == (args == null || args.length == 0 ? null : args[0]);
            }
            return null;
        };
        return Proxy.newProxyInstance(creatorClass.getClassLoader(), new Class[]{creatorClass}, invocationHandler);
    }

    private static Object useItemPacket(int sequence, float yaw, float pitch) throws Exception {
        Class<?> handClass = Class.forName("net.minecraft.world.InteractionHand");
        Enum mainHand = Enum.valueOf(handClass.asSubclass(Enum.class), "MAIN_HAND");
        Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ServerboundUseItemPacket");
        Constructor<?> constructor = packetClass.getConstructor(handClass, Integer.TYPE, Float.TYPE, Float.TYPE);
        return constructor.newInstance(mainHand, sequence, Float.valueOf(yaw), Float.valueOf(pitch));
    }

    private void finish(boolean shouldSwitchBack) {
        try {
            if (shouldSwitchBack && this.didSwitch && this.xpSlot >= 0 && this.oldSlot >= 0) {
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

    private static Object staticField(String className, String fieldName) throws Exception {
        return Class.forName(className).getField(fieldName).get(null);
    }

    private static Object field(Object target, String fieldName) throws Exception {
        for (Class<?> clazz = target.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            }
            catch (NoSuchFieldException noSuchFieldException) {
                continue;
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static Object noArgs(Object target, String methodName) throws Exception {
        Method method = MultiStackXPModule.findMethod(target.getClass(), methodName, 0, null);
        method.setAccessible(true);
        return method.invoke(target, new Object[0]);
    }

    private static Object intArg(Object target, String methodName, int arg) throws Exception {
        Method method = MultiStackXPModule.findMethod(target.getClass(), methodName, 1, Integer.TYPE);
        method.setAccessible(true);
        return method.invoke(target, arg);
    }

    private static Method findMethod(Class<?> targetClass, String methodName, int paramCount, Class<?> paramType) throws NoSuchMethodException {
        for (Class<?> clazz = targetClass; clazz != null; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() != paramCount || paramCount != 0 && method.getParameterTypes()[0] != paramType) continue;
                return method;
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    private void notifyFailure(Throwable throwable) {
        Throwable rootCause;
        if (this.errorReported) {
            return;
        }
        this.errorReported = true;
        for (rootCause = throwable; rootCause.getCause() != null && rootCause.getCause() != rootCause; rootCause = rootCause.getCause()) {
        }
        String suffix = rootCause.getMessage() == null ? "" : ": " + rootCause.getMessage();
        this.notifyUser(rootCause.getClass().getSimpleName() + suffix);
    }

    private void notifyUser(String message) {
        try {
            Object chatManager = MultiStackXPModule.staticField("lightning.Night", "CHAT_MANAGER");
            Method method = chatManager.getClass().getMethod("tagged", String.class, String.class);
            method.invoke(chatManager, message, this.getName());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

