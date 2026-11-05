package com.setupbuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildStorageTest {

    @TempDir
    Path tempDir;

    private BuildStorageService createStorage() {
        return new BuildStorageService(
                tempDir.resolve("builds.json")
        );
    }

    private BuildStorageService.SavedBuild sampleBuild() {
        return new BuildStorageService.SavedBuild(
                null,
                "Gaming PC",
                "cpu1",
                "gpu2",
                "mb1",
                "ram2",
                "ssd2",
                "standard",
                true,
                false
        );
    }

    @Test
    void saveBuildCreatesRecord() {
        BuildStorageService storage = createStorage();

        var saved = storage.save(sampleBuild());

        assertNotNull(saved.id());
        assertEquals("Gaming PC", saved.name());
        assertEquals(1, storage.getAll().size());
    }

    @Test
    void savedBuildSurvivesReload() {
        BuildStorageService storage = createStorage();
        storage.save(sampleBuild());

        BuildStorageService restarted = createStorage();

        List<BuildStorageService.SavedBuild> builds =
                restarted.getAll();

        assertEquals(1, builds.size());
        assertEquals("Gaming PC", builds.get(0).name());
    }

    @Test
    void updateBuildChangesName() {
        BuildStorageService storage = createStorage();

        var saved = storage.save(sampleBuild());

        var updated = new BuildStorageService.SavedBuild(
                saved.id(),
                "Updated Gaming PC",
                saved.cpu(),
                saved.gpu(),
                saved.motherboard(),
                saved.ram(),
                saved.storage(),
                saved.pricingStrategy(),
                saved.assembly(),
                saved.extendedWarranty()
        );

        storage.update(saved.id(), updated);

        assertEquals(
                "Updated Gaming PC",
                storage.getAll().get(0).name()
        );
    }

    @Test
    void deleteBuildRemovesRecord() {
        BuildStorageService storage = createStorage();

        var saved = storage.save(sampleBuild());

        storage.delete(saved.id());

        assertTrue(storage.getAll().isEmpty());
    }
}
