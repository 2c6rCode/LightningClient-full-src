# "Nice Security" - Museum of Dead Code

These are the dead decompiled classes from the original author's joke packer and auth system:
- `AesCrypto.java`: The AES decryption helper.
- `PayloadClassLoader.java`: The custom classloader reading `.enc` files and calling `defineClass`.
- `Payload.java`: The remote downloader checking the `LPK2` header and throwing Vietnamese error messages.
- `Injector.java` & `MemHandler.java`: The in-memory URL protocol handler (`lightning-mem:/`).

Moved out of the active compilation source (`src/main/java`) so scanner bots don't flag the compiled mod JAR as a dropper.
