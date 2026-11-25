/*
 * Decompiled with CFR 0.152.
 */
package lightning.gui.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.ToIntFunction;

public final class DescriptionLayout {
    private DescriptionLayout() {
    }

    public static List<String> wrap(String text, int maxWidth, ToIntFunction<String> widthFn) {
        if (text.isEmpty()) {
            return List.of();
        }
        if (maxWidth < 1) {
            throw new IllegalArgumentException("No text width");
        }
        ArrayList<String> lines = new ArrayList<String>();
        for (String rawLine : text.split("\\R", -1)) {
            String currentLine = "";
            for (String word : rawLine.strip().split("\\s+")) {
                int codePoint;
                if (word.isEmpty()) continue;
                String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
                if (widthFn.applyAsInt(candidate) <= maxWidth) {
                    currentLine = candidate;
                    continue;
                }
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine);
                    currentLine = "";
                }
                for (int i = 0; i < word.length(); i += Character.charCount(codePoint)) {
                    codePoint = word.codePointAt(i);
                    String glyph = new String(Character.toChars(codePoint));
                    if (widthFn.applyAsInt(glyph) > maxWidth) {
                        throw new IllegalArgumentException("Glyph exceeds text width");
                    }
                    if (widthFn.applyAsInt(currentLine + glyph) > maxWidth) {
                        lines.add(currentLine);
                        currentLine = "";
                    }
                    currentLine = currentLine + glyph;
                }
            }
            lines.add(currentLine);
        }
        return List.copyOf(lines);
    }

    public static Panel create(String englishText, String vietnameseText, String title, String hint, int padding, int lineHeight, int headerHeight, int preferredWidth, int preferredX, int preferredY, int maxX, int maxY, List<Rect> obstacles, ToIntFunction<String> widthFn) {
        int resolvedHeaderHeight = Math.max(headerHeight, lineHeight + 4);
        int minWidth = widthFn.applyAsInt(title) + widthFn.applyAsInt(hint) + padding * 3;
        if (maxX < minWidth || maxY < resolvedHeaderHeight + padding * 2) {
            return null;
        }
        LinkedHashSet<Integer> candidateWidths = new LinkedHashSet<Integer>();
        candidateWidths.add(Math.min(maxX, Math.max(minWidth, preferredWidth)));
        for (int i = candidateWidths.iterator().next() + 24; i < maxX; i += 24) {
            candidateWidths.add(i);
        }
        candidateWidths.add(maxX);
        for (Rect rect : obstacles) {
            int rightSpace;
            if (rect.x >= minWidth && rect.x <= maxX) {
                candidateWidths.add(rect.x);
            }
            if ((rightSpace = maxX - rect.x - rect.width) >= minWidth && rightSpace <= maxX) {
                candidateWidths.add(rightSpace);
            }
            for (Rect rect2 : obstacles) {
                int gapWidth = rect2.x - rect.x - rect.width;
                if (gapWidth < minWidth || gapWidth > maxX) continue;
                candidateWidths.add(gapWidth);
            }
        }
        Iterator<Integer> iterator = candidateWidths.iterator();
        while (iterator.hasNext()) {
            List<String> vietnameseLines;
            List<String> englishLines;
            int width = iterator.next();
            try {
                englishLines = DescriptionLayout.wrap(englishText, width - padding * 2, widthFn);
                vietnameseLines = DescriptionLayout.wrap(vietnameseText, width - padding * 2, widthFn);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                continue;
            }
            int height = resolvedHeaderHeight + padding * 2 + (englishLines.size() + vietnameseLines.size()) * lineHeight;
            if (height > maxY) continue;
            LinkedHashSet<Integer> candidateXs = new LinkedHashSet<Integer>();
            LinkedHashSet<Integer> candidateYs = new LinkedHashSet<Integer>();
            candidateXs.add(Math.clamp((long)preferredX, 0, maxX - width));
            candidateYs.add(Math.clamp((long)preferredY, 0, maxY - height));
            candidateXs.add(0);
            candidateXs.add(maxX - width);
            candidateYs.add(0);
            candidateYs.add(maxY - height);
            for (Rect rect : obstacles) {
                candidateXs.add(rect.x - width);
                candidateXs.add(rect.x + rect.width);
                candidateYs.add(rect.y - height);
                candidateYs.add(rect.y + rect.height);
            }
            for (int candY : candidateYs) {
                for (int candX : candidateXs) {
                    if (candX < 0 || candY < 0 || candX + width > maxX || candY + height > maxY) continue;
                    Rect rect = new Rect(candX, candY, width, height);
                    if (!obstacles.stream().noneMatch(rect::intersects)) continue;
                    return new Panel(rect, resolvedHeaderHeight, englishLines, vietnameseLines);
                }
            }
        }
        return null;
    }

    public record Rect(int x, int y, int width, int height) {
        public boolean intersects(Rect rect) {
            return this.x < rect.x + rect.width && this.x + this.width > rect.x && this.y < rect.y + rect.height && this.y + this.height > rect.y;
        }
    }

    public record Panel(Rect bounds, int headerHeight, List<String> english, List<String> vietnamese) {
        public int totalHeight() {
            return this.bounds.height();
        }
    }
}

