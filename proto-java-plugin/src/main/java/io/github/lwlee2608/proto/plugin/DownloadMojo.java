package io.github.lwlee2608.proto.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static io.github.lwlee2608.proto.plugin.PlatformIdentifier.Architecture.AARCH64;
import static io.github.lwlee2608.proto.plugin.PlatformIdentifier.Architecture.AMD64;
import static io.github.lwlee2608.proto.plugin.PlatformIdentifier.OperationSystem.LINUX;
import static io.github.lwlee2608.proto.plugin.PlatformIdentifier.OperationSystem.MAC;
import static io.github.lwlee2608.proto.plugin.PlatformIdentifier.OperationSystem.WINDOWS;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;

@Mojo(name = "download")
public class DownloadMojo extends AbstractMojo {


    @Parameter(
            name = "protocVersion",
            property = "protoc.version",
            defaultValue = "3.22.3"
    )
    protected String protocVersion;

    @Parameter(
            property = "outputDirectory",
            defaultValue = "${project.build.directory}/protoc/bin",
            required = true
    )
    protected File outputDirectory;

    @Parameter(
            name = "os",
            property = "os.identifier",
            defaultValue = ""
    )
    private String os;

    @Parameter(
            required = true,
            readonly = true,
            property = "localRepository"
    )
    protected ArtifactRepository localRepository;

    @Parameter(
            required = true,
            readonly = true,
            defaultValue = "${project.remoteArtifactRepositories}"
    )
    protected List<ArtifactRepository> remoteRepositories;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession session;

    @SuppressWarnings("deprecation")
    @Component
    protected ArtifactFactory artifactFactory;

    @Component
    protected RepositorySystem repositorySystem;

    @Component
    protected ResolutionErrorHandler resolutionErrorHandler;

    private final PlatformIdentifier.OperationSystem operationSystem = PlatformIdentifier.getOperationSystem();
    private final PlatformIdentifier.Architecture architecture = PlatformIdentifier.getArchitecture();

    public void execute() {
        if (os == null || os.isEmpty()) {
            os = resolveOperationSystem();
        }
        final Artifact artifact;
        try {
            artifact = artifactFactory.createDependencyArtifact(
                    "com.google.protobuf",
                    "protoc",
                    VersionRange.createFromVersionSpec(protocVersion),
                    "exe",
                    os,
                    Artifact.SCOPE_RUNTIME);
        } catch (InvalidVersionSpecificationException e) {
            getLog().error("Version " + protocVersion + " not found.");
            throw new RuntimeException(e);
        }
        // Download artifact
        resolveBinaryArtifact(artifact);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void resolveBinaryArtifact(Artifact artifact) {
        final ArtifactResolutionRequest request = new ArtifactResolutionRequest()
                .setArtifact(project.getArtifact())
                .setResolveRoot(false)
                .setResolveTransitively(false)
                .setArtifactDependencies(singleton(artifact))
                .setManagedVersionMap(emptyMap())
                .setLocalRepository(localRepository)
                .setRemoteRepositories(remoteRepositories)
                .setOffline(session.isOffline())
                .setForceUpdate(session.getRequest().isUpdateSnapshots())
                .setServers(session.getRequest().getServers())
                .setMirrors(session.getRequest().getMirrors())
                .setProxies(session.getRequest().getProxies());

        final ArtifactResolutionResult result = repositorySystem.resolve(request);

        try {
            resolutionErrorHandler.throwErrors(request, result);
        } catch (ArtifactResolutionException e) {
            throw new RuntimeException("Unable to resolve artifact: " + e.getMessage(), e);
        }

        Set<Artifact> artifacts = result.getArtifacts();

        if (artifacts == null || artifacts.isEmpty()) {
            throw new RuntimeException("Unable to resolve artifact");
        }

        Artifact resolvedBinaryArtifact = artifacts.iterator().next();
        File sourceFile = resolvedBinaryArtifact.getFile();
        String targetFileName = "protoc.exe";
        File targetFile = new File(outputDirectory, targetFileName);

        if (targetFile.exists()) {
            return;
        }

        try {
            FileUtils.forceMkdir(outputDirectory);
            FileUtils.copyFile(sourceFile, targetFile);
            if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
                targetFile.setExecutable(true);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to create or copy file: " + e.getMessage(), e);
        }
    }

    private String resolveOperationSystem() {
        switch (operationSystem) {
            case WINDOWS:
                return "windows-x86_64";
            case LINUX:
                switch (architecture) {
                    case AMD64:
                        return "linux-x86_64";
                    case AARCH64:
                        return "osx-aarch_64";
                }
                break;
            case MAC:
                switch (architecture) {
                    case AMD64:
                        return "osx-x86_64";
                    case AARCH64:
                        return "osx-aarch_64";
                }
                break;
        }
        getLog().error("Unsupported OS");
        throw new RuntimeException("Unsupported OS or architecture. Please specific os.identifier parameter for proto-java-plugin");
    }
}
