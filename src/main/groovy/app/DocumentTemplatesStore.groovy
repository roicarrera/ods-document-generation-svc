package app

import java.nio.file.Path

interface DocumentTemplatesStore {
    // Get document templates of a specific version into a target directory
    public Path getTemplatesForVersion(String version, Path targetDir)
}
