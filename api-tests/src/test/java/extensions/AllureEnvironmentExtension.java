package extensions;

import core.config.ConfigReader;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AllureEnvironmentExtension implements BeforeAllCallback {

    private static final Logger log = LoggerFactory.getLogger(AllureEnvironmentExtension.class);

    @Override
    public void beforeAll(ExtensionContext context) {
        String resultsDirPath = System.getProperty("allure.results.directory", "target/allure-results");
        File resultsDir = new File(resultsDirPath);

        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
        }

        File envFile = new File(resultsDir, "environment.properties");
        if (envFile.exists()) {
            return;
        }

        Properties props = new Properties();
        props.setProperty("Service URL", ConfigReader.getProperty("service.base.url"));
        props.setProperty("Service Api-key", ConfigReader.getProperty("service.api.key"));
        props.setProperty("External Mock Port", ConfigReader.getProperty("mock.service.port"));
        props.setProperty("Java Version", System.getProperty("java.version"));
        props.setProperty("OS", System.getProperty("os.name"));

        try (FileOutputStream fos = new FileOutputStream(envFile)) {
            props.store(fos, "Allure Environment Properties");
            log.info("[Allure] environment.properties created at: {}", envFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("[Allure] Failed to create environment.properties: {}", e.getMessage());
        }
    }
}
