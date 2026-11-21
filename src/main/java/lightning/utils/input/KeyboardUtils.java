/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.Window
 *  net.minecraft.client.Minecraft
 *  org.apache.commons.lang3.StringUtils
 *  org.lwjgl.glfw.GLFW
 */
package lightning.utils.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

public class KeyboardUtils {
    public static String getKeyName(int n) {
        return switch (n) {
            case -1 -> "Unknown";
            case 256 -> "Esc";
            case 96 -> "Grave Accent";
            case 161 -> "World 1";
            case 162 -> "World 2";
            case 283 -> "Print Screen";
            case 284 -> "Pause";
            case 260 -> "Insert";
            case 261 -> "Delete";
            case 268 -> "Home";
            case 266 -> "Page Up";
            case 267 -> "Page Down";
            case 269 -> "End";
            case 258 -> "Tab";
            case 341 -> "Left Control";
            case 345 -> "Right Control";
            case 342 -> "Left Alt";
            case 346 -> "Right Alt";
            case 340 -> "Left Shift";
            case 344 -> "Right Shift";
            case 265 -> "Arrow Up";
            case 264 -> "Arrow Down";
            case 263 -> "Arrow Left";
            case 262 -> "Arrow Right";
            case 39 -> "Apostrophe";
            case 259 -> "Backspace";
            case 280 -> "Caps Lock";
            case 348 -> "Menu";
            case 343 -> "Left Super";
            case 347 -> "Right Super";
            case 257 -> "Enter";
            case 335 -> "Numpad Enter";
            case 282 -> "Num Lock";
            case 32 -> "Space";
            case 290 -> "F1";
            case 291 -> "F2";
            case 292 -> "F3";
            case 293 -> "F4";
            case 294 -> "F5";
            case 295 -> "F6";
            case 296 -> "F7";
            case 297 -> "F8";
            case 298 -> "F9";
            case 299 -> "F10";
            case 300 -> "F11";
            case 301 -> "F12";
            case 302 -> "F13";
            case 303 -> "F14";
            case 304 -> "F15";
            case 305 -> "F16";
            case 306 -> "F17";
            case 307 -> "F18";
            case 308 -> "F19";
            case 309 -> "F20";
            case 310 -> "F21";
            case 311 -> "F22";
            case 312 -> "F23";
            case 313 -> "F24";
            case 314 -> "F25";
            case -2 -> "Right Click";
            case -3 -> "Middle Click";
            case -4 -> "Button 3";
            case -5 -> "Button 4";
            case 0 -> "None";
            default -> {
                String var1_1 = GLFW.glfwGetKeyName((int)n, (int)0);
                if (var1_1 == null) {
                    yield "Unknown";
                }
                yield StringUtils.capitalize((String)var1_1);
            }
        };
    }

    public static int getKeyNumber(String string) {
        return switch (string.toLowerCase()) {
            case "esc" -> {
                int var3_68;
                int var4_3 = 256;
                yield var3_68 = 256;
            }
            case "grave" -> {
                int var3_69;
                int var4_4 = 96;
                yield var3_69 = 96;
            }
            case "world1" -> {
                int var3_70;
                int var4_5 = 161;
                yield var3_70 = 161;
            }
            case "world2" -> {
                int var3_71;
                int var4_6 = 162;
                yield var3_71 = 162;
            }
            case "prtscr" -> {
                int var3_72;
                int var4_7 = 283;
                yield var3_72 = 283;
            }
            case "pause" -> {
                int var3_73;
                int var4_8 = 284;
                yield var3_73 = 284;
            }
            case "insert" -> {
                int var3_74;
                int var4_9 = 260;
                yield var3_74 = 260;
            }
            case "delete" -> {
                int var3_75;
                int var4_10 = 261;
                yield var3_75 = 261;
            }
            case "home" -> {
                int var3_76;
                int var4_11 = 268;
                yield var3_76 = 268;
            }
            case "pgup" -> {
                int var3_77;
                int var4_12 = 266;
                yield var3_77 = 266;
            }
            case "pgdown" -> {
                int var3_78;
                int var4_13 = 267;
                yield var3_78 = 267;
            }
            case "end" -> {
                int var3_79;
                int var4_14 = 269;
                yield var3_79 = 269;
            }
            case "tab" -> {
                int var3_80;
                int var4_15 = 258;
                yield var3_80 = 258;
            }
            case "lctrl" -> {
                int var3_81;
                int var4_16 = 341;
                yield var3_81 = 341;
            }
            case "rctrl" -> {
                int var3_82;
                int var4_17 = 345;
                yield var3_82 = 345;
            }
            case "lalt" -> {
                int var3_83;
                int var4_18 = 342;
                yield var3_83 = 342;
            }
            case "ralt" -> {
                int var3_84;
                int var4_19 = 346;
                yield var3_84 = 346;
            }
            case "lshift" -> {
                int var3_85;
                int var4_20 = 340;
                yield var3_85 = 340;
            }
            case "rshift" -> {
                int var3_86;
                int var4_21 = 344;
                yield var3_86 = 344;
            }
            case "up" -> {
                int var3_87;
                int var4_22 = 265;
                yield var3_87 = 265;
            }
            case "down" -> {
                int var3_88;
                int var4_23 = 264;
                yield var3_88 = 264;
            }
            case "left" -> {
                int var3_89;
                int var4_24 = 263;
                yield var3_89 = 263;
            }
            case "right" -> {
                int var3_90;
                int var4_25 = 262;
                yield var3_90 = 262;
            }
            case "apostrophe" -> {
                int var3_91;
                int var4_26 = 39;
                yield var3_91 = 39;
            }
            case "backspace" -> {
                int var3_92;
                int var4_27 = 259;
                yield var3_92 = 259;
            }
            case "capslock" -> {
                int var3_93;
                int var4_28 = 280;
                yield var3_93 = 280;
            }
            case "menu" -> {
                int var3_94;
                int var4_29 = 348;
                yield var3_94 = 348;
            }
            case "lsuper" -> {
                int var3_95;
                int var4_30 = 343;
                yield var3_95 = 343;
            }
            case "rsuper" -> {
                int var3_96;
                int var4_31 = 347;
                yield var3_96 = 347;
            }
            case "enter" -> {
                int var3_97;
                int var4_32 = 257;
                yield var3_97 = 257;
            }
            case "numenter" -> {
                int var3_98;
                int var4_33 = 335;
                yield var3_98 = 335;
            }
            case "numlock" -> {
                int var3_99;
                int var4_34 = 282;
                yield var3_99 = 282;
            }
            case "space" -> {
                int var3_100;
                int var4_35 = 32;
                yield var3_100 = 32;
            }
            case "f1" -> {
                int var3_101;
                int var4_36 = 290;
                yield var3_101 = 290;
            }
            case "f2" -> {
                int var3_102;
                int var4_37 = 291;
                yield var3_102 = 291;
            }
            case "f3" -> {
                int var3_103;
                int var4_38 = 292;
                yield var3_103 = 292;
            }
            case "f4" -> {
                int var3_104;
                int var4_39 = 293;
                yield var3_104 = 293;
            }
            case "f5" -> {
                int var3_105;
                int var4_40 = 294;
                yield var3_105 = 294;
            }
            case "f6" -> {
                int var3_106;
                int var4_41 = 295;
                yield var3_106 = 295;
            }
            case "f7" -> {
                int var3_107;
                int var4_42 = 296;
                yield var3_107 = 296;
            }
            case "f8" -> {
                int var3_108;
                int var4_43 = 297;
                yield var3_108 = 297;
            }
            case "f9" -> {
                int var3_109;
                int var4_44 = 298;
                yield var3_109 = 298;
            }
            case "f10" -> {
                int var3_110;
                int var4_45 = 299;
                yield var3_110 = 299;
            }
            case "f11" -> {
                int var3_111;
                int var4_46 = 300;
                yield var3_111 = 300;
            }
            case "f12" -> {
                int var3_112;
                int var4_47 = 301;
                yield var3_112 = 301;
            }
            case "f13" -> {
                int var3_113;
                int var4_48 = 302;
                yield var3_113 = 302;
            }
            case "f14" -> {
                int var3_114;
                int var4_49 = 303;
                yield var3_114 = 303;
            }
            case "f15" -> {
                int var3_115;
                int var4_50 = 304;
                yield var3_115 = 304;
            }
            case "f16" -> {
                int var3_116;
                int var4_51 = 305;
                yield var3_116 = 305;
            }
            case "f17" -> {
                int var3_117;
                int var4_52 = 306;
                yield var3_117 = 306;
            }
            case "f18" -> {
                int var3_118;
                int var4_53 = 307;
                yield var3_118 = 307;
            }
            case "f19" -> {
                int var3_119;
                int var4_54 = 308;
                yield var3_119 = 308;
            }
            case "f20" -> {
                int var3_120;
                int var4_55 = 309;
                yield var3_120 = 309;
            }
            case "f21" -> {
                int var3_121;
                int var4_56 = 310;
                yield var3_121 = 310;
            }
            case "f22" -> {
                int var3_122;
                int var4_57 = 311;
                yield var3_122 = 311;
            }
            case "f23" -> {
                int var3_123;
                int var4_58 = 312;
                yield var3_123 = 312;
            }
            case "f24" -> {
                int var3_124;
                int var4_59 = 313;
                yield var3_124 = 313;
            }
            case "f25" -> {
                int var3_125;
                int var4_60 = 314;
                yield var3_125 = 314;
            }
            case "rclick" -> {
                int var3_126;
                int var4_61 = -2;
                yield var3_126 = -2;
            }
            case "mclick" -> {
                int var3_127;
                int var4_62 = -3;
                yield var3_127 = -3;
            }
            case "button3" -> {
                int var3_128;
                int var4_63 = -4;
                yield var3_128 = -4;
            }
            case "button4" -> {
                int var3_129;
                int var4_64 = -5;
                yield var3_129 = -5;
            }
            case "none" -> {
                int var3_130;
                int var4_65 = 0;
                yield var3_130 = 0;
            }
            default -> {
                try {
                    int var4_66;
                    int var3_131;
                    yield var3_131 = (var4_66 = InputConstants.getKey((String)("key.keyboard." + string)).getValue());
                }
                catch (NumberFormatException var4_67) {
                    int var3_132;
                    int var5_133 = 0;
                    yield var3_132 = 0;
                }
            }
        };
    }

    public static boolean isBindDown(int n) {
        if (n == 0) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui != null && minecraft.gui.screen() != null) {
            return false;
        }
        if (minecraft.getWindow() == null) {
            return false;
        }
        if (n >= 0) {
            return InputConstants.isKeyDown((Window)minecraft.getWindow(), (int)n);
        }
        int n2 = -n - 1;
        return GLFW.glfwGetMouseButton((long)minecraft.getWindow().handle(), (int)n2) == 1;
    }
}

