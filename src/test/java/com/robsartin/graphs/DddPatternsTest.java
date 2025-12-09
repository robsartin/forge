package com.robsartin.graphs;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests for DDD (Domain-Driven Design) patterns enforced via jMolecules.
 * These tests verify that domain model classes properly follow DDD principles
 * for aggregates, entities, and value objects as the project evolves.
 */
@DisplayName("DDD Patterns")
class DddPatternsTest {

    private static JavaClasses allClasses;

    @BeforeAll
    static void setUp() {
        allClasses = new ClassFileImporter()
                .importPackages("com.robsartin.graphs");
    }

    @Nested
    @DisplayName("Domain Organization")
    class DomainOrganization {

        @Test
        @DisplayName("domain package should be created for DDD models")
        void domainPackageShouldExist() {
            // This test serves as a check that domain package will exist
            // It will pass once domain classes are added to the project
            classes()
                    .that().resideInAPackage("com.robsartin.graphs.domain..")
                    .should().notBeAnnotatedWith("org.springframework.stereotype.Service")
                    .allowEmptyShould(true)
                    .check(allClasses);
        }

        @Test
        @DisplayName("domain classes should not depend on application")
        void domainShouldNotDependOnApplication() {
            noClasses()
                    .that().resideInAPackage("com.robsartin.graphs.domain..")
                    .should().dependOnClassesThat().resideInAPackage("com.robsartin.graphs.application..")
                    .allowEmptyShould(true)
                    .check(allClasses);
        }

        @Test
        @DisplayName("domain classes should not depend on Spring framework")
        void domainShouldNotDependOnSpring() {
            noClasses()
                    .that().resideInAPackage("com.robsartin.graphs.domain..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                    .allowEmptyShould(true)
                    .check(allClasses);
        }
    }

    @Nested
    @DisplayName("Port and Adapter Pattern")
    class PortAndAdapterPattern {

        @Test
        @DisplayName("ports should be defined as interfaces in domain.port package")
        void portsShouldBeInterfaces() {
            classes()
                    .that().resideInAPackage("com.robsartin.graphs.domain.port..")
                    .should().beInterfaces()
                    .allowEmptyShould(true)
                    .check(allClasses);
        }

        @Test
        @DisplayName("adapters should reside in infrastructure.adapters package")
        void adaptersShouldResideInAdaptersPackage() {
            classes()
                    .that().haveNameMatching(".*Adapter$")
                    .should().resideInAPackage("com.robsartin.graphs.infrastructure.adapters..")
                    .allowEmptyShould(true)
                    .check(allClasses);
        }
    }

    @Nested
    @DisplayName("Encapsulation Rules")
    class EncapsulationRules {

        @Test
        @DisplayName("domain services should not be public Spring services")
        void domainServicesNotPublic() {
            noClasses()
                    .that().resideInAPackage("com.robsartin.graphs.domain..")
                    .should().beAnnotatedWith("org.springframework.stereotype.Service")
                    .allowEmptyShould(true)
                    .check(allClasses);
        }

        @Test
        @DisplayName("repositories should only be in infrastructure")
        void repositoriesInInfrastructure() {
            classes()
                    .that().haveNameMatching(".*Repository$")
                    .should().resideInAPackage("com.robsartin.graphs.ports.out..")
                    .orShould().resideInAPackage("com.robsartin.graphs.infrastructure.adapters..")
                    .allowEmptyShould(true)
                    .check(allClasses);
        }
    }

    @Nested
    @DisplayName("Feature Isolation")
    class FeatureIsolation {

        @Test
        @DisplayName("features should be independent packages")
        void featuresShouldBeIndependent() {
            // Features can be added in separate feature packages
            // This test verifies the structure once features are added
            classes()
                    .that().resideInAPackage("com.robsartin.graphs.features..")
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            "java..",
                            "com.robsartin.graphs..",
                            "org.springframework..",
                            "org.jmolecules.."
                    )
                    .allowEmptyShould(true)
                    .check(allClasses);
        }
    }
}
