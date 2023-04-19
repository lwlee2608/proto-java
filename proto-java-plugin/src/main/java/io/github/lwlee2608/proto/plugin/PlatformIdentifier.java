package io.github.lwlee2608.proto.plugin;

public class PlatformIdentifier {
    public enum OperationSystem {
        LINUX, WINDOWS, MAC, OTHERS
    }

    public enum Architecture {
        AMD64, AARCH64, OTHERS
    }

    private static String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static String OS_ARCH = System.getProperty("os.arch");

    public static OperationSystem getOperationSystem() {
        if (OS_NAME.contains("linux")) {
            return OperationSystem.LINUX;
        } else if (OS_NAME.contains("win")) {
            return OperationSystem.WINDOWS;
        } else if (OS_NAME.contains("mac")) {
            return OperationSystem.MAC;
        }

        return OperationSystem.OTHERS;
    }

    public static Architecture getArchitecture() {
        switch (OS_ARCH) {
            case "amd64":
            case "x86_64":
                return Architecture.AMD64;
            case "aarch64": return Architecture.AARCH64;
            default: return Architecture.OTHERS;
        }
    }
}
