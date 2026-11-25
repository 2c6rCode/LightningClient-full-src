/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  com.mojang.authlib.minecraft.UserApiService
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  lightning.mixins.accessors.MinecraftClientAccessor
 *  lombok.Generated
 *  net.minecraft.client.User
 *  net.minecraft.client.multiplayer.ProfileKeyPairManager
 */
package lightning.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import lightning.Night;
import lightning.accounts.Account;
import lightning.accounts.AccountType;
import lightning.accounts.MicrosoftAuth;
import lightning.mixins.accessors.MinecraftClientAccessor;
import lightning.utils.IMinecraft;
import lombok.Generated;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;

public class AccountManager
implements IMinecraft {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final List<Account> accounts = new CopyOnWriteArrayList<Account>();
    private Account currentAccount;

    public AccountManager() {
        this.loadAccounts();
    }

    public void loadAccounts() {
        try {
            Path path = this.getAccountsPath();
            if (!Files.exists(path, new LinkOption[0])) {
                return;
            }
            try (BufferedReader bufferedReader = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
                Type type = new TypeToken<List<Account>>(){}.getType();
                List list = (List)GSON.fromJson((Reader)bufferedReader, type);
                if (list != null) {
                    this.accounts.clear();
                    this.accounts.addAll(list);
                }
            }
        }
        catch (Exception exception) {
            Night.LOGGER.error("Failed to load accounts.json", (Throwable)exception);
        }
    }

    public void saveAccounts() {
        try {
            Path path = this.getAccountsPath();
            if (path.getParent() != null && !Files.exists(path.getParent(), new LinkOption[0])) {
                Files.createDirectories(path.getParent(), new FileAttribute[0]);
            }
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson(this.accounts, (Appendable)bufferedWriter);
            }
        }
        catch (Exception exception) {
            Night.LOGGER.error("Failed to save accounts.json", (Throwable)exception);
        }
    }

    private Path getAccountsPath() {
        return Paths.get("Lightning", "accounts.json");
    }

    private void applySession(User user, String accessToken) {
        MinecraftClientAccessor minecraftClientAccessor = (MinecraftClientAccessor)mc;
        minecraftClientAccessor.setUser(user);
        try {
            if (accessToken != null && !accessToken.isEmpty()) {
                YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
                UserApiService userApiService = yggdrasilAuthenticationService.createUserApiService(accessToken);
                minecraftClientAccessor.setUserApiService(userApiService);
                ProfileKeyPairManager profileKeyPairManager = ProfileKeyPairManager.create((UserApiService)userApiService, (User)user, (Path)AccountManager.mc.gameDirectory.toPath());
                minecraftClientAccessor.setProfileKeyPairManager(profileKeyPairManager);
                profileKeyPairManager.prepareKeyPair();
            } else {
                minecraftClientAccessor.setUserApiService(UserApiService.OFFLINE);
                minecraftClientAccessor.setProfileKeyPairManager(ProfileKeyPairManager.EMPTY_KEY_MANAGER);
            }
        }
        catch (Exception exception) {
            Night.LOGGER.warn("Failed to initialize ProfileKeyPairManager for account: " + user.getName(), (Throwable)exception);
        }
    }

    public boolean login(Account account) {
        if (account == null) {
            return false;
        }
        try {
            if (account.getType() == AccountType.OFFLINE) {
                User user = new User(account.getName(), account.getProfileId(), "", Optional.empty(), Optional.empty());
                this.applySession(user, "");
                this.currentAccount = account;
                account.setLastUsed(System.currentTimeMillis());
                this.saveAccounts();
                return true;
            }
            if (account.getType() == AccountType.MICROSOFT) {
                MicrosoftAuth.AuthResult authResult;
                if (account.getRefreshToken() != null && !account.getRefreshToken().isEmpty() && (authResult = MicrosoftAuth.refreshAccount(account.getRefreshToken())).success()) {
                    account.setName(authResult.username());
                    account.setUuid(authResult.uuid());
                    account.setAccessToken(authResult.accessToken());
                    if (authResult.refreshToken() != null && !authResult.refreshToken().isEmpty()) {
                        account.setRefreshToken(authResult.refreshToken());
                    }
                    User user = new User(account.getName(), account.getProfileId(), account.getAccessToken(), Optional.empty(), Optional.empty());
                    this.applySession(user, account.getAccessToken());
                    this.currentAccount = account;
                    account.setLastUsed(System.currentTimeMillis());
                    this.saveAccounts();
                    return true;
                }
                if (account.getAccessToken() != null && !account.getAccessToken().isEmpty()) {
                    User user = new User(account.getName(), account.getProfileId(), account.getAccessToken(), Optional.empty(), Optional.empty());
                    this.applySession(user, account.getAccessToken());
                    this.currentAccount = account;
                    account.setLastUsed(System.currentTimeMillis());
                    this.saveAccounts();
                    return true;
                }
                return false;
            }
            if (account.getType() == AccountType.SESSION) {
                User user = new User(account.getName(), account.getProfileId(), account.getAccessToken() != null ? account.getAccessToken() : "", Optional.empty(), Optional.empty());
                this.applySession(user, account.getAccessToken());
                this.currentAccount = account;
                account.setLastUsed(System.currentTimeMillis());
                this.saveAccounts();
                return true;
            }
        }
        catch (Exception exception) {
            Night.LOGGER.error("Failed to login to account: " + account.getName(), (Throwable)exception);
            return false;
        }
        return false;
    }

    public Account loginOffline(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        username = username.trim();
        Account account = null;
        for (Account existing : this.accounts) {
            if (existing.getType() != AccountType.OFFLINE || !existing.getName().equalsIgnoreCase(username)) continue;
            account = existing;
            break;
        }
        if (account == null) {
            UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
            account = new Account(username, offlineUuid.toString(), AccountType.OFFLINE);
            this.accounts.add(account);
        }
        this.login(account);
        return account;
    }

    public void addAccount(Account account) {
        if (account == null) {
            return;
        }
        this.accounts.removeIf(existing -> existing.getName().equalsIgnoreCase(account.getName()) && existing.getType() == account.getType());
        this.accounts.add(account);
        this.saveAccounts();
    }

    public void removeAccount(Account account) {
        if (account == null) {
            return;
        }
        this.accounts.remove(account);
        if (this.currentAccount == account) {
            this.currentAccount = null;
        }
        this.saveAccounts();
    }

    public String getCurrentName() {
        if (mc.getUser() != null) {
            return mc.getUser().getName();
        }
        return this.currentAccount != null ? this.currentAccount.getName() : "Unknown";
    }

    @Generated
    public List<Account> getAccounts() {
        return this.accounts;
    }

    @Generated
    public Account getCurrentAccount() {
        return this.currentAccount;
    }

    @Generated
    public void setCurrentAccount(Account account) {
        this.currentAccount = account;
    }
}

