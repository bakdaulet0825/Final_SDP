package com.setupbuilder;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-builds")
public class SavedBuildController {

    private final BuildStorageService storage;

    public SavedBuildController(BuildStorageService storage) {
        this.storage = storage;
    }

    @GetMapping
    public List<BuildStorageService.SavedBuild> getAll() {
        return storage.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BuildStorageService.SavedBuild save(
            @RequestBody BuildStorageService.SavedBuild build
    ) {
        return storage.save(build);
    }

    @PutMapping("/{id}")
    public BuildStorageService.SavedBuild update(
            @PathVariable String id,
            @RequestBody BuildStorageService.SavedBuild build
    ) {
        return storage.update(id, build);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        storage.delete(id);
    }

    @ExceptionHandler(BuildStorageService.BuildNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound() {
        return "Saved build not found";
    }
}
