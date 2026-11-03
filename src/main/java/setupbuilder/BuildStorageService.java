package com.setupbuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BuildStorageService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path storagePath = Path.of("data", "builds.json");

    public record SavedBuild(
            String id,
            String name,
            String cpu,
            String gpu,
            String motherboard,
            String ram,
            String storage,
            String pricingStrategy,
            boolean assembly,
            boolean extendedWarranty
    ) {}

    public synchronized List<SavedBuild> getAll() {
        try {
            if (!Files.exists(storagePath)) {
                return new ArrayList<>();
            }

            return mapper.readValue(
                    storagePath.toFile(),
                    new TypeReference<List<SavedBuild>>() {}
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read saved builds", e);
        }
    }

    public synchronized SavedBuild save(SavedBuild build) {
        List<SavedBuild> builds = getAll();

        SavedBuild saved = new SavedBuild(
                UUID.randomUUID().toString(),
                build.name(),
                build.cpu(),
                build.gpu(),
                build.motherboard(),
                build.ram(),
                build.storage(),
                build.pricingStrategy(),
                build.assembly(),
                build.extendedWarranty()
        );

        builds.add(saved);
        writeAll(builds);
        return saved;
    }

    public synchronized SavedBuild update(String id, SavedBuild updated) {
        List<SavedBuild> builds = getAll();

        for (int i = 0; i < builds.size(); i++) {
            if (builds.get(i).id().equals(id)) {
                SavedBuild saved = new SavedBuild(
                        id,
                        updated.name(),
                        updated.cpu(),
                        updated.gpu(),
                        updated.motherboard(),
                        updated.ram(),
                        updated.storage(),
                        updated.pricingStrategy(),
                        updated.assembly(),
                        updated.extendedWarranty()
                );

                builds.set(i, saved);
                writeAll(builds);
                return saved;
            }
        }

        throw new BuildNotFoundException();
    }

    public synchronized void delete(String id) {
        List<SavedBuild> builds = getAll();

        boolean removed = builds.removeIf(build -> build.id().equals(id));

        if (!removed) {
            throw new BuildNotFoundException();
        }

        writeAll(builds);
    }

    private void writeAll(List<SavedBuild> builds) {
        try {
            Files.createDirectories(storagePath.getParent());
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(storagePath.toFile(), builds);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save builds", e);
        }
    }

    public static class BuildNotFoundException extends RuntimeException {}
}
