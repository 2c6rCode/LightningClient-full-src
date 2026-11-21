/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.minecraft;

import java.util.Optional;

public final class BaritoneUtils {
    private BaritoneUtils() {
    }

    public static Object getPrimaryBaritone() {
        try {
            Class<?> clazz = Class.forName("baritone.api.BaritoneAPI");
            Object object = clazz.getMethod("getProvider", new Class[0]).invoke(null, new Object[0]);
            return object.getClass().getMethod("getPrimaryBaritone", new Class[0]).invoke(object, new Object[0]);
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    public static boolean isAvailable() {
        return BaritoneUtils.getPrimaryBaritone() != null;
    }

    public static boolean isActive() {
        Object object;
        Object object2 = BaritoneUtils.getPrimaryBaritone();
        if (object2 == null) {
            return false;
        }
        try {
            object = object2.getClass().getMethod("getPathingBehavior", new Class[0]).invoke(object2, new Object[0]);
            if (((Boolean)object.getClass().getMethod("isPathing", new Class[0]).invoke(object, new Object[0])).booleanValue()) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            object = object2.getClass().getMethod("getCustomGoalProcess", new Class[0]).invoke(object2, new Object[0]);
            if (((Boolean)object.getClass().getMethod("isActive", new Class[0]).invoke(object, new Object[0])).booleanValue()) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            object = object2.getClass().getMethod("getElytraProcess", new Class[0]).invoke(object2, new Object[0]);
            if (object != null && ((Boolean)object.getClass().getMethod("isActive", new Class[0]).invoke(object, new Object[0])).booleanValue()) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            Object object3 = object2.getClass().getMethod("getPathingControlManager", new Class[0]).invoke(object2, new Object[0]);
            Optional optional = (Optional)object3.getClass().getMethod("mostRecentInControl", new Class[0]).invoke(object3, new Object[0]);
            if (optional.isPresent() && ((Boolean)(object = optional.get()).getClass().getMethod("isActive", new Class[0]).invoke(object, new Object[0])).booleanValue()) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }
}

