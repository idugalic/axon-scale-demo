package com.demo;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packagesOf = AxonScaleDemoArchitectureTests.class)
@ActiveProfiles(profiles = "command,query")
public class AxonScaleDemoArchitectureTests {

    /**
     * Testing if the classes in "..command.. , ..query.. " packages are `package private`.
     * <p>
     * This will work with Java only. Kotlin does not have package modifier.
     */
    @ArchTest
    public static final ArchRule encapsulationJavaRule = ArchRuleDefinition.classes()
            .that().resideInAnyPackage("..command..", "..query..")
            .should().bePackagePrivate();

}
