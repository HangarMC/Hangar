package io.papermc.hangar.service.plugindata;

import io.papermc.hangar.exceptions.HangarException;
import io.papermc.hangar.service.plugindata.handler.PaperPluginFileHandler;
import io.papermc.hangar.service.plugindata.handler.VelocityFileHandler;
import io.papermc.hangar.service.plugindata.handler.WaterfallPluginFileHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PluginDataService.class, PaperPluginFileHandler.class, VelocityFileHandler.class, WaterfallPluginFileHandler.class})
class PluginDataServiceTest {

    private static final Path path = Path.of("src/test/resources/io/papermc/hangar/service/plugindata");

    @Autowired
    private PluginDataService classUnderTest;

    @Test
    void test_paper_happyDay() throws Exception {
        PluginFileData data = classUnderTest.loadMeta(path.resolve("Paper.jar"), -1).getData();

        assertTrue(data.validate());
        assertEquals("Maintenance", data.getName());
        assertEquals("Enable maintenance mode with a custom maintenance motd and icon.", data.getDescription());
        assertEquals("3.0.5", data.getVersion());
        assertEquals("https://www.spigotmc.org/resources/maintenance.40699/", data.getWebsite());
        assertEquals(List.of("KennyTV"), data.getAuthors());
//        assertEquals(4, data.getDependencies().size());
//        assertEquals("ProtocolLib", data.getDependencies().get(0).getPluginId());
//        assertEquals("ServerListPlus", data.getDependencies().get(1).getPluginId());
//        assertEquals("ProtocolSupport", data.getDependencies().get(2).getPluginId());
//        assertEquals("paperapi", data.getDependencies().get(3).getPluginId());
//        assertEquals("1.13", data.getDependencies().get(3).getVersion());
    }

    @Test
    void test_waterfall_happyDay() throws Exception {
        PluginFileData data = classUnderTest.loadMeta(path.resolve("Waterfall.jar"), -1).getData();

        assertTrue(data.validate());
        assertEquals("Maintenance", data.getName());
        assertEquals("Enable maintenance mode with a custom maintenance motd and icon.", data.getDescription());
        assertEquals("3.0.5", data.getVersion());
        assertEquals("https://www.spigotmc.org/resources/maintenance.40699/", data.getWebsite());
        assertEquals(List.of("KennyTV"), data.getAuthors());
//        assertEquals(1, data.getDependencies().size());
//        assertEquals("waterfall", data.getDependencies().get(0).getPluginId());
    }

    @Test
    void test_velocity_happyDay() throws Exception {
        PluginFileData data = classUnderTest.loadMeta(path.resolve("Velocity.jar"), -1).getData();

        assertTrue(data.validate());
        assertEquals("Maintenance", data.getName());
        assertEquals("Enable maintenance mode with a custom maintenance motd and icon.", data.getDescription());
        assertEquals("3.0.5", data.getVersion());
        assertEquals("https://forums.velocitypowered.com/t/maintenance/129", data.getWebsite());
        assertEquals(List.of("KennyTV"), data.getAuthors());
//        assertEquals(2, data.getDependencies().size());
//        assertEquals("serverlistplus", data.getDependencies().get(0).getPluginId());
//        assertEquals(false, data.getDependencies().get(0).isRequired());
//        assertEquals("velocity", data.getDependencies().get(1).getPluginId());
    }

    @Test
    void test_emptyMeta_shouldFail() {
        HangarException hangarException = assertThrows(HangarException.class, () -> {
            classUnderTest.loadMeta(path.resolve("EmptyMeta.jar"), -1);
        });
        assertEquals("error.plugin.metaNotFound", hangarException.getMessageKey());
    }

    @Test
    void test_noMeta_shouldFail() {
        HangarException hangarException = assertThrows(HangarException.class, () -> {
            classUnderTest.loadMeta(path.resolve("Empty.jar"), -1);
        });

        assertEquals("error.plugin.metaNotFound", hangarException.getMessageKey());
    }

    @Test
    void test_incompleteMeta_shouldFail() {
        HangarException hangarException = assertThrows(HangarException.class, () -> {
            classUnderTest.loadMeta(path.resolve("IncompleteMeta.jar"), -1);
        });

        assertEquals("error.plugin.incomplete", hangarException.getMessageKey());
    }

    @Test
    void test_zip_happyDay() throws Exception {
        PluginFileData data = classUnderTest.loadMeta(path.resolve("TestZip.zip"), -1).getData();

        assertTrue(data.validate());
        assertEquals("Maintenance", data.getName());
        assertEquals("Enable maintenance mode with a custom maintenance motd and icon.", data.getDescription());
        assertEquals("3.0.5", data.getVersion());
        assertEquals("https://www.spigotmc.org/resources/maintenance.40699/", data.getWebsite());
        assertEquals(List.of("KennyTV"), data.getAuthors());
//        assertEquals(4, data.getDependencies().size());
//        assertEquals("ProtocolLib", data.getDependencies().get(0).getPluginId());
//        assertEquals("ServerListPlus", data.getDependencies().get(1).getPluginId());
//        assertEquals("ProtocolSupport", data.getDependencies().get(2).getPluginId());
//        assertEquals("paperapi", data.getDependencies().get(3).getPluginId());
//        assertEquals("1.13", data.getDependencies().get(3).getVersion());
    }

    @Test
    void test_zipNoJar_shouldFail() {
        HangarException hangarException = assertThrows(HangarException.class, () -> {
            classUnderTest.loadMeta(path.resolve("Empty.zip"), -1);
        });
        assertEquals("error.plugin.jarNotFound", hangarException.getMessageKey());
    }
}
