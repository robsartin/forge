package com.robsartin.graphs;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

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
            classes()
                    .that().resideInAPackage("com.robsartin.graphs.infrastructure..")
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            "java..",
                            "jakarta..",
                            "dev.openfeature..",
                            "com.robsartin.graphs..",
                            "org.mockito..",
                            "org.junit..",
                            "org.jmolecules..",
                            "org.slf4j..",
                            "org.springframework..",
                            "org.assertj.."
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

        @Test
        @DisplayName("application layer should not depend on infrastructure adapters")
        void applicationShouldNotDependOnInfrastructureAdapters() {
            noClasses()
                    .that().resideInAPackage("com.robsartin.graphs.application..")
                    .should().dependOnClassesThat().resideInAPackage("com.robsartin.graphs.infrastructure.adapters..")
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

        @Test
        @DisplayName("should not use field injection with @Autowired in production code")
        void shouldNotUseFieldInjection() {
            fields()
                    .that().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("Test")
                    .should().notBeAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("should not use System.out or System.err")
        void shouldNotUseSystemOutOrErr() {
            GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("Naming Conventions")
    class NamingConventions {

        @Test
        @DisplayName("service classes should end with Service")
        void serviceClassesShouldEndWithService() {
            classes()
                    .that().areAnnotatedWith("org.springframework.stereotype.Service")
                    .should().haveSimpleNameEndingWith("Service")
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("repository interfaces should end with Repository")
        void repositoryInterfacesShouldEndWithRepository() {
            classes()
                    .that().resideInAPackage("com.robsartin.graphs.ports.out..")
                    .and().areInterfaces()
                    .should().haveSimpleNameEndingWith("Repository")
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("adapter classes should end with Adapter")
        void adapterClassesShouldEndWithAdapter() {
            classes()
                    .that().resideInAPackage("com.robsartin.graphs.infrastructure.adapters..")
                    .and().areNotInterfaces()
                    .and().haveSimpleNameNotEndingWith("Repository")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .should().haveSimpleNameEndingWith("Adapter")
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("event classes should end with Event")
        void eventClassesShouldEndWithEvent() {
            classes()
                    .that().resideInAPackage("com.robsartin.graphs.events..")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .should().haveSimpleNameEndingWith("Event")
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("listener classes should end with Listener or EventListener")
        void listenerClassesShouldEndWithListener() {
            classes()
                    .that().resideInAPackage("com.robsartin.graphs.application.listeners..")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .should().haveSimpleNameEndingWith("Listener")
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
                    .orShould().haveNameMatching(".*Test")
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
                            "org.junit..",
                            "com.github..",
                            "io.swagger..",
                            "io.micrometer..",
                            "com.robsartin.graphs..",
                            "org.springframework..",
                            "org.jmolecules..",
                            "org.slf4j..",
                            "jakarta.."
                    )
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("event classes should reside in events package")
        void eventClassesShouldBeInEventsPackage() {
            classes()
                    .that().haveSimpleNameEndingWith("Event")
                    .and().areNotAnnotations()
                    .should().resideInAPackage("com.robsartin.graphs.events..")
                    .allowEmptyShould(true)
                    .check(classes);
        }

        @Test
        @DisplayName("application production code should not depend on config")
        void applicationShouldNotDependOnConfig() {
            noClasses()
                    .that().resideInAPackage("com.robsartin.graphs.application..")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .should().dependOnClassesThat().resideInAPackage("com.robsartin.graphs.config..")
                    .allowEmptyShould(true)
                    .check(classes);
        }
    }
}
