/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.player.Player
 */
package lightning.commands.impl;

import java.util.List;
import lightning.Night;
import lightning.commands.Command;
import lightning.commands.RegisterCommand;
import lightning.events.impl.PlayerPopEvent;
import lightning.modules.impl.miscellaneous.FakePlayerModule;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

@RegisterCommand(name="fakeplayer", aliases={"fp"}, description="Spawns a client-side fake player to test on.", syntax="[remove|pop|swim|record|stop|play|<name>]")
public class FakePlayerCommand
extends Command {
    private static final byte TOTEM_POP_EVENT_ID = 35;

    @Override
    public void execute(String[] args) {
        FakePlayerModule fakePlayerModule = Night.MODULE_MANAGER.getModule(FakePlayerModule.class);
        if (fakePlayerModule == null) {
            return;
        }
        if (args.length == 0) {
            fakePlayerModule.setToggled(!fakePlayerModule.isToggled());
            return;
        }
        switch (args[0].toLowerCase()) {
            case "remove": {
                if (fakePlayerModule.isToggled()) {
                    fakePlayerModule.setToggled(false);
                    break;
                }
                Night.CHAT_MANAGER.warn("No fake player spawned.");
                break;
            }
            case "pop": {
                this.popBot(fakePlayerModule);
                break;
            }
            case "swim": {
                this.toggleSwim(fakePlayerModule);
                break;
            }
            case "record": {
                fakePlayerModule.startRecording();
                break;
            }
            case "stop": {
                fakePlayerModule.stopRecording();
                break;
            }
            case "play": {
                fakePlayerModule.startPlaying();
                break;
            }
            default: {
                fakePlayerModule.name.setValue(args[0]);
                if (!fakePlayerModule.isToggled()) {
                    fakePlayerModule.setToggled(true);
                    break;
                }
                fakePlayerModule.setToggled(false);
                fakePlayerModule.setToggled(true);
            }
        }
    }

    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            return List.of("remove", "pop", "swim", "record", "stop", "play");
        }
        return List.of();
    }

    private boolean nullCheck() {
        return FakePlayerCommand.mc.level == null || FakePlayerCommand.mc.player == null;
    }

    private void popBot(FakePlayerModule fakePlayerModule) {
        if (this.nullCheck() || mc.getConnection() == null) {
            Night.CHAT_MANAGER.warn("You need to be in a world to do that.");
            return;
        }
        RemotePlayer remotePlayer = fakePlayerModule.getPlayer();
        if (remotePlayer == null || remotePlayer.level() != FakePlayerCommand.mc.level) {
            Night.CHAT_MANAGER.warn("No fake player spawned. Spawn one first.");
            return;
        }
        mc.getConnection().handleEntityEvent(new ClientboundEntityEventPacket((Entity)remotePlayer, (byte)35));
        Night.EVENT_HANDLER.post(new PlayerPopEvent((Player)remotePlayer, 1));
        Night.CHAT_MANAGER.tagged("Popped fake player.", "FakePlayer");
    }

    private void toggleSwim(FakePlayerModule fakePlayerModule) {
        if (this.nullCheck()) {
            Night.CHAT_MANAGER.warn("You need to be in a world to do that.");
            return;
        }
        RemotePlayer remotePlayer = fakePlayerModule.getPlayer();
        if (remotePlayer == null || remotePlayer.level() != FakePlayerCommand.mc.level) {
            Night.CHAT_MANAGER.warn("No fake player spawned. Spawn one first.");
            return;
        }
        Pose pose = remotePlayer.getPose() != Pose.SWIMMING ? Pose.SWIMMING : Pose.STANDING;
        remotePlayer.setPose(pose);
        remotePlayer.refreshDimensions();
        Night.CHAT_MANAGER.tagged(pose == Pose.SWIMMING ? "Fake player now in swim pose." : "Fake player now standing.", "FakePlayer");
    }
}

