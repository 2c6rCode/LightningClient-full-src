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

    private static Path2D.Double path(double d, double d2) {
        Path2D.Double double_ = new Path2D.Double();
        double_.moveTo(d, d2);
        return double_;
    }

    private static List<double[]> flatten(Path2D path2D) {
        ArrayList<double[]> arrayList = new ArrayList<double[]>();
        double[] dArray = new double[6];
        PathIterator pathIterator = path2D.getPathIterator(null, 0.015);
        while (!pathIterator.isDone()) {
            int n = pathIterator.currentSegment(dArray);
            if (n != 4) {
                arrayList.add(new double[]{dArray[0], dArray[1]});
            }
            pathIterator.next();
        }
        if (arrayList.size() > 1 && Math.hypot(((double[])arrayList.getFirst())[0] - ((double[])arrayList.getLast())[0], ((double[])arrayList.getFirst())[1] - ((double[])arrayList.getLast())[1]) < 1.0E-8) {
            arrayList.removeLast();
        }
        return List.copyOf(arrayList);
    }

    public static Mesh model(String string) {
        Path2D.Double double_ = CrystalWingsMath.path(0.08, -0.32);
        ArrayList<List<double[]>> arrayList = new ArrayList<List<double[]>>();
        switch (string) {
            case "Sharp": 
            case "Outline": {
                double_.curveTo(0.12, 0.25, 0.7, 0.48, 1.55, 1.08);
                double_.curveTo(1.44, 0.62, 1.22, 0.35, 0.79, 0.14);
                double_.lineTo(1.43, 0.56);
                double_.curveTo(1.34, 0.18, 1.05, 0.04, 0.59, -0.08);
                double_.lineTo(1.15, 0.19);
                double_.curveTo(0.96, -0.14, 0.72, -0.22, 0.38, -0.28);
                double_.curveTo(0.26, -0.29, 0.17, -0.31, 0.08, -0.32);
                break;
            }
            case "Sweep": {
                double_.curveTo(0.29, 0.1, 0.89, 0.14, 1.6, 0.65);
                double_.curveTo(1.4, 0.15, 0.93, -0.06, 0.42, -0.11);
                double_.lineTo(1.17, 0.12);
                double_.curveTo(0.95, -0.17, 0.57, -0.24, 0.08, -0.32);
                break;
            }
            case "SoftBat": {
                double_.curveTo(0.15, 0.02, 0.28, 0.56, 0.38, 0.83);
                double_.curveTo(0.58, 0.6, 1.11, 0.48, 1.5, 0.05);
                double_.curveTo(1.26, 0.18, 1.2, -0.02, 1.17, -0.22);
                double_.curveTo(0.97, -0.05, 0.8, -0.14, 0.77, -0.39);
                double_.curveTo(0.58, -0.18, 0.32, -0.26, 0.08, -0.32);
                for (double d : new double[]{0.77, 1.17}) {
                    Path2D.Double double_2 = CrystalWingsMath.path(0.38, 0.83);
                    double_2.quadTo(d - 0.22, 0.15, d, d == 0.77 ? -0.39 : -0.22);
                    arrayList.add(CrystalWingsMath.flatten(double_2));
                }
                break;
            }
            case "Butterfly": {
                double_.curveTo(0.27, 0.1, 0.83, 1.1, 1.32, 0.88);
                double_.curveTo(1.73, 0.67, 1.12, 0.1, 0.73, -0.06);
                double_.curveTo(1.28, -0.2, 1.09, -0.76, 0.67, -0.65);
                double_.curveTo(0.35, -0.61, 0.23, -0.47, 0.08, -0.32);
                Path2D.Double double_3 = CrystalWingsMath.path(0.22, -0.23);
                double_3.quadTo(0.83, 0.12, 1.2, 0.63);
                arrayList.add(CrystalWingsMath.flatten(double_3));
                break;
            }
            case "MiniAngel": {
                double_.curveTo(0.04, 0.19, 0.62, 0.3, 1.25, 0.86);
                double_.curveTo(1.46, 0.79, 1.13, 0.36, 0.77, 0.16);
                double_.curveTo(1.43, 0.66, 1.41, 0.2, 0.66, 0.04);
                double_.curveTo(1.27, 0.37, 1.25, -0.02, 0.52, -0.1);
                double_.curveTo(1.08, 0.04, 0.96, -0.29, 0.38, -0.22);
                double_.curveTo(0.79, -0.21, 0.56, -0.56, 0.08, -0.32);
                break;
            }
            default: {
                double_.curveTo(-0.01, 0.03, 0.42, 0.22, 1.27, 0.83);
                double_.curveTo(1.65, 1.01, 1.65, 0.56, 1.04, 0.22);
                double_.curveTo(1.76, 0.5, 1.61, -0.04, 0.9, 0.0);
                double_.curveTo(1.55, -0.2, 1.16, -0.56, 0.63, -0.33);
                double_.curveTo(0.39, -0.57, 0.12, -0.57, 0.08, -0.32);
                if (!string.equals("Curl")) break;
                Path2D.Double double_4 = CrystalWingsMath.path(0.21, -0.34);
                double_4.curveTo(0.4, -0.47, 0.54, -0.24, 0.43, -0.1);
                double_4.curveTo(0.32, 0.03, 0.22, -0.12, 0.32, -0.18);
                arrayList.add(CrystalWingsMath.flatten(double_4));
            }
        }
        double_.closePath();
        Object object = CrystalWingsMath.flatten(double_);
        return new Mesh((List<double[]>)object, CrystalWingsMath.triangulate((List<double[]>)object), List.copyOf(arrayList));
    }

    private static double cross(double[] dArray, double[] dArray2, double[] dArray3) {
        return (dArray2[0] - dArray[0]) * (dArray3[1] - dArray[1]) - (dArray2[1] - dArray[1]) * (dArray3[0] - dArray[0]);
    }

    public static double area(double[][] dArray) {
        double d = 0.0;
        for (int i = 0; i < dArray.length; ++i) {
            double[] dArray2 = dArray[(i + 1) % dArray.length];
            d += dArray[i][0] * dArray2[1] - dArray2[0] * dArray[i][1];
        }
        return Math.abs(d) / 2.0;
    }

    private static List<double[][]> triangulate(List<double[]> list) {
        double d;
        ArrayList<double[]> arrayList = new ArrayList<double[]>(list);
        ArrayList<double[][]> arrayList2 = new ArrayList<double[][]>();
        double d2 = 0.0;
        for (int i = 0; i < arrayList.size(); ++i) {
            double[] dArray = arrayList.get(i);
            double[] dArray2 = arrayList.get((i + 1) % arrayList.size());
            d2 += dArray[0] * dArray2[1] - dArray2[0] * dArray[1];
        }
        double d3 = d = d2 < 0.0 ? -1.0 : 1.0;
        while (arrayList.size() > 3) {
            boolean bl = false;
            for (int i = 0; i < arrayList.size(); ++i) {
                double[] dArray;
                double[] dArray3;
                double[] dArray4 = arrayList.get((i + arrayList.size() - 1) % arrayList.size());
                if (d * CrystalWingsMath.cross(dArray4, dArray3 = arrayList.get(i), dArray = arrayList.get((i + 1) % arrayList.size())) <= 1.0E-10) continue;
                boolean bl2 = false;
                for (double[] dArray5 : arrayList) {
                    if (dArray5 == dArray4 || dArray5 == dArray3 || dArray5 == dArray || !(d * CrystalWingsMath.cross(dArray4, dArray3, dArray5) >= -1.0E-10) || !(d * CrystalWingsMath.cross(dArray3, dArray, dArray5) >= -1.0E-10) || !(d * CrystalWingsMath.cross(dArray, dArray4, dArray5) >= -1.0E-10)) continue;
                    bl2 = true;
                    break;
                }
                if (bl2) continue;
                arrayList2.add(new double[][]{dArray4, dArray3, dArray});
                arrayList.remove(i);
                bl = true;
                break;
            }
            if (bl) continue;
            throw new IllegalArgumentException("Wing silhouette is not a simple polygon");
        }
        arrayList2.add((double[][])arrayList.toArray(n -> new double[n][]));
        return List.copyOf(arrayList2);
    }

    public static double progress(long l, int n) {
        return Math.clamp((double)l / ((double)Math.max(1, n) * 1000000.0), 0.0, 1.0);
    }

    public static double ease(double d) {
        d = Math.clamp(d, 0.0, 1.0);
        return 1.0 - Math.pow(1.0 - d, 3.0);
    }

    public static double appearScale(String string, double d, double d2, double d3) {
        if (string.equals("Grow")) {
            return d2 + (1.0 - d2) * CrystalWingsMath.ease(d);
        }
        if (string.equals("Pop")) {
            return d2 + (1.0 - d2) * CrystalWingsMath.ease(d) + d3 * Math.sin(Math.PI * d);
        }
        return 1.0;
    }

    public static double flap(double d, double d2, double d3) {
        return Math.sin(d * d2 * Math.PI * 2.0) * Math.toRadians(d3);
    }

    public record Mesh(List<double[]> outline, List<double[][]> triangles, List<List<double[]>> seams) {
    }
}

