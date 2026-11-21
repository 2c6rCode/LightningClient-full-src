/*
 * Decompiled with CFR 0.152.
 */
package lightning.managers;

import java.util.ArrayList;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.TickEvent;
import lightning.utils.IMinecraft;

public class TaskManager
implements IMinecraft {
    private final ArrayList<Runnable> tasks = new ArrayList();

    public TaskManager() {
        Night.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (!this.tasks.isEmpty()) {
            this.tasks.getFirst().run();
            this.tasks.removeFirst();
        }
    }

    public void submit(Runnable runnable) {
        this.tasks.add(runnable);
    }
}

