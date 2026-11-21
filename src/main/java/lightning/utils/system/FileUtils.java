/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.system;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

public class FileUtils {
    public static void resetFile(String string) throws IOException {
        if (Files.exists(Paths.get(string, new String[0]), new LinkOption[0])) {
            new File(string).delete();
        }
        Files.createFile(Paths.get(string, new String[0]), new FileAttribute[0]);
    }

    public static void createDirectory(String string) throws IOException {
        if (!Files.exists(Paths.get(string, new String[0]), new LinkOption[0])) {
            Files.createDirectories(Paths.get(string, new String[0]), new FileAttribute[0]);
        }
    }

    public static boolean fileExists(String string) {
        return Files.exists(Paths.get(string, new String[0]), new LinkOption[0]);
    }

    public static void playSound(File file, float f) {
        if (file.exists()) {
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(file));
                ((FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(20.0f * (float)Math.log10(f));
                clip.start();
                clip.addLineListener(lineEvent -> {
                    if (lineEvent.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public static List<String> readLines(File file) {
        List<String> list = new ArrayList<String>();
        if (file.exists()) {
            try (Stream<String> stream = Files.lines(Paths.get(file.getPath(), new String[0]));){
                list = stream.filter(string -> !string.isEmpty()).toList();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return list;
    }
}

