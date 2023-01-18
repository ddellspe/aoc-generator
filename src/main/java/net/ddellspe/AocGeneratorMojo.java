package net.ddellspe;

import com.google.common.annotations.VisibleForTesting;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import javax.lang.model.element.Modifier;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Goal which generates the files necessary for an Advent of Code Run using a package per day, and
 * the current day of the month (in US eastern time) as the default structure for new files with
 * "DayXX" as the default class/test class name.
 */
@Mojo(name = "generate-day", defaultPhase = LifecyclePhase.NONE)
public class AocGeneratorMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project = null;

  @Parameter(defaultValue = "-1", property = "day", readonly = true)
  private int day = -1;

  @Parameter(defaultValue = "false", property = "force", readonly = true)
  private boolean force = false;

  @Parameter(defaultValue = "true", property = "useDayPackage", readonly = true)
  private boolean useDayPackage = true;

  public AocGeneratorMojo() {}

  @VisibleForTesting
  protected AocGeneratorMojo(MavenProject project, int day, boolean force, boolean useDayPackage) {
    this.project = project;
    this.day = day;
    this.force = force;
    this.useDayPackage = useDayPackage;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (day < 0) {
      // puzzles are released in the Eastern Timezone of the US, so that's what we default to
      day =
          Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("America/New_York")))
              .get(Calendar.DAY_OF_MONTH);
    }
    getLog().info(String.format("Generating Advent of Code files for Day %02d", day));
    String fullPkg = project.getGroupId();
    if (useDayPackage) {
      fullPkg += String.format(".day%02d", day);
    }
    getLog().info(String.format("Writing files to package: %s", fullPkg));
    String cls = String.format("Day%02d", day);
    String path =
        Arrays.stream(fullPkg.split("\\."))
            .map(File::new)
            .reduce(
                new File(""),
                (prev, cur) -> new File(String.valueOf(Paths.get(prev.getPath(), cur.getPath()))))
            .getPath();
    List<MethodSpec> methods = new ArrayList<>();
    methods.add(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());
    methods.add(
        MethodSpec.methodBuilder("part1")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(long.class)
            .addParameter(String.class, "filename")
            .addStatement(
                "$T<String> lines = $T.stringPerLine(filename, " + cls + ".class)",
                List.class,
                ClassName.get(project.getGroupId() + ".utils", "InputUtils"))
            .addStatement("return 0L")
            .build());
    methods.add(
        MethodSpec.methodBuilder("part2")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(long.class)
            .addParameter(String.class, "filename")
            .addStatement(
                "$T<String> lines = $T.stringPerLine(filename, " + cls + ".class)",
                List.class,
                ClassName.get(project.getGroupId() + ".utils", "InputUtils"))
            .addStatement("return 0L")
            .build());
    TypeSpec dayClass =
        TypeSpec.classBuilder(cls).addModifiers(Modifier.PUBLIC).addMethods(methods).build();
    JavaFile sourceFile = JavaFile.builder(fullPkg, dayClass).skipJavaLangImports(true).build();
    try {
      File srcFile =
          Paths.get(project.getBuild().getSourceDirectory(), path, cls + ".java").toFile();
      if (!srcFile.exists() || (srcFile.exists() && force)) {
        sourceFile.writeToFile(new File(project.getBuild().getSourceDirectory()));
      } else {
        getLog()
            .info("Source file already exists at: " + srcFile.getPath() + ". Skipping creation");
      }
    } catch (IOException e) {
      getLog().error("Unable to create new java file: " + cls + ".java");
    }
    methods.clear();
    methods.add(MethodSpec.constructorBuilder().build());
    methods.add(
        MethodSpec.methodBuilder("providedInputTestPart1")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addAnnotation(ClassName.get("org.junit.jupiter.api", "Test"))
            .addStatement("assertEquals(0L, " + cls + ".part1($S))", "example.txt")
            .build());
    methods.add(
        MethodSpec.methodBuilder("solutionPart1")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addAnnotation(ClassName.get("org.junit.jupiter.api", "Test"))
            .addStatement(
                "$T.out.println($S + " + cls + ".part1($S))",
                System.class,
                String.format("Day %02d Part 1 Answer is: ", day),
                "input.txt")
            .build());
    methods.add(
        MethodSpec.methodBuilder("providedInputTestPart2")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addAnnotation(ClassName.get("org.junit.jupiter.api", "Test"))
            .addStatement("assertEquals(0L, " + cls + ".part2($S))", "example.txt")
            .build());
    methods.add(
        MethodSpec.methodBuilder("solutionPart2")
            .addModifiers(Modifier.PUBLIC)
            .returns(void.class)
            .addAnnotation(ClassName.get("org.junit.jupiter.api", "Test"))
            .addStatement(
                "$T.out.println($S + " + cls + ".part2($S))",
                System.class,
                String.format("Day %02d Part 2 Answer is: ", day),
                "input.txt")
            .build());
    TypeSpec dayTestClass =
        TypeSpec.classBuilder(cls + "Test")
            .addModifiers(Modifier.PUBLIC)
            .addMethods(methods)
            .build();
    JavaFile testFile =
        JavaFile.builder(fullPkg, dayTestClass)
            .skipJavaLangImports(true)
            .addStaticImport(ClassName.get("org.junit.jupiter.api", "Assertions"), "assertEquals")
            .build();
    try {
      File tstFile =
          Paths.get(project.getBuild().getSourceDirectory(), path, cls + "Test.java").toFile();
      if (!tstFile.exists() || (tstFile.exists() && force)) {
        sourceFile.writeToFile(new File(project.getBuild().getSourceDirectory()));
      } else {
        getLog()
            .info(
                "Test source file already exists at: " + tstFile.getPath() + ". Skipping creation");
      }
      testFile.writeToFile(new File(project.getBuild().getTestSourceDirectory()));
    } catch (IOException e) {
      getLog().error("Unable to create new java test file: " + cls + "Test.java");
    }
    File inputFile =
        Paths.get(project.getBuild().getResources().get(0).getDirectory(), path, "input.txt")
            .toFile();
    if (!inputFile.exists() || (inputFile.exists() && force)) {
      try {
        if ((inputFile.getParentFile().isDirectory() || inputFile.getParentFile().mkdirs())
            && !inputFile.createNewFile()) {
          getLog().warn("Failed to create input file for tests at: " + inputFile.getPath());
        }
      } catch (IOException e) {
        getLog().error("Unable to create new input file for tests at: " + inputFile.getPath());
      }
    } else {
      getLog().info("Input file already exists at: " + inputFile.getPath() + ". Skipping creation");
    }
    File exampleFile =
        Paths.get(project.getBuild().getTestResources().get(0).getDirectory(), path, "example.txt")
            .toFile();
    if (!exampleFile.exists() || (exampleFile.exists() && force)) {
      try {
        if ((exampleFile.getParentFile().isDirectory() || exampleFile.getParentFile().mkdirs())
            && !exampleFile.createNewFile()) {
          getLog().warn("Failed to create example file for tests at: " + exampleFile.getPath());
        }
      } catch (IOException e) {
        getLog().error("Unable to create new example file for tests at: " + exampleFile.getPath());
      }
    } else {
      getLog()
          .info("Example file already exists at: " + exampleFile.getPath() + ". Skipping creation");
    }
  }

  public MavenProject getProject() {
    return project;
  }

  public int getDay() {
    return day;
  }

  public boolean isForce() {
    return force;
  }

  public boolean isUseDayPackage() {
    return useDayPackage;
  }

  public void setProject(MavenProject project) {
    this.project = project;
  }

  public void setDay(int day) {
    this.day = day;
  }

  public void setForce(boolean force) {
    this.force = force;
  }

  public void setUseDayPackage(boolean useDayPackage) {
    this.useDayPackage = useDayPackage;
  }
}
