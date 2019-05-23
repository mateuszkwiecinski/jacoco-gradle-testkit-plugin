package pl.droidsonroids.gradle.jacoco.testkit

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pl.droidsonroids.gradle.jacoco.testkit.Tasks.generateJacocoTestKitProperties
import java.io.File
import java.util.Properties

class JaCoCoTestKitPluginFunctionalTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Before
    fun setUp() {
        temporaryFolder.newFile("gradle.properties").fillFromResource("testkit-gradle.properties")
    }

    @Test
    fun `gradle properties file generated with default version`() {
        temporaryFolder.newFile("build.gradle").fillFromResource("simple.gradle")
        GradleRunner.create()
                .withProjectDir(temporaryFolder.root)
                .withTestKitDir(temporaryFolder.newFolder())
                .withArguments(generateJacocoTestKitProperties)
                .withPluginClasspath()
                .build()

        val args = readArgsFromProperties()
        assertThat(args)
                .startsWith("\"-javaagent:")
                .contains("=destfile=")
                .contains(JacocoPlugin.DEFAULT_JACOCO_VERSION)
                .endsWith("test.exec\"")
    }

    @Test
    fun `gradle properties file generated with explicit version`() {
        temporaryFolder.newFile("build.gradle").fillFromResource("extension.gradle")
        GradleRunner.create()
                .withProjectDir(temporaryFolder.root)
                .withTestKitDir(temporaryFolder.newFolder())
                .withArguments(generateJacocoTestKitProperties)
                .withPluginClasspath()
                .build()

        val args = readArgsFromProperties()
        assertThat(args)
                .startsWith("\"-javaagent:")
                .contains("=destfile=")
                .contains("0.7.7.201606060606")
                .endsWith(".exec\"")
    }

    @Test
    fun `plugin compatible with Gradle older than 3_4`() {
        temporaryFolder.newFile("build.gradle").fillFromResource("simple.gradle")
        GradleRunner.create()
                .withGradleVersion("3.3")
                .withProjectDir(temporaryFolder.root)
                .withTestKitDir(temporaryFolder.newFolder())
                .withPluginClasspath()
                .build()
    }

    @Test
    fun `gradle properties file generated with custom destination file`() {
        temporaryFolder.newFile("build.gradle").fillFromResource("custom-destination.gradle")
        GradleRunner.create()
                .withProjectDir(temporaryFolder.root)
                .withTestKitDir(temporaryFolder.newFolder())
                .withArguments("generateJacocoIntegrationTestKitProperties")
                .withPluginClasspath()
                .build()

        val args = readArgsFromProperties()
        assertThat(args)
                .endsWith("integration.exec\"")
    }

    private fun readArgsFromProperties(): String {
        val propertiesFile = File(temporaryFolder.root, "build/testkit/testkit-gradle.properties")
        val properties = Properties()
        propertiesFile.inputStream().use { inputStream ->
            properties.load(inputStream)
        }
        return properties.getProperty("org.gradle.jvmargs")
    }
}