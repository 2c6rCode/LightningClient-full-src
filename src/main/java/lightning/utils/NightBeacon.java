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

    public static void pre(Object connection, Object packet, Object sendListener, boolean flush, Object callbackInfo) {
        if (!ENABLED || broken || packet == null) {
            return;
        }
        try {
            if (!"ServerboundClientInformationPacket".equals(packet.getClass().getSimpleName())) {
                return;
            }
            Object rewrittenPacket = NightBeacon.rewrite(packet);
            if (rewrittenPacket == null || rewrittenPacket == packet) {
                return;
            }
            if (sendMethod == null) {
                block2: for (Class<?> clazz = connection.getClass(); clazz != null && sendMethod == null; clazz = clazz.getSuperclass()) {
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
            sendMethod.invoke(connection, rewrittenPacket, sendListener, flush);
            callbackInfo.getClass().getMethod("cancel", new Class[0]).invoke(callbackInfo, new Object[0]);
        }
        catch (Throwable throwable) {
            broken = true;
        }
    }

    private static Object rewrite(Object packet) throws Exception {
        Class<?> clazz = packet.getClass();
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
        Object clientInfo = pktGetter.invoke(packet, new Object[0]);
        java.lang.reflect.RecordComponent[] dataComponents = clientInfo.getClass().getRecordComponents();
        Object[] componentValues = new Object[dataComponents.length];
        for (int i = 0; i < dataComponents.length; ++i) {
            Method method = dataComponents[i].getAccessor();
            method.setAccessible(true);
            componentValues[i] = method.invoke(clientInfo, new Object[0]);
        }
        int modelCustom = (Integer)componentValues[modelIdx];
        if ((modelCustom & 0x80) != 0) {
            return packet;
        }
        componentValues[NightBeacon.modelIdx] = modelCustom | 0x80;
        return pktCtor.newInstance(infoCtor.newInstance(componentValues));
    }

    public static boolean or(Object entity, boolean original) {
        return original || NightBeacon.isMarked(entity);
    }

    public static boolean isMarked(Object entity) {
        if (!ENABLED || readBroken || entity == null) {
            return false;
        }
        try {
            if (!inited) {
                block2: for (Class<?> clazz = entity.getClass(); clazz != null && accessorField == null; clazz = clazz.getSuperclass()) {
                    for (Field field : clazz.getDeclaredFields()) {
                        if (!Modifier.isStatic(field.getModifiers()) || !field.getName().equals("DATA_PLAYER_MODE_CUSTOMISATION")) continue;
                        field.setAccessible(true);
                        accessorField = field;
                        continue block2;
                    }
                }
                block4: for (Class<?> clazz = entity.getClass(); clazz != null && getEntityData == null; clazz = clazz.getSuperclass()) {
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (!method.getName().equals("getEntityData") || method.getParameterCount() != 0) continue;
                        method.setAccessible(true);
                        getEntityData = method;
                        continue block4;
                    }
                }
                inited = true;
                if (accessorField == null || getEntityData == null) {
                    readBroken = true;
                    return false;
                }
            }
            Object entityDataObj = getEntityData.invoke(entity, new Object[0]);
            if (dataGet == null) {
                for (Method method : entityDataObj.getClass().getMethods()) {
                    if (!method.getName().equals("get") || method.getParameterCount() != 1) continue;
                    dataGet = method;
                    break;
                }
                if (dataGet == null) {
                    readBroken = true;
                    return false;
                }
            }
            Object rawValue = dataGet.invoke(entityDataObj, accessorField.get(null));
            return rawValue instanceof Byte && ((Byte)rawValue & 0x80) != 0;
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

