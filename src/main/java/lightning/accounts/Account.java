/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.Expose
 *  com.google.gson.annotations.SerializedName
 */
package lightning.accounts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lightning.accounts.AccountType;

public class Account {
    @Expose
    @SerializedName(value="name")
    private String name;
    @Expose
    @SerializedName(value="uuid")
    private String uuid;
    @Expose
    @SerializedName(value="type")
    private AccountType type;
    @Expose
    @SerializedName(value="refreshToken")
    private String refreshToken;
    @Expose
    @SerializedName(value="accessToken")
    private String accessToken;
    @Expose
    @SerializedName(value="lastUsed")
    private long lastUsed;

    public Account() {
    }

    public Account(String string, String string2, AccountType accountType) {
        this.name = string;
        this.uuid = string2;
        this.type = accountType;
        this.lastUsed = System.currentTimeMillis();
    }

    public Account(String string, String string2, AccountType accountType, String string3, String string4) {
        this.name = string;
        this.uuid = string2;
        this.type = accountType;
        this.refreshToken = string3;
        this.accessToken = string4;
        this.lastUsed = System.currentTimeMillis();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String string) {
        this.name = string;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String string) {
        this.uuid = string;
    }

    public AccountType getType() {
        return this.type;
    }

    public void setType(AccountType accountType) {
        this.type = accountType;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(String string) {
        this.refreshToken = string;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String string) {
        this.accessToken = string;
    }

    public long getLastUsed() {
        return this.lastUsed;
    }

    public void setLastUsed(long l) {
        this.lastUsed = l;
    }

    public UUID getProfileId() {
        try {
            if (this.uuid != null && !this.uuid.isEmpty()) {
                if (this.uuid.contains("-")) {
                    return UUID.fromString(this.uuid);
                }
                return UUID.fromString(this.uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})", "$1-$2-$3-$4-$5"));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.name).getBytes(StandardCharsets.UTF_8));
    }
}

