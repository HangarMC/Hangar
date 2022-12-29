package io.papermc.hangar.service.internal.versions.plugindata;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.PaperFileTypeHandler;
import io.papermc.hangar.service.internal.versions.plugindata.handler.VelocityFileTypeHandler;
import io.papermc.hangar.service.internal.versions.plugindata.handler.WaterfallFileTypeHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PluginDataService.class, PaperFileTypeHandler.class, WaterfallFileTypeHandler.class, VelocityFileTypeHandler.class})
class PluginDataServiceTest {

    private static final Path path = Path.of("src/test/resources/io/papermc/hangar/service/internal/versions/plugindata");

    @Autowired
    private PluginDataService classUnderTest;

    @Test
    void testLoadPaperPluginMetadata() throws Exception {
        final PluginFileData data = this.classUnderTest.loadMeta("Paper.jar", Files.newInputStream(path.resolve("Paper.jar")).readAllBytes(), -1).data();
        data.validate();
        assertEquals("Maintenance", data.getName());
        assertEquals("Enable maintenance mode with a custom maintenance motd and icon.", data.getDescription());
        assertEquals("3.0.5", data.getVersion());
        assertIterableEquals(List.of("KennyTV"), data.getAuthors().get(Platform.PAPER));

        final Set<PluginDependency> deps = data.getDependencies().get(Platform.PAPER);
        assertThat(deps).hasSize(3);
        assertThat(deps).extracting(PluginDependency::getName).containsExactlyInAnyOrder("ProtocolLib", "ServerListPlus", "ProtocolSupport");
        assertThat(deps).extracting(PluginDependency::isRequired).containsOnly(false);

        assertIterableEquals(Set.of("1.13"), data.getPlatformDependencies().get(Platform.PAPER));
    }

    @Test
    void testLoadWaterfallPluginMetadata() throws Exception {
        final PluginFileData data = this.classUnderTest.loadMeta("Waterfall.jar", Files.newInputStream(path.resolve("Waterfall.jar")).readAllBytes(), -1).data();

        data.validate();
        assertEquals("Maintenance", data.getName());
        assertEquals("Enable maintenance mode with a custom maintenance motd and icon.", data.getDescription());
        assertEquals("3.0.5", data.getVersion());
        assertIterableEquals(List.of("KennyTV"), data.getAuthors().get(Platform.WATERFALL));

        final Set<PluginDependency> deps = data.getDependencies().get(Platform.WATERFALL);
        assertThat(deps)
            .hasSize(2)
            .anyMatch(pd -> pd.getName().equals("ServerListPlus") && !pd.isRequired())
            .anyMatch(pd -> pd.getName().equals("SomePlugin") && pd.isRequired());
    }

    @Test
    void testLoadVelocityPluginMetadata() throws Exception {
        final PluginFileData data = this.classUnderTest.loadMeta("Velocity.jar", Files.newInputStream(path.resolve("Velocity.jar")).readAllBytes(), -1).data();

        data.validate();
        assertEquals("Maintenance", data.getName());
        assertEquals("Enable maintenance mode with a custom maintenance motd and icon.", data.getDescription());
        assertEquals("3.0.5", data.getVersion());
        assertIterableEquals(List.of("KennyTV"), data.getAuthors().get(Platform.VELOCITY));

        final Set<PluginDependency> deps = data.getDependencies().get(Platform.VELOCITY);
        assertThat(deps)
            .hasSize(1)
            .anyMatch(pd -> pd.getName().equals("serverlistplus") && !pd.isRequired());
    }

    @Test
    void testLoadPaperPluginZipMetadata() throws Exception {
        final PluginFileData data = this.classUnderTest.loadMeta("TestZip.zip", Files.newInputStream(path.resolve("TestZip.zip")).readAllBytes(), -1).data();

        data.validate();
        assertEquals("Maintenance", data.getName());
        assertEquals("Enable maintenance mode with a custom maintenance motd and icon.", data.getDescription());
        assertEquals("3.0.5", data.getVersion());
        assertIterableEquals(List.of("KennyTV"), data.getAuthors().get(Platform.PAPER));

        final Set<PluginDependency> deps = data.getDependencies().get(Platform.PAPER);
        assertThat(deps).hasSize(3);
        assertThat(deps).extracting(PluginDependency::getName).containsExactlyInAnyOrder("ProtocolLib", "ServerListPlus", "ProtocolSupport");
        assertThat(deps).extracting(PluginDependency::isRequired).containsOnly(false);

        assertIterableEquals(Set.of("1.13"), data.getPlatformDependencies().get(Platform.PAPER));
    }

    @ParameterizedTest
    @CsvSource({
        "EmptyMeta.jar,version.new.error.metaNotFound",
        "Empty.zip,version.new.error.jarNotFound"
    })
    void testLoadMetaShouldFail(final String jarName, final String expectedMsg) {
        final Path jarPath = path.resolve(jarName);
        final HangarApiException exception = assertThrows(HangarApiException.class, () -> {
            this.classUnderTest.loadMeta(jarName, Files.newInputStream(jarPath).readAllBytes(), -1);
        });
        assertEquals("400 BAD_REQUEST \"" + expectedMsg + "\"", exception.getMessage());
    }
}
