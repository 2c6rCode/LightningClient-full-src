/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lombok.Generated;

public class Listener {
    private static Method lookupMethod;
    private final Class<?> subscriber;
    private Consumer<Object> consumer;
    private final int priority;

    public Listener(Class<?> targetClass, Object target, Method method) {
        this.subscriber = method.getParameters()[0].getType();
        this.priority = method.getAnnotation(SubscribeEvent.class).priority();
        try {
            MethodType methodType = MethodType.methodType(Void.TYPE, method.getParameters()[0].getType());
            MethodHandle methodHandle = MethodHandles.lookup().findVirtual(targetClass, method.getName(), methodType);
            MethodType factoryType = MethodType.methodType(Consumer.class, targetClass);
            this.consumer = (Consumer<Object>) LambdaMetafactory.metafactory(MethodHandles.lookup(), "accept", factoryType, MethodType.methodType(Void.TYPE, Object.class), methodHandle, methodType).getTarget().invoke(target);
        }
        catch (Throwable throwable) {
            Night.LOGGER.error("Fast event dispatch failed for " + targetClass.getName() + "." + method.getName() + ", falling back to reflection", throwable);
            if (!method.canAccess(target)) {
                method.setAccessible(true);
            }
            this.consumer = new ReflectiveFallbackConsumer(method, target);
        }
    }

    public void invoke(Object event) {
        this.consumer.accept(event);
    }

    @Generated
    public Class<?> getSubscriber() {
        return this.subscriber;
    }

    @Generated
    public Consumer<Object> getConsumer() {
        return this.consumer;
    }

    @Generated
    public int getPriority() {
        return this.priority;
    }

    private static final class ReflectiveFallbackConsumer
    implements Consumer<Object> {
        private final Method method;
        private final Object target;

        private ReflectiveFallbackConsumer(Method method, Object target) {
            this.method = method;
            this.target = target;
        }

        @Override
        public void accept(Object event) {
            try {
                this.method.invoke(this.target, event);
            }
            catch (Exception exception) {
                Night.LOGGER.error("The Event System threw an exception!", (Throwable)exception);
            }
        }
    }
}

