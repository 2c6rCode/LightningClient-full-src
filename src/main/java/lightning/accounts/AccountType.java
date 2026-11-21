/*
 * Decompiled with CFR 0.152.
 */
package lightning.accounts;

public enum AccountType {
    OFFLINE("Offline"),
    MICROSOFT("Microsoft"),
    SESSION("Session");

    private final String displayName;

    private AccountType(String string2) {
        this.displayName = string2;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}

