package com.marginallyclever.robotoverlord;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/**
 * Used to confirm results generated by {@link Path} and {@link File}.
 */
public class PathUtilsTest {
    @Test
    public void testSetWorkingDirectory() {
        String before = PathUtils.getCurrentWorkingDirectory();
        PathUtils.setCurrentWorkingDirectory(PathUtils.APP_BASE);
        String after = PathUtils.getCurrentWorkingDirectory();
        Assertions.assertNotEquals(before, after);
    }

    @Test
    public void test() {
        try {
            testResults("src\\main\\java\\com\\marginallyclever\\robotoverlord\\swinginterface\\actions\\SceneLoadAction.java");
            testResults("\\src\\main\\java\\com\\marginallyclever\\robotoverlord\\swinginterface\\actions\\");
            testResults("src\\main\\java\\com\\marginallyclever\\robotoverlord\\swinginterface\\actions");
        } catch (Exception ignored) {
            // ignored.
        }
    }

    private void testResults(String address) throws IOException {
        System.out.println("Address: " + address);
        File file = new File(address);
        System.out.println("  file.exists = "+file.exists());
        System.out.println("  file.isDirectory = "+file.isDirectory());

        Path path = Path.of(address);
        System.out.println("  path.toString = "+path.toString());
        System.out.println("  path.filename = "+path.getFileName());
        System.out.println("  path.parent = "+path.getParent());
        System.out.println("  path.root = "+path.getRoot());
        System.out.println("  path.nameCount = "+path.getNameCount());
        System.out.println("  path.isAbsolute = "+(path.isAbsolute()?"true":"false"));
        System.out.println("  path.toAbsolutePath = "+path.toAbsolutePath());
        System.out.println("  path.toRealPath = "+path.toRealPath());
        System.out.println("  path.toRealPath(nofollow) = "+path.toRealPath(LinkOption.NOFOLLOW_LINKS));
    }
}
