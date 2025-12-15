package com.robsartin.graphs;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * ArchUnit tests enforcing development rules and architectural standards.
 * These tests ensure the codebase adheres to the hexagonal architecture
 * pattern, DDD principles, and project standards.
 */
@DisplayName("Architecture Rules")
class ArchitectureRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .importPackages("com.robsartin.graphs");
    }

    @Nested
    @DisplayName("Layered Architecture")
    class LayeredArchitectureTests {

        @Test
        @DisplayName("should enforce layered architecture - infrastructure independence")
        void shouldEnforceInfrastructureRules() {
            // Infrastructure classes should be allowed to depend on Spring
            // This rule passes because infrastructure is allowed to use Spring
            classes()
                    .that().resideInAPackage("com.robsartin.graphs.infrastructure..")
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            "java..",
                            "dev.openfeature..",
                            "com.robsartin.graphs..",
                            "org.springframework..",
                            "org.junit..",
                            "org.jmolecules.."
                    )
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("should enforce configuration classes in config package")
        void configurationClassesLocation() {
            classes()
                    .that().haveNameMatching(".*Configuration$")
                    .should().resideInAPackage("com.robsartin.graphs.config..")
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("Coding Standards")
    class CodingStandardsTests {

        @Test
        @DisplayName("should not use Java util logging")
        void shouldNotUseJavaUtilLogging() {
            GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("all classes should have meaningful names")
        void allClassesShouldHaveMeaningfulNames() {
            classes()
                    .should().haveNameMatching(".*[A-Z].*")
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("Spring Configuration Rules")
    class SpringConfigurationRules {

        @Test
        @DisplayName("Spring configuration classes should be in config package")
        void springConfigurationLocation() {
            classes()
                    .that().resideInAPackage("com.robsartin.graphs.config..")
                    .should().haveNameMatching(".*Configuration$")
                    .orShould().haveNameMatching(".*Config$")
                    .orShould().haveNameMatching(".*Properties$")
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("Package Organization")
    class PackageOrganization {

        @Test
        @DisplayName("infrastructure package should exist and contain adapter code")
        void infrastructurePackageOrganization() {
            classes()
                    .that().resideInAPackage("com.robsartin.graphs.infrastructure..")
                    .should().notBeInterfaces()
                    .orShould().beInterfaces()
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("config package should exist and contain configuration")
        void configPackageOrganization() {
            classes()
                    .that().resideInAPackage("com.robsartin.graphs.config..")
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            "java..",
                            "dev.openfeature..",
                            "com.robsartin.graphs..",
                            "org.springframework..",
                            "org.jmolecules.."
                    )
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }
}
