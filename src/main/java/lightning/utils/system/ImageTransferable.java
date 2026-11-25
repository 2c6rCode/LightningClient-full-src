/*
 * Decompiled with CFR 0.152.
 */
package lightning.utils.system;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.util.Collections;

public class ImageTransferable
implements Transferable {
    private final Image image;
    private final File file;
    private final boolean includeFile;

    public ImageTransferable(Image image, File file, boolean includeFile) {
        this.image = image;
        this.file = file;
        this.includeFile = includeFile;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        if (this.includeFile && this.file != null) {
            return new DataFlavor[]{DataFlavor.imageFlavor, DataFlavor.javaFileListFlavor};
        }
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        if (DataFlavor.imageFlavor.equals(dataFlavor)) {
            return this.image != null;
        }
        return this.includeFile && this.file != null && DataFlavor.javaFileListFlavor.equals(dataFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException {
        if (DataFlavor.imageFlavor.equals(dataFlavor) && this.image != null) {
            return this.image;
        }
        if (this.includeFile && this.file != null && DataFlavor.javaFileListFlavor.equals(dataFlavor)) {
            return Collections.singletonList(this.file);
        }
        throw new UnsupportedFlavorException(dataFlavor);
    }
}

