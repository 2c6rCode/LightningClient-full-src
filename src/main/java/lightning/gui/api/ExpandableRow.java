/*
 * Decompiled with CFR 0.152.
 */
package lightning.gui.api;

import java.util.List;
import lightning.gui.api.Button;

public interface ExpandableRow {
    public String getRowName();

    public boolean isOpen();

    public float getOpenAmount();

    public List<Button> getButtons();

    public void setRevealHeight(int revealHeight);

    public void setSearchQuery(String query);
}

