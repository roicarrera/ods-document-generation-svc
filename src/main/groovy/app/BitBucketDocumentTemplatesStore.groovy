package app

import com.google.inject.Inject
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import feign.Feign
import feign.Headers
import feign.Param
import feign.RequestLine
import feign.auth.BasicAuthRequestInterceptor;

import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

import net.lingala.zip4j.core.ZipFile

import org.apache.http.client.utils.URIBuilder

interface BitBucketDocumentTemplatesStoreHttpAPI {
  @Headers("Accept: application/octet-stream")
  @RequestLine("GET /rest/api/latest/projects/{documentTemplatesProject}/repos/{documentTemplatesRepo}/archive?at=refs/heads/release/v{version}&format=zip")
  byte[] getTemplatesZipArchiveForVersion(@Param("documentTemplatesProject") String documentTemplatesProject, @Param("documentTemplatesRepo") String documentTemplatesRepo, @Param("version") String version)
}

class BitBucketDocumentTemplatesStore implements DocumentTemplatesStore {

    Config config

    // TODO: use dependency injection
    BitBucketDocumentTemplatesStore() {
        this.config = ConfigFactory.load()
    }

    // Extract some Zip archive content into a target directory
    private static def Path extractZipArchive(byte[] zipArchiveContent, Path targetDir) {
        def tmpFile = Files.createTempFile("archive-", ".zip")

        try {
            // Write content to a temp file
            tmpFile.setBytes(zipArchiveContent)

            // Create a ZipFile from the temp file
            ZipFile zipFile = new ZipFile(tmpFile.toFile())

            // Extract the ZipFile into targetDir
            zipFile.extractAll(targetDir.toString())
        } catch (Throwable e) {
            throw e
        } finally {
            Files.delete(tmpFile)
        }

        return targetDir
    }

    // Get document templates of a specific version into a target directory
    def Path getTemplatesForVersion(String version, Path targetDir) {
        def uri = getZipArchiveDownloadURI(version)

        Feign.Builder builder = Feign.builder()

        if (System.getenv("BITBUCKET_USERNAME") && System.getenv("BITBUCKET_PASSWORD")) {
            builder.requestInterceptor(new BasicAuthRequestInterceptor(
                System.getenv("BITBUCKET_USERNAME"),
                System.getenv("BITBUCKET_PASSWORD")
            ))
        }

        BitBucketDocumentTemplatesStoreHttpAPI store = builder.target(
            BitBucketDocumentTemplatesStoreHttpAPI.class,
            uri.getScheme() + "://" + uri.getAuthority()
        )

        def zipArchiveContent = store.getTemplatesZipArchiveForVersion(
            System.getenv("BITBUCKET_DOCUMENT_TEMPLATES_PROJECT"),
            System.getenv("BITBUCKET_DOCUMENT_TEMPLATES_REPO"),
            version
        )
        return extractZipArchive(zipArchiveContent, targetDir)
    }

    // Get a URI to download document templates of a specific version
    def URI getZipArchiveDownloadURI(String version) {
        return new URIBuilder(System.getenv("BITBUCKET_URL"))
            .setPath("/rest/api/latest/projects/${System.getenv('BITBUCKET_DOCUMENT_TEMPLATES_PROJECT')}/repos/${System.getenv('BITBUCKET_DOCUMENT_TEMPLATES_REPO')}/archive")
            .addParameter("at", "refs/heads/release/v${version}")
            .addParameter("format", "zip")
            .build()
    }
}
