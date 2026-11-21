/*
 * Decompiled with CFR 0.152.
 */
package lightning.events;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import lightning.events.Event;
import lightning.events.Listener;
import lightning.events.SubscribeEvent;

public class EventHandler {
    private final Map<Object, List<Listener>> listeners = new ConcurrentHashMap<Object, List<Listener>>();
    private final Map<Class<?>, List<Listener>> staticListeners = new ConcurrentHashMap();
    private final Map<Class<?>, List<Listener>> listenerMap = new ConcurrentHashMap();

    public void subscribe(Object object) {
        for (Listener listener : this.getListeners(object.getClass(), object)) {
            this.insert(this.listenerMap.computeIfAbsent(listener.getSubscriber(), clazz -> new CopyOnWriteArrayList<>()), listener);
        }
    }

    public void unsubscribe(Object object) {
        for (Listener listener : this.getListeners(object.getClass(), object)) {
            List<Listener> list = this.listenerMap.get(listener.getSubscriber());
            if (list == null) continue;
            list.remove(listener);
        }
    }

    public void post(Event event) {
        List<Listener> list = this.listenerMap.get(event.getClass());
        if (list == null) {
            return;
        }
        for (Listener listener : list) {
            listener.invoke(event);
        }
    }

    public boolean isListening(Class<?> clazz) {
        List<Listener> list = this.listenerMap.get(clazz);
        return list != null && !list.isEmpty();
    }

    private List<Listener> getListeners(final Class<?> clazz, final Object object) {
        Function<Object, List<Listener>> function = object2 -> {
            CopyOnWriteArrayList<Listener> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
            this.processListeners(copyOnWriteArrayList, clazz, object);
            return copyOnWriteArrayList;
        };
        if (object == null) {
            return this.staticListeners.computeIfAbsent(clazz, function);
        }
        for (Object object2 : this.listeners.keySet()) {
            if (object2 != object) continue;
            return this.listeners.get(object);
        }
        List list = (List)function.apply(object);
        this.listeners.put(object, list);
        return list;
    }

    private void processListeners(List<Listener> list, Class<?> clazz, Object object) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!this.isValid(method)) continue;
            list.add(new Listener(clazz, object, method));
        }
        if (clazz.getSuperclass() != null) {
            this.processListeners(list, clazz.getSuperclass(), object);
        }
    }

    private boolean isValid(Method method) {
        if (!method.isAnnotationPresent(SubscribeEvent.class)) {
            return false;
        }
        if (method.getReturnType() != Void.TYPE) {
            return false;
        }
        if (method.getParameterCount() != 1) {
            return false;
        }
        return !method.getParameters()[0].getType().isPrimitive();
    }

    private void insert(List<Listener> list, Listener listener) {
        int n;
        for (n = 0; n < list.size() && listener.getPriority() <= list.get(n).getPriority(); ++n) {
        }
        list.add(n, listener);
    }
}

