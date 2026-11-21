/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.events;

import lombok.Generated;

public class Event {
    private boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }

    @Generated
    public Event() {
    }

    @Generated
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Generated
    public void setCancelled(boolean bl) {
        this.cancelled = bl;
    }

    @Generated
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Event)) {
            return false;
        }
        Event event = (Event)object;
        if (!event.canEqual(this)) {
            return false;
        }
        return this.isCancelled() == event.isCancelled();
    }

    @Generated
    protected boolean canEqual(Object object) {
        return object instanceof Event;
    }

    @Generated
    public int hashCode() {
        int n = 59;
        int n2 = 1;
        n2 = n2 * 59 + (this.isCancelled() ? 79 : 97);
        return n2;
    }

    @Generated
    public String toString() {
        return "Event(cancelled=" + this.isCancelled() + ")";
    }
}

