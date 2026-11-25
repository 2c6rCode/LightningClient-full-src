/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.graphics;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

public final class CrystalWingsMath {
    public static final String[] STYLES = new String[]{"Cloud", "Curl", "Sharp", "MiniAngel", "Sweep", "SoftBat", "Butterfly", "Outline"};

    private CrystalWingsMath() {
    }

    private static Path2D.Double path(double startX, double startY) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(startX, startY);
        return path;
    }

    private static List<double[]> flatten(Path2D path2D) {
        ArrayList<double[]> arrayList = new ArrayList<double[]>();
        double[] coords = new double[6];
        PathIterator pathIterator = path2D.getPathIterator(null, 0.015);
        while (!pathIterator.isDone()) {
            int segmentType = pathIterator.currentSegment(coords);
            if (segmentType != 4) {
                arrayList.add(new double[]{coords[0], coords[1]});
            }
            pathIterator.next();
        }
        if (arrayList.size() > 1 && Math.hypot(((double[])arrayList.getFirst())[0] - ((double[])arrayList.getLast())[0], ((double[])arrayList.getFirst())[1] - ((double[])arrayList.getLast())[1]) < 1.0E-8) {
            arrayList.removeLast();
        }
        return List.copyOf(arrayList);
    }

    public static Mesh model(String style) {
        Path2D.Double path = CrystalWingsMath.path(0.08, -0.32);
        ArrayList<List<double[]>> seams = new ArrayList<List<double[]>>();
        switch (style) {
            case "Sharp": 
            case "Outline": {
                path.curveTo(0.12, 0.25, 0.7, 0.48, 1.55, 1.08);
                path.curveTo(1.44, 0.62, 1.22, 0.35, 0.79, 0.14);
                path.lineTo(1.43, 0.56);
                path.curveTo(1.34, 0.18, 1.05, 0.04, 0.59, -0.08);
                path.lineTo(1.15, 0.19);
                path.curveTo(0.96, -0.14, 0.72, -0.22, 0.38, -0.28);
                path.curveTo(0.26, -0.29, 0.17, -0.31, 0.08, -0.32);
                break;
            }
            case "Sweep": {
                path.curveTo(0.29, 0.1, 0.89, 0.14, 1.6, 0.65);
                path.curveTo(1.4, 0.15, 0.93, -0.06, 0.42, -0.11);
                path.lineTo(1.17, 0.12);
                path.curveTo(0.95, -0.17, 0.57, -0.24, 0.08, -0.32);
                break;
            }
            case "SoftBat": {
                path.curveTo(0.15, 0.02, 0.28, 0.56, 0.38, 0.83);
                path.curveTo(0.58, 0.6, 1.11, 0.48, 1.5, 0.05);
                path.curveTo(1.26, 0.18, 1.2, -0.02, 1.17, -0.22);
                path.curveTo(0.97, -0.05, 0.8, -0.14, 0.77, -0.39);
                path.curveTo(0.58, -0.18, 0.32, -0.26, 0.08, -0.32);
                for (double seamX : new double[]{0.77, 1.17}) {
                    Path2D.Double seamPath = CrystalWingsMath.path(0.38, 0.83);
                    seamPath.quadTo(seamX - 0.22, 0.15, seamX, seamX == 0.77 ? -0.39 : -0.22);
                    seams.add(CrystalWingsMath.flatten(seamPath));
                }
                break;
            }
            case "Butterfly": {
                path.curveTo(0.27, 0.1, 0.83, 1.1, 1.32, 0.88);
                path.curveTo(1.73, 0.67, 1.12, 0.1, 0.73, -0.06);
                path.curveTo(1.28, -0.2, 1.09, -0.76, 0.67, -0.65);
                path.curveTo(0.35, -0.61, 0.23, -0.47, 0.08, -0.32);
                Path2D.Double seamPath = CrystalWingsMath.path(0.22, -0.23);
                seamPath.quadTo(0.83, 0.12, 1.2, 0.63);
                seams.add(CrystalWingsMath.flatten(seamPath));
                break;
            }
            case "MiniAngel": {
                path.curveTo(0.04, 0.19, 0.62, 0.3, 1.25, 0.86);
                path.curveTo(1.46, 0.79, 1.13, 0.36, 0.77, 0.16);
                path.curveTo(1.43, 0.66, 1.41, 0.2, 0.66, 0.04);
                path.curveTo(1.27, 0.37, 1.25, -0.02, 0.52, -0.1);
                path.curveTo(1.08, 0.04, 0.96, -0.29, 0.38, -0.22);
                path.curveTo(0.79, -0.21, 0.56, -0.56, 0.08, -0.32);
                break;
            }
            default: {
                path.curveTo(-0.01, 0.03, 0.42, 0.22, 1.27, 0.83);
                path.curveTo(1.65, 1.01, 1.65, 0.56, 1.04, 0.22);
                path.curveTo(1.76, 0.5, 1.61, -0.04, 0.9, 0.0);
                path.curveTo(1.55, -0.2, 1.16, -0.56, 0.63, -0.33);
                path.curveTo(0.39, -0.57, 0.12, -0.57, 0.08, -0.32);
                if (!style.equals("Curl")) break;
                Path2D.Double seamPath = CrystalWingsMath.path(0.21, -0.34);
                seamPath.curveTo(0.4, -0.47, 0.54, -0.24, 0.43, -0.1);
                seamPath.curveTo(0.32, 0.03, 0.22, -0.12, 0.32, -0.18);
                seams.add(CrystalWingsMath.flatten(seamPath));
            }
        }
        path.closePath();
        List<double[]> outline = CrystalWingsMath.flatten(path);
        return new Mesh(outline, CrystalWingsMath.triangulate(outline), List.copyOf(seams));
    }

    private static double cross(double[] origin, double[] first, double[] second) {
        return (first[0] - origin[0]) * (second[1] - origin[1]) - (first[1] - origin[1]) * (second[0] - origin[0]);
    }

    public static double area(double[][] polygon) {
        double sum = 0.0;
        for (int i = 0; i < polygon.length; ++i) {
            double[] next = polygon[(i + 1) % polygon.length];
            sum += polygon[i][0] * next[1] - next[0] * polygon[i][1];
        }
        return Math.abs(sum) / 2.0;
    }

    private static List<double[][]> triangulate(List<double[]> outline) {
        ArrayList<double[]> remaining = new ArrayList<double[]>(outline);
        ArrayList<double[][]> triangles = new ArrayList<double[][]>();
        double signedArea = 0.0;
        for (int i = 0; i < remaining.size(); ++i) {
            double[] curr = remaining.get(i);
            double[] next = remaining.get((i + 1) % remaining.size());
            signedArea += curr[0] * next[1] - next[0] * curr[1];
        }
        double winding = signedArea < 0.0 ? -1.0 : 1.0;
        while (remaining.size() > 3) {
            boolean earFound = false;
            for (int i = 0; i < remaining.size(); ++i) {
                double[] prev = remaining.get((i + remaining.size() - 1) % remaining.size());
                double[] curr = remaining.get(i);
                double[] next = remaining.get((i + 1) % remaining.size());
                if (winding * CrystalWingsMath.cross(prev, curr, next) <= 1.0E-10) continue;
                boolean containsPoint = false;
                for (double[] candidate : remaining) {
                    if (candidate == prev || candidate == curr || candidate == next || !(winding * CrystalWingsMath.cross(prev, curr, candidate) >= -1.0E-10) || !(winding * CrystalWingsMath.cross(curr, next, candidate) >= -1.0E-10) || !(winding * CrystalWingsMath.cross(next, prev, candidate) >= -1.0E-10)) continue;
                    containsPoint = true;
                    break;
                }
                if (containsPoint) continue;
                triangles.add(new double[][]{prev, curr, next});
                remaining.remove(i);
                earFound = true;
                break;
            }
            if (earFound) continue;
            throw new IllegalArgumentException("Wing silhouette is not a simple polygon");
        }
        triangles.add((double[][])remaining.toArray(size -> new double[size][]));
        return List.copyOf(triangles);
    }

    public static double progress(long elapsedNanos, int durationMs) {
        return Math.clamp((double)elapsedNanos / ((double)Math.max(1, durationMs) * 1000000.0), 0.0, 1.0);
    }

    public static double ease(double t) {
        t = Math.clamp(t, 0.0, 1.0);
        return 1.0 - Math.pow(1.0 - t, 3.0);
    }

    public static double appearScale(String mode, double t, double startScale, double overshoot) {
        if (mode.equals("Grow")) {
            return startScale + (1.0 - startScale) * CrystalWingsMath.ease(t);
        }
        if (mode.equals("Pop")) {
            return startScale + (1.0 - startScale) * CrystalWingsMath.ease(t) + overshoot * Math.sin(Math.PI * t);
        }
        return 1.0;
    }

    public static double flap(double timeSeconds, double frequency, double amplitudeDeg) {
        return Math.sin(timeSeconds * frequency * Math.PI * 2.0) * Math.toRadians(amplitudeDeg);
    }

    public record Mesh(List<double[]> outline, List<double[][]> triangles, List<List<double[]>> seams) {
    }
}

