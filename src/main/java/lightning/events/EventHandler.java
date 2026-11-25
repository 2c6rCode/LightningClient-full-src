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

    public void subscribe(Object target) {
        for (Listener listener : this.getListeners(target.getClass(), target)) {
            this.insert(this.listenerMap.computeIfAbsent(listener.getSubscriber(), clazz -> new CopyOnWriteArrayList<>()), listener);
        }
    }

    public void unsubscribe(Object target) {
        for (Listener listener : this.getListeners(target.getClass(), target)) {
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

    public boolean isListening(Class<?> eventClass) {
        List<Listener> list = this.listenerMap.get(eventClass);
        return list != null && !list.isEmpty();
    }

    private List<Listener> getListeners(final Class<?> targetClass, final Object target) {
        Function<Object, List<Listener>> loader = ignored -> {
            CopyOnWriteArrayList<Listener> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
            this.processListeners(copyOnWriteArrayList, targetClass, target);
            return copyOnWriteArrayList;
        };
        if (target == null) {
            return this.staticListeners.computeIfAbsent(targetClass, loader);
        }
        for (Object key : this.listeners.keySet()) {
            if (key != target) continue;
            return this.listeners.get(target);
        }
        List<Listener> resolved = loader.apply(target);
        this.listeners.put(target, resolved);
        return resolved;
    }

    private void processListeners(List<Listener> outListeners, Class<?> targetClass, Object target) {
        for (Method method : targetClass.getDeclaredMethods()) {
            if (!this.isValid(method)) continue;
            outListeners.add(new Listener(targetClass, target, method));
        }
        if (targetClass.getSuperclass() != null) {
            this.processListeners(outListeners, targetClass.getSuperclass(), target);
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
        int index;
        for (index = 0; index < list.size() && listener.getPriority() <= list.get(index).getPriority(); ++index) {
        }
        list.add(index, listener);
    }
}

