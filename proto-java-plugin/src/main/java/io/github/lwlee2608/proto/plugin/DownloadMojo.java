package io.github.lwlee2608.proto.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Mojo(name = "download")
public class DownloadMojo extends AbstractMojo {
    @Parameter(
            name = "outputDirectory",
            property = "output.dir",
            defaultValue = "${project.build.directory}/protoc"
    )
    private File outputDirectory;

    @Parameter(
            name = "protocVersion",
            property = "protoc.version",
            defaultValue = "3.4.0"
    )
    private String protocVersion;

    @Parameter(
            name = "os",
            property = "os.identifier",
            defaultValue = "linux-x86_64"
    )
    private String os;

    public void execute() {
        getLog().info("Starting download protoc binary");
        // Download Binary
        String downloadUrl = "https://github.com/protocolbuffers/protobuf/releases/download/v" + protocVersion + "/protoc-" + protocVersion + "-" + os + ".zip";

        try {
            downloadAndUnzip(downloadUrl, outputDirectory.getAbsolutePath());
        } catch (Exception e) {
            getLog().error("Fail to download protoc binary with URL: " + downloadUrl);
            throw new RuntimeException(e);
        }

        // Make binary executable
        try {
            ProcessBuilder pb = new ProcessBuilder("chmod", "+x", outputDirectory.getAbsolutePath() + "/bin/protoc");

            Process p = pb.start();

            int exitCode = p.waitFor();

            if (exitCode != 0) {
                System.err.println("Error: chmod exited with code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            getLog().error("Fail to make binary executable");
            throw new RuntimeException(e);
        }
        getLog().info("Proto Java plugin completed");
    }

    private void downloadAndUnzip(String downloadUrl, String outputDirectory) throws Exception {
        getLog().info("Starting download of " + downloadUrl + " to " + outputDirectory);
        URL url = new URL(downloadUrl);
        InputStream in = url.openStream();
        ZipInputStream zipIn = new ZipInputStream(in);
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
            String filePath = outputDirectory + "/" + entry.getName();
            if (!entry.isDirectory()) {
                FileOutputStream out = new FileOutputStream(filePath);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = zipIn.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                out.close();
            } else {
                new File(filePath).mkdirs();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        in.close();
        getLog().info("Download completed");
    }
}
