package net.ddellspe;

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
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * @phase process-sources
 */
@Mojo(name = "generate-day", defaultPhase = LifecyclePhase.NONE)
public class AocGeneratorMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "-1", property = "day", readonly = true)
  private int day;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (day == -1) {
      day =
          Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("America/New_York")))
              .get(Calendar.DAY_OF_MONTH);
    }
    getLog().info("Generating files for Day " + day);
    getLog().info(project.getBuild().getTestSourceDirectory());
    String pkg = String.format("day%02d", day);
    String cls = String.format("Day%02d", day);
    String path =
        Arrays.stream(project.getGroupId().split("\\."))
            .map(File::new)
            .reduce(
                new File(""),
                (prev, cur) -> new File(String.valueOf(Paths.get(prev.getPath(), cur.getPath()))))
            .getPath();
    List<MethodSpec> methods = new ArrayList<>();
    methods.add(MethodSpec.constructorBuilder().build());
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
    JavaFile sourceFile =
        JavaFile.builder(project.getGroupId() + "." + pkg, dayClass)
            .skipJavaLangImports(true)
            .build();
    try {
      sourceFile.writeToFile(new File(project.getBuild().getSourceDirectory()));
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
        JavaFile.builder(project.getGroupId() + "." + pkg, dayTestClass)
            .skipJavaLangImports(true)
            .addStaticImport(ClassName.get("org.junit.jupiter.api", "Assertions"), "assertEquals")
            .build();
    try {
      testFile.writeToFile(new File(project.getBuild().getTestSourceDirectory()));
    } catch (IOException e) {
      getLog().error("Unable to create new java file: " + cls + ".java");
    }
    File inputFile =
        Paths.get(project.getBuild().getResources().get(0).getDirectory(), path, pkg, "input.txt")
            .toFile();
    if (!inputFile.exists()) {
      try {
        if (inputFile.getParentFile().mkdirs() && !inputFile.createNewFile()) {
          getLog().warn("Failed to create input file for tests at: " + inputFile.getPath());
        }
      } catch (IOException e) {
        getLog().error("Unable to create new input file for tests at: " + inputFile.getPath());
      }
    }
    File exampleFile =
        Paths.get(
                project.getBuild().getTestResources().get(0).getDirectory(),
                path,
                pkg,
                "example.txt")
            .toFile();
    if (!exampleFile.exists()) {
      try {
        if (exampleFile.getParentFile().mkdirs() && !exampleFile.createNewFile()) {
          getLog().warn("Failed to create example file for tests at: " + exampleFile.getPath());
        }
      } catch (IOException e) {
        getLog().error("Unable to create new example file for tests at: " + exampleFile.getPath());
      }
    }
  }
}
