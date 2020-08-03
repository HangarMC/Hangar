package me.minidigger.hangar.service.pluginupload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import me.minidigger.hangar.config.hangar.HangarConfig;

@Component
public class ProjectFiles {

    private Path rootDir;
    private Path publicDir;
    private Path confDir;
    private Path uploadsDir;
    private Path pluginsDir;
    private Path tmpDir;

    @Autowired
    public ProjectFiles(HangarConfig hangarConfig) {
        rootDir = Path.of("");
        publicDir = rootDir.resolve("public");
        confDir = rootDir.resolve("conf");
        uploadsDir = Path.of(hangarConfig.getPluginUploadDir());
        pluginsDir = uploadsDir.resolve("plugins");
        tmpDir = uploadsDir.resolve("tmp");
    }

    public Path getProjectDir(String owner, String name) {
        return getUserDir(owner).resolve(name);
    }

    public Path getVersionDir(String owner, String name, String version) {
        return getProjectDir(owner, name).resolve("versions").resolve(version);
    }

    public Path getUserDir(String user) {
        return pluginsDir.resolve(user);
    }

    public void renameProject(String owner, String oldName, String newName) {
        Path newProjectDir = getProjectDir(owner, newName);
        Path oldProjectDir = getProjectDir(owner, oldName);

        try {
            Files.move(oldProjectDir, newProjectDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Path getIconsDir(String owner, String name) {
        return getProjectDir(owner, name).resolve("icons");
    }

    public Path getIconDir(String owner, String name) {
        return getIconsDir(owner, name).resolve("icon");
    }

    public Path getIconPath(String owner, String name) {
        return findFirstFile(getIconDir(owner, name));
    }

    public Path getPendingIconDir(String owner, String name) {
        return getIconDir(owner, name).resolve("pending");
    }

    public Path getPendingIconPath(String owner, String name) {
        return findFirstFile(getPendingIconDir(owner, name));
    }

    public Path getTempDir(String owner) {
        return tmpDir.resolve(owner);
    }

    private Path findFirstFile(Path dir) {
        try {
            return Files.list(dir).filter(Files::isRegularFile).findFirst().orElse(null); // TODO no clue if this is what is desired here
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        //     private def findFirstFile(dir: Path): F[Option[Path]] = {
        //      import cats.instances.lazyList._
        //      val findFirst = fileIO.list(dir).use { fs =>
        //        fileIO.traverseLimited(fs)(f => fileIO.isDirectory(f).tupleLeft(f)).map {
        //          _.collectFirst {
        //            case (p, false) => p
        //          }
        //        }
        //      }
        //
        //      fileIO.exists(dir).ifM(findFirst, F.pure(None))
        //    }
        //  }
    }
}
