/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.phys.Vec3
 */
package lightning.modules.impl.miscellaneous;

import java.util.concurrent.ConcurrentLinkedQueue;
import lightning.Night;
import lightning.events.SubscribeEvent;
import lightning.events.impl.BreakBlockEvent;
import lightning.events.impl.ChatInputEvent;
import lightning.events.impl.ConsumeItemEvent;
import lightning.events.impl.PacketSendEvent;
import lightning.events.impl.TickEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import lightning.settings.impl.BooleanSetting;
import lightning.settings.impl.ModeSetting;
import lightning.settings.impl.NumberSetting;
import lightning.settings.impl.StringSetting;
import lightning.utils.system.MathUtils;
import lightning.utils.system.Timer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name="Announcer", description="Announces your actions in chat.", category=Module.Category.MISCELLANEOUS)
public class AnnouncerModule
extends Module {
    public StringSetting watermark = new StringSetting("Watermark", "The client name that will be used in the announcer.", "Lightning");
    public ModeSetting language = new ModeSetting("Language", "The language that will be used for the announcer.", "English", new String[]{"English", "German", "French", "Japanese", "Finnish", "Russian", "Spanish", "Swedish", "Turkish", "Dutch", "Greek", "Chinese", "Italian", "Norwegian", "Romanian", "Czech", "Portuguese", "Slovenian", "Polish", "Korean", "Lithuanian", "Indonesian", "Hungarian", "Random"});
    public NumberSetting delay = new NumberSetting("Delay", "The delay for the announcer.", 5, 0, 30);
    public BooleanSetting clientside = new BooleanSetting("Clientside", "Sends the messages only on your side.", false);
    public BooleanSetting greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
    public BooleanSetting distance = new BooleanSetting("Distance", "Announces your distance travelled in chat.", true);
    public BooleanSetting blocksMined = new BooleanSetting("BlocksMined", "Announces blocks mined in chat.", true);
    public BooleanSetting blocksPlaced = new BooleanSetting("BlocksPlaced", "Announces blocks placed in chat.", true);
    public BooleanSetting eating = new BooleanSetting("Eating", "Announces when you eat in chat.", true);
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue();
    private final Timer messageTimer = new Timer();
    private final Timer distanceTimer = new Timer();
    private Vec3 lastPos;
    private int mined;
    private int placed;
    private int eaten;

    @Override
    public void onEnable() {
        this.queue.clear();
        this.messageTimer.reset();
        this.distanceTimer.reset();
        this.lastPos = null;
        this.mined = 0;
        this.placed = 0;
        this.eaten = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent tickEvent) {
        if (this.getNull()) {
            return;
        }
        if (this.messageTimer.hasTimeElapsed(this.delay.getValue().intValue() * 1000) && !this.queue.isEmpty()) {
            String string = this.queue.poll();
            if (this.clientside.getValue()) {
                Night.CHAT_MANAGER.message(string);
            } else {
                AnnouncerModule.mc.player.connection.sendChat((this.greenText.getValue() ? "> " : "") + string);
            }
            this.messageTimer.reset();
        }
        if (this.lastPos == null) {
            this.lastPos = new Vec3(AnnouncerModule.mc.player.xo, AnnouncerModule.mc.player.yo, AnnouncerModule.mc.player.zo);
        }
        double d = Math.abs(this.lastPos.x - AnnouncerModule.mc.player.xo) + Math.abs(this.lastPos.y - AnnouncerModule.mc.player.yo) + Math.abs(this.lastPos.z - AnnouncerModule.mc.player.zo);
        if (this.distance.getValue() && d > 1.0 && this.distanceTimer.hasTimeElapsed(10000) && this.queue.size() <= 5) {
            this.queue.add(this.getDistanceMessage("" + MathUtils.round(d, 1)));
            this.lastPos = new Vec3(AnnouncerModule.mc.player.xo, AnnouncerModule.mc.player.yo, AnnouncerModule.mc.player.zo);
            this.distanceTimer.reset();
        }
    }

    @SubscribeEvent
    public void onBreakBlock(BreakBlockEvent breakBlockEvent) {
        if (this.getNull()) {
            return;
        }
        ++this.mined;
        if (this.blocksMined.getValue() && (double)this.mined >= MathUtils.random(6.0, 1.0) && this.queue.size() <= 5) {
            this.queue.add(this.getMineMessage("" + this.mined));
            this.mined = 0;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketSendEvent packetSendEvent) {
        ServerboundUseItemOnPacket serverboundUseItemOnPacket;
        if (this.getNull()) {
            return;
        }
        Packet<?> packet = packetSendEvent.getPacket();
        if (packet instanceof ServerboundUseItemOnPacket && AnnouncerModule.mc.player.getItemInHand((serverboundUseItemOnPacket = (ServerboundUseItemOnPacket)packet).getHand()).getItem() instanceof BlockItem) {
            ++this.placed;
            if (this.blocksPlaced.getValue() && (double)this.placed >= MathUtils.random(6.0, 1.0) && this.queue.size() <= 5) {
                this.queue.add(this.getPlaceMessage("" + this.placed));
                this.placed = 0;
            }
        }
    }

    @SubscribeEvent
    public void onConsumeItem(ConsumeItemEvent consumeItemEvent) {
        if (this.getNull() || consumeItemEvent.getStack().get(DataComponents.FOOD) == null) {
            return;
        }
        ++this.eaten;
        if (this.eating.getValue() && (double)this.eaten >= MathUtils.random(6.0, 1.0) && this.queue.size() <= 5) {
            this.queue.add(this.getEatMessage(this.eaten + " " + consumeItemEvent.getStack().getHoverName().getString()));
            this.eaten = 0;
        }
    }

    @SubscribeEvent
    public void onChatInput(ChatInputEvent chatInputEvent) {
        if (this.getNull()) {
            return;
        }
        this.messageTimer.reset();
    }

    private String getDistanceMessage(String string) {
        String[] stringArray = new String[]{"I just flew " + string + " meters thanks to " + this.watermark.getValue() + "!", "Ich bin gerade " + string + " Meter weit geflogen, dank " + this.watermark.getValue() + "!", "Je viens de voler " + string + " m\u00e8tres gr\u00e2ce \u00e0 " + this.watermark.getValue() + "!", this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + string + "\u30e1\u30fc\u30c8\u30eb\u98db\u3093\u3060\u3088\uff01", "Lensin juuri " + string + " metri\u00e4 " + this.watermark.getValue() + " ansiosta!", "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u043f\u0440\u043e\u043b\u0435\u0442\u0435\u043b " + string + " \u043c\u0435\u0442\u0440\u043e\u0432 \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f " + this.watermark.getValue() + "!", "Acabo de volar " + string + " metros gracias a " + this.watermark.getValue() + "!", "Jag fl\u00f6g just " + string + " meter tack vare " + this.watermark.getValue() + "!", this.watermark.getValue() + " sayesinde " + string + " metre u\u00e7tum!", "Ik heb net " + string + " meter gevlogen dankzij " + this.watermark.getValue() + "!", "\u039c\u03cc\u03bb\u03b9\u03c2 \u03c0\u03ad\u03c4\u03b1\u03be\u03b1 " + string + " \u03bc\u03ad\u03c4\u03c1\u03b1 \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03b7\u03bd " + this.watermark.getValue() + "!", "\u6211\u521a\u521a\u98de\u4e86" + string + "\u7c73\uff0c\u591a\u4e8f\u4e86" + this.watermark.getValue() + "!", "Ho appena volato per " + string + " metri grazie ad " + this.watermark.getValue() + "!", "Jeg fl\u00f8y nettopp " + string + " meter takket v\u00e6re " + this.watermark.getValue() + "!", "Tocmai am zburat " + string + " de metri datorit\u0103 lui " + this.watermark.getValue() + "!", "D\u00edky " + this.watermark.getValue() + " jsem pr\u00e1v\u011b ulet\u011bl " + string + " metr\u016f!", "Acabei de voar " + string + " metros gra\u00e7as ao " + this.watermark.getValue() + "!", "Z " + this.watermark.getValue() + " sem pravkar preletel " + string + " metrov!", "W\u0142a\u015bnie przelecia\u0142em " + string + " metr\u00f3w dzi\u0119ki " + this.watermark.getValue() + "!", this.watermark.getValue() + " \ub355\ubd84\uc5d0 \ubc29\uae08 " + string + "\ub97c \ube44\ud589\ud588\uc2b5\ub2c8\ub2e4!", this.watermark.getValue() + " d\u0117ka k\u0105 tik nuskridau " + string + " metrus!", "Saya baru saja terbang sejauh " + string + " meter berkat " + this.watermark.getValue() + "!", string + " m\u00e9tert rep\u00fcltem az " + this.watermark.getValue() + " k\u00f6sz\u00f6nhet\u0151en!"};
        return stringArray[this.getLanguageIndex()];
    }

    private String getMineMessage(String string) {
        String[] stringArray = new String[]{"I just mined " + string + " blocks thanks to " + this.watermark.getValue() + "!", "Ich habe gerade " + string + " Bl\u00f6cke abgebaut, dank " + this.watermark.getValue() + "!", "Je viens d'extraire " + string + " blocs gr\u00e2ce \u00e0 " + this.watermark.getValue() + "!", this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + string + "\u30d6\u30ed\u30c3\u30af\u63a1\u6398\u3057\u305f\u3068\u3053\u308d\u3067\u3059\uff01", "Louhin juuri " + string + " lohkoa " + this.watermark.getValue() + " ansiosta!", "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u0434\u043e\u0431\u044b\u043b " + string + " \u0431\u043b\u043e\u043a\u043e\u0432 \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f " + this.watermark.getValue() + "!", "\u00a1Acabo de minar " + string + " bloques gracias a " + this.watermark.getValue() + "!", "Jag har precis tagit fram " + string + " block tack vare " + this.watermark.getValue() + "!", this.watermark.getValue() + " sayesinde az \u00f6nce " + string + " blok kazd\u0131m!", "Ik heb net " + string + " blokken gedolven dankzij " + this.watermark.getValue() + "!", "\u039c\u03cc\u03bb\u03b9\u03c2 \u03b5\u03be\u03cc\u03c1\u03c5\u03be\u03b1 " + string + " \u03bc\u03c0\u03bb\u03bf\u03ba \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03b7\u03bd " + this.watermark.getValue() + "!", "\u6211\u521a\u521a\u5f00\u91c7\u4e86" + string + "\u4e2a\u533a\u5757\uff0c\u611f\u8c22" + this.watermark.getValue() + "!", "Ho appena estratto " + string + " blocchi grazie ad " + this.watermark.getValue() + "!", "Jeg har nettopp utvunnet " + string + " blokker takket v\u00e6re " + this.watermark.getValue() + "!", "Tocmai am minat " + string + " de blocuri datorit\u0103 lui " + this.watermark.getValue() + "!", "Pr\u00e1v\u011b jsem vyt\u011b\u017eil " + string + " blok\u016f d\u00edky " + this.watermark.getValue() + "!", "Acabei de extrair " + string + " blocos gra\u00e7as ao " + this.watermark.getValue() + "!", "Zahvaljujo\u010d " + this.watermark.getValue() + " sem pravkar izkopal " + string + " blokov!", "W\u0142a\u015bnie wydoby\u0142em " + string + " blok\u00f3w dzi\u0119ki " + this.watermark.getValue() + "!", "\ubc29\uae08 " + this.watermark.getValue() + " \ub355\ubd84\uc5d0 " + string + " \ube14\ub85d\uc744 \ucc44\uad74\ud588\uc2b5\ub2c8\ub2e4!", this.watermark.getValue() + " d\u0117ka k\u0105 tik i\u0161kasiau " + string + " blokus!", "Saya baru saja menambang " + string + " blok berkat " + this.watermark.getValue() + "!", "Most b\u00e1ny\u00e1sztam " + string + " blokkot az " + this.watermark.getValue() + " k\u00f6sz\u00f6nhet\u0151en!"};
        return stringArray[this.getLanguageIndex()];
    }

    private String getPlaceMessage(String string) {
        String[] stringArray = new String[]{"I just placed " + string + " blocks thanks to " + this.watermark.getValue() + "!", "Ich habe gerade " + string + " Bl\u00f6cke dank " + this.watermark.getValue() + " platziert!", "Je viens de placer " + string + " blocs gr\u00e2ce \u00e0 " + this.watermark.getValue() + "!", this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + string + "\u500b\u306e\u30d6\u30ed\u30c3\u30af\u3092\u7f6e\u3044\u305f\u3068\u3053\u308d\u3060!", "Sijoitin juuri " + string + " lohkoa " + this.watermark.getValue() + " ansiosta!", "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u0440\u0430\u0437\u043c\u0435\u0441\u0442\u0438\u043b " + string + " \u0431\u043b\u043e\u043a\u043e\u0432 \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f " + this.watermark.getValue() + "!", "\u00a1Acabo de colocar " + string + " bloques gracias a " + this.watermark.getValue() + "!", "Jag har precis placerat " + string + " block tack vare " + this.watermark.getValue() + "!", "Az \u00f6nce " + this.watermark.getValue() + " sayesinde " + string + " blok yerle\u015ftirdim!", "Ik heb net " + string + " blokken geplaatst dankzij " + this.watermark.getValue() + "!", "\u039c\u03cc\u03bb\u03b9\u03c2 \u03c4\u03bf\u03c0\u03bf\u03b8\u03ad\u03c4\u03b7\u03c3\u03b1 " + string + " \u03bc\u03c0\u03bb\u03bf\u03ba \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03bf " + this.watermark.getValue() + "!", "\u591a\u4e8f\u4e86 " + this.watermark.getValue() + "\uff0c\u6211\u521a\u521a\u653e\u4e86 " + string + " \u5757!", "Ho appena piazzato " + string + " blocchi grazie a " + this.watermark.getValue() + "!", "Jeg har nettopp plassert " + string + " blokker takket v\u00e6re " + this.watermark.getValue() + "!", "Tocmai am plasat " + string + " blocuri datorit\u0103 lui " + this.watermark.getValue() + "!", "Pr\u00e1v\u011b jsem um\u00edstil " + string + " blok\u016f d\u00edky " + this.watermark.getValue() + "!", "Acabei de colocar " + string + " blocos gra\u00e7as ao " + this.watermark.getValue() + "!", "Pravkar sem postavil " + string + " blokov zahvaljujo\u010d " + this.watermark.getValue() + "!", "W\u0142a\u015bnie umie\u015bci\u0142em " + string + " blok\u00f3w dzi\u0119ki " + this.watermark.getValue() + "!", this.watermark.getValue() + " \ub355\ubd84\uc5d0 \ubc29\uae08 XXX \ube14\ub85d\uc744 \ubc30\uce58\ud588\uc2b5\ub2c8\ub2e4!", "K\u0105 tik \u012fd\u0117jau " + string + " blok\u0173 d\u0117ka " + this.watermark.getValue() + "!", "Saya baru saja menempatkan blok " + string + " berkat " + this.watermark.getValue() + "!", "Most helyeztem el " + string + " blokkot a " + this.watermark.getValue() + "-nek k\u00f6sz\u00f6nhet\u0151en!"};
        return stringArray[this.getLanguageIndex()];
    }

    private String getEatMessage(String string) {
        String[] stringArray = new String[]{"I just ate " + string + " thanks to " + this.watermark.getValue() + "!", "Ich habe gerade " + string + " gegessen, dank " + this.watermark.getValue() + "!", "Je viens de manger " + string + " gr\u00e2ce \u00e0 " + this.watermark.getValue() + " !", this.watermark.getValue() + "\u306e\u304a\u304b\u3052\u3067" + string + "\u3092\u98df\u3079\u305f\u3088!", "S\u00f6in juuri " + string + " kiitos " + this.watermark.getValue() + "!", "\u042f \u0442\u043e\u043b\u044c\u043a\u043e \u0447\u0442\u043e \u0441\u044a\u0435\u043b " + string + " \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u044f " + this.watermark.getValue() + "!", "\u00a1Acabo de comer " + string + " gracias a " + this.watermark.getValue() + "!", "Jag \u00e5t just " + string + " tack vare " + this.watermark.getValue() + "!", "Az \u00f6nce " + this.watermark.getValue() + " sayesinde " + string + " yedim!", "Ik heb net " + string + " gegeten dankzij " + this.watermark.getValue() + "!", "\u039c\u03cc\u03bb\u03b9\u03c2 \u03ad\u03c6\u03b1\u03b3\u03b1 " + string + " \u03c7\u03ac\u03c1\u03b7 \u03c3\u03c4\u03bf " + this.watermark.getValue() + "!", "\u6211\u521a\u5403\u4e86 " + string + "\uff0c\u591a\u4e8f\u4e86 " + this.watermark.getValue() + "!", "Ho appena mangiato " + string + " grazie a " + this.watermark.getValue() + "!", "Jeg spiste nettopp " + string + " takket v\u00e6re " + this.watermark.getValue() + "!", "Pr\u00e1v\u011b jsem sn\u011bdl " + string + " d\u00edky " + this.watermark.getValue() + "!", "Acabei de comer " + string + " gra\u00e7as a " + this.watermark.getValue() + "!", "Pravkar sem pojedel " + string + " zaradi " + this.watermark.getValue() + "!", "W\u0142a\u015bnie zjad\u0142em " + string + " dzi\u0119ki " + this.watermark.getValue() + "!", "\ubc29\uae08 " + this.watermark.getValue() + " \ub355\ubd84\uc5d0 " + string + "\ub97c \uba39\uc5c8\uc5b4\uc694!", "K\u0105 tik suvalgiau " + string + " d\u0117l " + this.watermark.getValue() + "!", "Saya baru saja makan " + string + " berkat " + this.watermark.getValue() + "!", "Most ettem " + string + "-t, h\u00e1la " + this.watermark.getValue() + "-nek!"};
        return stringArray[this.getLanguageIndex()];
    }

    private int getLanguageIndex() {
        return switch (this.language.getValue()) {
            case "German" -> 1;
            case "French" -> 2;
            case "Japanese" -> 3;
            case "Finnish" -> 4;
            case "Russian" -> 5;
            case "Spanish" -> 6;
            case "Swedish" -> 7;
            case "Turkish" -> 8;
            case "Dutch" -> 9;
            case "Greek" -> 10;
            case "Chinese" -> 11;
            case "Italian" -> 12;
            case "Norwegian" -> 13;
            case "Romanian" -> 14;
            case "Czech" -> 15;
            case "Portuguese" -> 16;
            case "Slovenian" -> 17;
            case "Polish" -> 18;
            case "Korean" -> 19;
            case "Lithuanian" -> 20;
            case "Indonesian" -> 21;
            case "Hungarian" -> 22;
            case "Random" -> (int)MathUtils.random(22.0, 0.0);
            default -> 0;
        };
    }
}

