package geektime.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.mockito.internal.util.collections.Sets;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class ContainerTest {
    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Nested
    public class TypeBinding {
        // TODO: instance
        @Test
        @DisplayName("should bind type to a specific instance")
        public void should_bind_type_to_a_specific_instance() {
            Component instance = new Component() {
            };
            config.bind(Component.class, instance);
            Context context = config.getContext();
            assertSame(instance, context.get(Component.class).get());
        }

        @ParameterizedTest(name = "support {0}")
        @MethodSource
        public void should_bind_type_to_an_injectable_component(Class<? extends Component> componentType) {
            Dependency dependency = new Dependency() {
            };
            config.bind(Dependency.class, dependency);
            config.bind(Component.class, componentType);

            Optional<Component> component = config.getContext().get(Component.class);

            assertTrue(component.isPresent());
//            assertSame(dependency, component.get().dependency());
        }

        public static Stream<Arguments> should_bind_type_to_an_injectable_component() {
            return Stream.of(Arguments.of(Named.of("Constructor Injection", TypeBinding.ConstructorInjection.class)),
                    Arguments.of(Named.of("Field Injection", TypeBinding.FieldInjection.class)),
                    Arguments.of(Named.of("Method Injection", TypeBinding.MethodInjection.class)));
        }

        static class ConstructorInjection implements Component {

            private Dependency dependency;

            @Inject
            public ConstructorInjection(Dependency dependency) {
                this.dependency = dependency;
            }

            public Dependency dependency() {
                return dependency;
            }
        }

        static class FieldInjection implements Component {
            @Inject
            Dependency dependency;


            public Dependency dependency() {
                return dependency;
            }
        }

        static class MethodInjection implements Component {
            private Dependency dependency;

            @Inject
            void install(Dependency dependency) {
                this.dependency = dependency;
            }
        }

        @Test
        public void should_return_empty_if_component_not_defined() {
            Optional<Component> component = config.getContext().get(Component.class);
            assertTrue(component.isEmpty());
        }
    }

    @Nested
    public class DependencyCheck {

        // TODO: dependencies not exist
        // 如果组件需要的依赖不存在，则抛出异常

        @ParameterizedTest
        @MethodSource
        public void should_throw_exception_if_dependency_not_found_para(Class<? extends Component> component) {
            config.bind(Component.class, component);

            DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

            assertEquals(Dependency.class, exception.getDependency());
            assertEquals(Component.class, exception.getComponent());
        }

        public static Stream<Arguments> should_throw_exception_if_dependency_not_found_para() {
            return Stream.of(Arguments.of(Named.of("Inject Constructor", DependencyCheck.MissingDependencyConstructor.class)),
                    Arguments.of(Named.of("Inject Field", DependencyCheck.MissingDependencyField.class)),
                    Arguments.of(Named.of("Inject Method", DependencyCheck.MissingDependencyMethod.class)));
        }

        static class MissingDependencyConstructor implements Component {
            @Inject
            public MissingDependencyConstructor(Dependency dependency) {
            }
        }

        static class MissingDependencyField implements Component {
            @Inject
            Dependency dependency;
        }

        static class MissingDependencyMethod implements Component {

            @Inject
            void install(Dependency dependency) {
            }
        }

        // 如果组件间存在循环依赖，则抛出异常
        @ParameterizedTest(name = "cyclic dependency between {0} and {1}")
        @MethodSource
        public void should_throw_exception_if_cyclic_dependencies_found(Class<? extends Component> component,
                                                                             Class<? extends Dependency> dependency) {
            config.bind(Component.class, component);
            config.bind(Dependency.class, dependency);

            CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

            Set<Class<?>> classes = Sets.newSet(exception.getComponents());

            assertEquals(2, classes.size());
            assertTrue(classes.contains(Component.class));
            assertTrue(classes.contains(Dependency.class));
        }
        public static Stream<Arguments> should_throw_exception_if_cyclic_dependencies_found() {
            List<Arguments> arguments = new ArrayList<>();
            for (Named component : List.of(Named.of("Inject Constructor", DependencyCheck.CyclicComponentInjectConstructor.class),
                    Named.of("Inject Field", DependencyCheck.CyclicComponentInjectField.class),
                    Named.of("Inject Method", DependencyCheck.CyclicComponentInjectMethod.class))) {
                for (Named dependency : List.of(Named.of("Inject Constructor", DependencyCheck.CyclicDependencyInjectConstructor.class),
                        Named.of("Inject Field", DependencyCheck.CyclicDependencyInjectField.class),
                        Named.of("Inject Method", DependencyCheck.CyclicDependencyInjectMethod.class))) {
                    arguments.add(Arguments.of(component, dependency));
                }
            }
            return arguments.stream();
        }

        static class CyclicComponentInjectConstructor implements Component  {
            @Inject
            public CyclicComponentInjectConstructor(Dependency dependency) {
            }
        }

        static class CyclicComponentInjectField implements Component {
            @Inject
            Dependency dependency;
        }

        static class CyclicComponentInjectMethod implements Component {
            @Inject
            void install(Dependency dependency) {
            }
        }

        static class CyclicDependencyInjectConstructor implements Dependency {
            @Inject
            public CyclicDependencyInjectConstructor(Component component) {
            }
        }

        static class CyclicDependencyInjectField implements Dependency {
            @Inject
            Component component;
        }

        static class CyclicDependencyInjectMethod implements Dependency {
            @Inject
            void install(Component component) {

            }
        }


        // A -> B -> C -> A
        @ParameterizedTest(name = "indirect cyclic dependency between {0}, {1} and {2}")
        @MethodSource
        public void should_throw_exception_if_transitive_cyclic_dependencies_found(
                Class<? extends Component> component,
                Class<? extends Dependency> dependency,
                Class<? extends AnotherDependency> anotherDependency
        ) {
            config.bind(Component.class, component);
            config.bind(Dependency.class, dependency);
            config.bind(AnotherDependency.class, anotherDependency);

            CyclicDependenciesFoundException exception = assertThrows(
                    CyclicDependenciesFoundException.class, () -> config.getContext());

            List<Class<?>> components = Arrays.asList(exception.getComponents());

            assertEquals(3, components.size());
            assertTrue(components.contains(Component.class));
            assertTrue(components.contains(Dependency.class));
            assertTrue(components.contains(AnotherDependency.class));
        }

        public static Stream<Arguments> should_throw_exception_if_transitive_cyclic_dependencies_found() {
            List<Arguments> arguments = new ArrayList<>();
            for (Named component : List.of(Named.of("Inject Constructor", DependencyCheck.CyclicComponentInjectConstructor.class),
                    Named.of("Inject Field", DependencyCheck.CyclicComponentInjectField.class),
                    Named.of("Inject Method", DependencyCheck.CyclicComponentInjectMethod.class))) {
                for (Named dependency : List.of(Named.of("Inject Constructor", DependencyCheck.IndirectCyclicDependencyInjectConstructor.class),
                        Named.of("Inject Field", DependencyCheck.IndirectCyclicDependencyInjectField.class),
                        Named.of("Inject Method", DependencyCheck.IndirectCyclicDependencyInjectMethod.class))) {
                    for (Named anotherDependency : List.of(Named.of("Inject Constructor", IndirectCyclicAnotherDependencyInjectConstructor.class),
                            Named.of("Inject Field", DependencyCheck.IndirectCyclicAnotherDependencyInjectField.class),
                            Named.of("Inject Method", DependencyCheck.IndirectCyclicAnotherDependencyInjectMethod.class))) {
                        arguments.add(Arguments.of(component, dependency, anotherDependency));
                    }
                }
            }
            return arguments.stream();
        }

        public static class IndirectCyclicDependencyInjectConstructor implements Dependency  {
            @Inject
            public IndirectCyclicDependencyInjectConstructor(AnotherDependency anotherDependency) {
            }
        }

        public static class IndirectCyclicDependencyInjectField implements Dependency  {
            @Inject
            AnotherDependency anotherDependency;
        }

        public static class IndirectCyclicDependencyInjectMethod implements Dependency {
            @Inject
            void install(AnotherDependency anotherDependency) {
            }
        }

        public static class IndirectCyclicAnotherDependencyInjectConstructor  implements AnotherDependency  {
            @Inject
            public IndirectCyclicAnotherDependencyInjectConstructor(Component component) {
            }
        }

        public static class IndirectCyclicAnotherDependencyInjectField implements AnotherDependency  {
            @Inject
            Component component;
        }

        public static class IndirectCyclicAnotherDependencyInjectMethod implements AnotherDependency  {
            @Inject
            void install(Component component) {

            }
        }
    }


    @Nested
    public class DependenciesSelection {
    }

    @Nested
    public class LifecycleManagement {
    }
}