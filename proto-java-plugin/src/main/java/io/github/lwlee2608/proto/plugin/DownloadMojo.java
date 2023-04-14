package io.github.lwlee2608.proto.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

@Mojo(name = "download")
public class DownloadMojo extends AbstractMojo {

    @Parameter(
            name = "mavenRepoUrl",
            property = "maven.repo.url",
            defaultValue = "https://repo1.maven.org/maven2"
    )
    // Alternative mirrors: https://repo1.maven.org/maven2, https://repo.maven.apache.org/maven2
    private String mavenRepoUrl;

    @Parameter(
            name = "outputDirectory",
            property = "output.dir",
            defaultValue = "${project.build.directory}/protoc"
    )
    private File outputDirectory;

    @Parameter(
            name = "protocVersion",
            property = "protoc.version",
            defaultValue = "3.22.3"
    )
    private String protocVersion;

    @Parameter(
            name = "os",
            property = "os.identifier",
            defaultValue = ""
    )
    private String os;

    private PlatformIdentifier.OperationSystem operationSystem = PlatformIdentifier.getOperationSystem();
    private PlatformIdentifier.Architecture architecture = PlatformIdentifier.getArchitecture();

    public void execute() {
        if (os == null || os.isEmpty()) {
            os = resolveOperationSystem();
        }

        // Download Binary
        String downloadUrl = mavenRepoUrl + "/com/google/protobuf/protoc/" + protocVersion + "/protoc-" + protocVersion + "-" + os + ".exe";
        getLog().info("Starting download protoc binary from " + downloadUrl);

        Path targetProtocBinary = Paths.get(outputDirectory.getAbsolutePath() + "/bin/protoc.exe");
        try {
            downloadFile(downloadUrl, targetProtocBinary);
        } catch (Exception e) {
            getLog().error("Fail to download protoc binary with URL: " + downloadUrl);
            getLog().error("error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        setExecutable(targetProtocBinary);

        getLog().info("Proto Java plugin completed");
    }

    private String resolveOperationSystem() {
        // follow naming convention in https://repo1.maven.org/maven2/com/google/protobuf/protoc/3.22.3/
        switch (operationSystem) {
            case WINDOWS:
                return "windows-x86_64";
            case LINUX:
                switch (architecture) {
                    case AMD64: return "linux-x86_64";
                    case AARCH64: return "osx-aarch_64";
                }
                break;
            case MAC:
                switch (architecture) {
                    case AMD64: return "osx-x86_64";
                    case AARCH64: return "osx-aarch_64";
                }
                break;
        }

        throw new RuntimeException("Unsupported OS or architecture. Please specific os.identifier parameter for proto-java-plugin");
    }

    private void downloadFile(String url, Path outputFile) throws Exception {
        URL downloadUrl = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(downloadUrl.openStream());

        // Create directory structure for output file
        Path outputDirectory = outputFile.getParent();
        if (outputDirectory != null && !Files.exists(outputDirectory)) {
            Files.createDirectories(outputDirectory);
        }

        FileOutputStream fos = new FileOutputStream(outputFile.toFile());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    private void setExecutable(Path path) {
        // Make binary executable
        if (operationSystem == PlatformIdentifier.OperationSystem.WINDOWS) {
            return;
        }

        try {
            Set<PosixFilePermission> filePermissions = Files.getPosixFilePermissions(path);
            filePermissions.add(PosixFilePermission.OWNER_EXECUTE);

            Files.setPosixFilePermissions(path, filePermissions);
        } catch (IOException e) {
            getLog().error("Fail to make binary executable");
            throw new RuntimeException(e);
        }
    }
}
