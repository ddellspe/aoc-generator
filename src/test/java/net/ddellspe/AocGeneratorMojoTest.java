package net.ddellspe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AocGeneratorMojoTest {
  int day = -1;
  String resourceRoot = "";

  @BeforeEach
  public void before() {
    day =
        Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("America/New_York")))
            .get(Calendar.DAY_OF_MONTH);
    File rootFile = new File(Objects.requireNonNull(getClass().getResource("/root.txt")).getFile());
    resourceRoot = rootFile.getParent();
  }

  @AfterEach
  public void after() {
    File resourceDirectory = new File(resourceRoot);
    File[] resourceFiles = resourceDirectory.listFiles();
    if (resourceFiles != null) {
      Arrays.stream(Objects.requireNonNull(resourceFiles))
          .sequential()
          .filter(file -> file.getName().equals("project"))
          .filter(File::isDirectory)
          .forEach(
              file -> {
                try {
                  FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              });
    }
  }

  @Test
  public void testDefaults() {
    AocGeneratorMojo mojo = new AocGeneratorMojo();
    assertFalse(mojo.isForce());
    assertTrue(mojo.isUseDayPackage());
    assertEquals(-1, mojo.getDay());
    assertNull(mojo.getProject());
  }

  @Test
  public void testDefaultsAllWritesNoDaySpecified() {
    MavenProject mockProject = Mockito.mock(MavenProject.class);
    Build mockBuild = Mockito.mock(Build.class);
    Log mockLog = Mockito.mock(Log.class);
    Resource mockResource = Mockito.mock(Resource.class);
    List<Resource> resources = new ArrayList<>();
    resources.add(mockResource);
    AocGeneratorMojo mojo = new AocGeneratorMojo();
    mojo.setProject(mockProject);
    mojo.setLog(mockLog);
    mojo.setUseDayPackage(false);
    String cls = String.format("Day%02d", day);
    String msg = String.format("Day %02d", day);

    when(mockProject.getGroupId()).thenReturn("project.ignored");
    when(mockProject.getBuild()).thenReturn(mockBuild);
    when(mockBuild.getSourceDirectory()).thenReturn(resourceRoot);
    when(mockBuild.getTestSourceDirectory()).thenReturn(resourceRoot);
    when(mockResource.getDirectory()).thenReturn(resourceRoot);
    when(mockBuild.getResources()).thenReturn(resources);
    when(mockBuild.getTestResources()).thenReturn(resources);

    try {
      mojo.execute();
    } catch (MojoExecutionException | MojoFailureException e) {
      fail();
    }
    verify(mockLog, times(1)).info("Generating Advent of Code files for " + msg);
    verify(mockLog, times(1)).info("Writing files to package: project.ignored");
    File sourceFile = Paths.get(resourceRoot, "project", "ignored", cls + ".java").toFile();
    assertTrue(sourceFile.isFile());
    File testSourceFile = Paths.get(resourceRoot, "project", "ignored", cls + "Test.java").toFile();
    assertTrue(testSourceFile.isFile());
    File inputFile = Paths.get(resourceRoot, "project", "ignored", "input.txt").toFile();
    assertTrue(inputFile.isFile());
    File exampleFile = Paths.get(resourceRoot, "project", "ignored", "example.txt").toFile();
    assertTrue(exampleFile.isFile());
  }

  @Test
  public void testDefaultsAllWritesDayZero() {
    MavenProject mockProject = Mockito.mock(MavenProject.class);
    Build mockBuild = Mockito.mock(Build.class);
    Log mockLog = Mockito.mock(Log.class);
    Resource mockResource = Mockito.mock(Resource.class);
    List<Resource> resources = new ArrayList<>();
    resources.add(mockResource);
    AocGeneratorMojo mojo = new AocGeneratorMojo(mockProject, 0, true, true);
    mojo.setLog(mockLog);
    File sourceFile = Paths.get(resourceRoot, "project", "ignored", "day00", "Day00.java").toFile();
    File testSourceFile =
        Paths.get(resourceRoot, "project", "ignored", "day00", "Day00Test.java").toFile();
    File inputFile = Paths.get(resourceRoot, "project", "ignored", "day00", "input.txt").toFile();
    File exampleFile =
        Paths.get(resourceRoot, "project", "ignored", "day00", "example.txt").toFile();

    when(mockProject.getGroupId()).thenReturn("project.ignored");
    when(mockProject.getBuild()).thenReturn(mockBuild);
    when(mockBuild.getSourceDirectory()).thenReturn(resourceRoot);
    when(mockBuild.getTestSourceDirectory()).thenReturn(resourceRoot);
    when(mockResource.getDirectory()).thenReturn(resourceRoot);
    when(mockBuild.getResources()).thenReturn(resources);
    when(mockBuild.getTestResources()).thenReturn(resources);

    try {
      mojo.execute();
    } catch (MojoExecutionException | MojoFailureException e) {
      fail();
    }
    verify(mockLog, times(1)).info("Generating Advent of Code files for Day 00");
    verify(mockLog, times(1)).info("Writing files to package: project.ignored.day00");
    assertTrue(sourceFile.isFile());
    assertTrue(testSourceFile.isFile());
    assertTrue(inputFile.isFile());
    assertTrue(exampleFile.isFile());
  }

  @Test
  public void testDefaultsAllWritesDayZeroWriteException() {
    MavenProject mockProject = Mockito.mock(MavenProject.class);
    Build mockBuild = Mockito.mock(Build.class);
    Log mockLog = Mockito.mock(Log.class);
    Resource mockResource = Mockito.mock(Resource.class);
    List<Resource> resources = new ArrayList<>();
    resources.add(mockResource);
    AocGeneratorMojo mojo = new AocGeneratorMojo(mockProject, 0, true, true);
    mojo.setLog(mockLog);

    File sourceFile = Paths.get(resourceRoot, "project", "ignored", "day00", "Day00.java").toFile();
    File testSourceFile =
        Paths.get(resourceRoot, "project", "ignored", "day00", "Day00Test.java").toFile();
    File inputFile = Paths.get(resourceRoot, "project", "ignored", "day00", "input.txt").toFile();
    File exampleFile =
        Paths.get(resourceRoot, "project", "ignored", "day00", "example.txt").toFile();
    sourceFile.mkdirs();
    testSourceFile.mkdirs();
    inputFile.mkdirs();
    exampleFile.mkdirs();

    when(mockProject.getGroupId()).thenReturn("project.ignored");
    when(mockProject.getBuild()).thenReturn(mockBuild);
    when(mockBuild.getSourceDirectory()).thenReturn(resourceRoot);
    when(mockBuild.getTestSourceDirectory()).thenReturn(resourceRoot);
    when(mockResource.getDirectory()).thenReturn(resourceRoot);
    when(mockBuild.getResources()).thenReturn(resources);
    when(mockBuild.getTestResources()).thenReturn(resources);

    try {
      mojo.execute();
    } catch (MojoExecutionException | MojoFailureException e) {
      fail();
    }
    verify(mockLog, times(1)).info("Generating Advent of Code files for Day 00");
    verify(mockLog, times(1)).info("Writing files to package: project.ignored.day00");
    verify(mockLog, times(1)).error("Unable to create new java file: Day00.java");
    verify(mockLog, times(1)).error("Unable to create new java test file: Day00Test.java");
    verify(mockLog, times(1))
        .warn("Failed to create input file for tests at: " + inputFile.getPath());
    verify(mockLog, times(1))
        .warn("Failed to create example file for tests at: " + exampleFile.getPath());
    assertTrue(sourceFile.isDirectory());
    assertTrue(testSourceFile.isDirectory());
    assertTrue(inputFile.isDirectory());
    assertTrue(exampleFile.isDirectory());
  }

  @Test
  public void testDefaultsAllWritesDayZeroNoForceWriteException() {
    MavenProject mockProject = Mockito.mock(MavenProject.class);
    Build mockBuild = Mockito.mock(Build.class);
    Log mockLog = Mockito.mock(Log.class);
    Resource mockResource = Mockito.mock(Resource.class);
    List<Resource> resources = new ArrayList<>();
    resources.add(mockResource);
    AocGeneratorMojo mojo = new AocGeneratorMojo(mockProject, 0, true, true);
    mojo.setDay(0);
    mojo.setForce(false);
    mojo.setLog(mockLog);

    File sourceFile = Paths.get(resourceRoot, "project", "ignored", "day00", "Day00.java").toFile();
    File testSourceFile =
        Paths.get(resourceRoot, "project", "ignored", "day00", "Day00Test.java").toFile();
    File inputFile = Paths.get(resourceRoot, "project", "ignored", "day00", "input.txt").toFile();
    File exampleFile =
        Paths.get(resourceRoot, "project", "ignored", "day00", "example.txt").toFile();
    sourceFile.mkdirs();
    testSourceFile.mkdirs();
    inputFile.mkdirs();
    exampleFile.mkdirs();

    when(mockProject.getGroupId()).thenReturn("project.ignored");
    when(mockProject.getBuild()).thenReturn(mockBuild);
    when(mockBuild.getSourceDirectory()).thenReturn(resourceRoot);
    when(mockBuild.getTestSourceDirectory()).thenReturn(resourceRoot);
    when(mockResource.getDirectory()).thenReturn(resourceRoot);
    when(mockBuild.getResources()).thenReturn(resources);
    when(mockBuild.getTestResources()).thenReturn(resources);

    try {
      mojo.execute();
    } catch (MojoExecutionException | MojoFailureException e) {
      fail();
    }
    verify(mockLog, times(1)).info("Generating Advent of Code files for Day 00");
    verify(mockLog, times(1)).info("Writing files to package: project.ignored.day00");
    verify(mockLog, times(1))
        .info("Source file already exists at: " + sourceFile.getPath() + ". Skipping creation");
    verify(mockLog, times(1))
        .info(
            "Test source file already exists at: "
                + testSourceFile.getPath()
                + ". Skipping creation");
    verify(mockLog, times(1))
        .info("Input file already exists at: " + inputFile.getPath() + ". Skipping creation");
    verify(mockLog, times(0))
        .warn("Example file already exists at: " + exampleFile.getPath() + ". Skipping creation");
    assertTrue(sourceFile.isDirectory());
    assertTrue(testSourceFile.isDirectory());
    assertTrue(inputFile.isDirectory());
    assertTrue(exampleFile.isDirectory());
  }
}
