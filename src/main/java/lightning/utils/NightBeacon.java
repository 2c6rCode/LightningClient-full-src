/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;

public final class NightBeacon {
    public static final int MARK = 128;
    private static final boolean ENABLED = !"false".equalsIgnoreCase(System.getProperty("night.beacon", "true"));
    private static volatile boolean broken = false;
    private static Constructor<?> pktCtor;
    private static Constructor<?> infoCtor;
    private static Method pktGetter;
    private static int modelIdx;
    private static Method sendMethod;
    private static Field accessorField;
    private static Method getEntityData;
    private static Method dataGet;
    private static boolean inited;
    private static boolean readBroken;

    private NightBeacon() {
    }

    public static void pre(Object object, Object object2, Object object3, boolean bl, Object object4) {
        if (!ENABLED || broken || object2 == null) {
            return;
        }
        try {
            if (!"ServerboundClientInformationPacket".equals(object2.getClass().getSimpleName())) {
                return;
            }
            Object object5 = NightBeacon.rewrite(object2);
            if (object5 == null || object5 == object2) {
                return;
            }
            if (sendMethod == null) {
                block2: for (Class<?> clazz = object.getClass(); clazz != null && sendMethod == null; clazz = clazz.getSuperclass()) {
                    for (Method method : clazz.getDeclaredMethods()) {
                        Class<?>[] classArray = method.getParameterTypes();
                        if (!method.getName().equals("send") || classArray.length != 3 || classArray[2] != Boolean.TYPE) continue;
                        method.setAccessible(true);
                        sendMethod = method;
                        continue block2;
                    }
                }
            }
            if (sendMethod == null) {
                return;
            }
            sendMethod.invoke(object, object5, object3, bl);
            object4.getClass().getMethod("cancel", new Class[0]).invoke(object4, new Object[0]);
        }
        catch (Throwable throwable) {
            broken = true;
        }
    }

    private static Object rewrite(Object object) throws Exception {
        int n;
        Object[] objectArray;
        Object object2;
        Object object3;
        Class<?> clazz = object.getClass();
        if (pktCtor == null) {
            java.lang.reflect.RecordComponent[] recComponents = clazz.getRecordComponents();
            if (recComponents == null || recComponents.length != 1) {
                throw new IllegalStateException();
            }
            pktGetter = recComponents[0].getAccessor();
            pktGetter.setAccessible(true);
            pktCtor = clazz.getDeclaredConstructor(recComponents[0].getType());
            pktCtor.setAccessible(true);
            Class<?> infoClass = recComponents[0].getType();
            java.lang.reflect.RecordComponent[] infoComponents = infoClass.getRecordComponents();
            Class[] classArray = new Class[infoComponents.length];
            for (int i = 0; i < infoComponents.length; ++i) {
                classArray[i] = infoComponents[i].getType();
                if (!infoComponents[i].getName().toLowerCase().contains("modelcustom")) continue;
                modelIdx = i;
            }
            if (modelIdx < 0 || classArray[modelIdx] != Integer.TYPE) {
                throw new IllegalStateException();
            }
            infoCtor = infoClass.getDeclaredConstructor(classArray);
            infoCtor.setAccessible(true);
        }
        object3 = pktGetter.invoke(object, new Object[0]);
        java.lang.reflect.RecordComponent[] dataComponents = object3.getClass().getRecordComponents();
        objectArray = new Object[dataComponents.length];
        for (n = 0; n < dataComponents.length; ++n) {
            Method method = dataComponents[n].getAccessor();
            method.setAccessible(true);
            objectArray[n] = method.invoke(object3, new Object[0]);
        }
        n = (Integer)objectArray[modelIdx];
        if ((n & 0x80) != 0) {
            return object;
        }
        objectArray[NightBeacon.modelIdx] = n | 0x80;
        return pktCtor.newInstance(infoCtor.newInstance(objectArray));
    }

    public static boolean or(Object object, boolean bl) {
        return bl || NightBeacon.isMarked(object);
    }

    public static boolean isMarked(Object object) {
        if (!ENABLED || readBroken || object == null) {
            return false;
        }
        try {
            Byte by;
            AccessibleObject accessibleObject;
            int n;
            int n2;
            Object object2;
            Class<?> clazz;
            if (!inited) {
                block2: for (clazz = object.getClass(); clazz != null && accessorField == null; clazz = clazz.getSuperclass()) {
                    Field[] declaredFields = clazz.getDeclaredFields();
                    n2 = declaredFields.length;
                    for (n = 0; n < n2; ++n) {
                        Field f = declaredFields[n];
                        if (!Modifier.isStatic(f.getModifiers()) || !f.getName().equals("DATA_PLAYER_MODE_CUSTOMISATION")) continue;
                        f.setAccessible(true);
                        accessorField = f;
                        continue block2;
                    }
                }
                block4: for (clazz = object.getClass(); clazz != null && getEntityData == null; clazz = clazz.getSuperclass()) {
                    Method[] declaredMethods = clazz.getDeclaredMethods();
                    n2 = declaredMethods.length;
                    for (n = 0; n < n2; ++n) {
                        Method m = declaredMethods[n];
                        if (!m.getName().equals("getEntityData") || m.getParameterCount() != 0) continue;
                        m.setAccessible(true);
                        getEntityData = m;
                        continue block4;
                    }
                }
                inited = true;
                if (accessorField == null || getEntityData == null) {
                    readBroken = true;
                    return false;
                }
            }
            Object entityDataObj = getEntityData.invoke(object, new Object[0]);
            if (dataGet == null) {
                Method[] entityDataMethods = entityDataObj.getClass().getMethods();
                n2 = entityDataMethods.length;
                for (n = 0; n < n2; ++n) {
                    Method m = entityDataMethods[n];
                    if (!m.getName().equals("get") || m.getParameterCount() != 1) continue;
                    dataGet = m;
                    break;
                }
                if (dataGet == null) {
                    readBroken = true;
                    return false;
                }
            }
            return (object2 = dataGet.invoke(entityDataObj, accessorField.get(null))) instanceof Byte && ((by = (Byte)object2) & 0x80) != 0;
        }
        catch (Throwable throwable) {
            readBroken = true;
            return false;
        }
    }

    static {
        modelIdx = -1;
    }
}

