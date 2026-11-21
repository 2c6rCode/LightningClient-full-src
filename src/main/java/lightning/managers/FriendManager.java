/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package lightning.managers;

import java.awt.Color;
import java.util.ArrayList;
import lightning.Night;
import lightning.modules.impl.core.FriendModule;
import lightning.modules.impl.core.PingBypassModule;
import lightning.pingbypass.PingBypassFlags;
import lombok.Generated;

public class FriendManager {
    private final ArrayList<String> friends = new ArrayList();

    public boolean contains(String string) {
        if (this.getFriendFire()) {
            return false;
        }
        return this.friends.stream().anyMatch(string::equalsIgnoreCase);
    }

    public void add(String string) {
        if (this.contains(string)) {
            return;
        }
        this.friends.add(string);
        this.syncToProxy();
    }

    public void remove(String string) {
        this.friends.removeIf(string::equalsIgnoreCase);
        this.syncToProxy();
    }

    public void clear() {
        this.friends.clear();
        this.syncToProxy();
    }

    private void syncToProxy() {
        PingBypassModule pingBypassModule;
        if (PingBypassFlags.proxyForwardingActive && Night.PINGBYPASS_CONFIG != null && !Night.PINGBYPASS_CONFIG.isServer() && (pingBypassModule = Night.MODULE_MANAGER.getModule(PingBypassModule.class)) != null) {
            pingBypassModule.syncFriendsToProxy();
        }
    }

    public boolean getFriendFire() {
        if (Night.MODULE_MANAGER == null) {
            return false;
        }
        FriendModule friendModule = Night.MODULE_MANAGER.getModule(FriendModule.class);
        return friendModule != null && friendModule.friendlyFire != null && friendModule.friendlyFire.getValue();
    }

    public void sendFriendMessage(String string) {
        if (Night.MODULE_MANAGER == null) {
            return;
        }
        FriendModule friendModule = Night.MODULE_MANAGER.getModule(FriendModule.class);
        if (friendModule != null) {
            friendModule.sendFriendMessage(string);
        }
    }

    public Color getDefaultFriendColor() {
        return this.getDefaultFriendColor(255);
    }

    public Color getDefaultFriendColor(int n) {
        return new Color(85, 255, 255, n);
    }

    @Generated
    public ArrayList<String> getFriends() {
        return this.friends;
    }
}

