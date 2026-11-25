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
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Generated
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Event)) {
            return false;
        }
        Event event = (Event)other;
        if (!event.canEqual(this)) {
            return false;
        }
        return this.isCancelled() == event.isCancelled();
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof Event;
    }

    @Generated
    public int hashCode() {
        int prime = 59;
        int result = 1;
        result = result * 59 + (this.isCancelled() ? 79 : 97);
        return result;
    }

    @Generated
    public String toString() {
        return "Event(cancelled=" + this.isCancelled() + ")";
    }
}

