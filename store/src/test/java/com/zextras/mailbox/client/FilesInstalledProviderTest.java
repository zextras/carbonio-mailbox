package com.zextras.mailbox.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.nio.file.Paths;

class FilesInstalledProviderTest {
    @Test
    void shouldThrowExceptionWhenConsulTokenFileDoesNotExist() {
        FilesInstalledProvider filesInstalledProvider = new FilesInstalledProvider(Paths.get("/wrongpath"));
        Assertions.assertThrows(UnableToCheckServiceInstalledException.class, filesInstalledProvider::isInstalled);
    }
}