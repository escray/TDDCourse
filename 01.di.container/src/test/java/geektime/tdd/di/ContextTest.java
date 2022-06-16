package geektime.tdd.di;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.util.collections.Sets;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.*;

public class ContextTest {
    ContextConfig config;
    TestComponent instance;

    @BeforeEach
    public void setup() {

        config = new ContextConfig();
        instance = new TestComponent(){};
    }

    @Nested
    public class TypeBinding {
        // DONE: instance
        @Test
        @DisplayName("should bind type to a specific instance")
        public void should_bind_type_to_a_specific_instance() {
            config.bind(TestComponent.class, instance);

            Context context = config.getContext();
            assertSame(instance, context.get(ComponentRef.of(TestComponent.class)).get());
        }

        @ParameterizedTest(name = "support {0}")
        @MethodSource
        public void should_bind_type_to_an_injectable_component(Class<? extends TestComponent> componentType) {
            Dependency dependency = new Dependency() {
            };
            config.bind(Dependency.class, dependency);
            config.bind(TestComponent.class, componentType);

            Optional<TestComponent> component = config.getContext().get(ComponentRef.of(TestComponent.class));

            assertTrue(component.isPresent());
        }

        public static Stream<Arguments> should_bind_type_to_an_injectable_component() {
            return Stream.of(Arguments.of(Named.of("Constructor Injection", TypeBinding.ConstructorInjection.class)),
                    Arguments.of(Named.of("Field Injection", TypeBinding.FieldInjection.class)),
                    Arguments.of(Named.of("Method Injection", TypeBinding.MethodInjection.class)));
        }

        static class ConstructorInjection implements TestComponent {

            private Dependency dependency;

            @Inject
            public ConstructorInjection(Dependency dependency) {
                this.dependency = dependency;
            }

            public Dependency dependency() {
                return dependency;
            }
        }

        static class FieldInjection implements TestComponent {
            @Inject
            Dependency dependency;
            public Dependency dependency() {
                return dependency;
            }
        }

        static class MethodInjection implements TestComponent {
            private Dependency dependency;

            @Inject
            void install(Dependency dependency) {
                this.dependency = dependency;
            }
        }

        @Test
        public void should_return_empty_if_component_not_defined() {
            Optional<TestComponent> component = config.getContext().get(ComponentRef.of(TestComponent.class));
            assertTrue(component.isEmpty());
        }

        // Context
        // DONE: could get Provider<T> from context
        @Test
        public void should_retrieve_component_bind_type_as_provider() {
            config.bind(TestComponent.class, instance);

            Context context = config.getContext();

            ParameterizedType type = new TypeLiteral<Provider<TestComponent>>() {}.getType();

            assertEquals(Provider.class, type.getRawType());
            assertEquals(TestComponent.class, type.getActualTypeArguments()[0]);

            Provider<TestComponent> provider = context.get(new ComponentRef<Provider<TestComponent>>(){}).get();
            assertSame(instance, provider.get());
        }

        @Test
        public void should_not_retrieve_bind_type_as_unsupported_container() {

            config.bind(TestComponent.class, instance);

            Context context = config.getContext();

            ParameterizedType type = new TypeLiteral<List<TestComponent>>() {}.getType();

            assertFalse(context.get(new ComponentRef<List<TestComponent>>() {
            }).isPresent());
        }

        static abstract class TypeLiteral<T> {
            public ParameterizedType getType() {
                return (ParameterizedType)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            }
        }

        @Nested
        public class WithQualifier {
            // DONE: binding component with qualifier
            // DONE: binding component with multi qualifiers
            @Test
            public void should_bind_instance_with_multi_qualifiers() {

                config.bind(TestComponent.class, instance,
                        new NamedLiteral("ChoseOne"),
                        // new NamedLiteral("Skywalker"));
                        new SkywalkerLiteral());
                Context context = config.getContext();

                TestComponent choseOne = context.get(ComponentRef.of(TestComponent.class, new NamedLiteral("ChoseOne"))).get();
                TestComponent skywalker = context.get(ComponentRef.of(TestComponent.class, new SkywalkerLiteral())).get();

                assertSame(instance, choseOne);
                assertSame(instance, skywalker);
            }

            @Test
            public void should_bind_component_with_multi_qualifiers() {
                Dependency dependency = new Dependency() {};
                config.bind(Dependency.class, dependency);
                config.bind(InjectConstructor.class, InjectConstructor.class,
                        new NamedLiteral("ChosenOne"),
                        new SkywalkerLiteral());

                Context context = config.getContext();
                InjectConstructor chosenOne = context.get(ComponentRef.of(InjectConstructor.class, new NamedLiteral("ChosenOne"))).get();
                InjectConstructor skywalker = context.get(ComponentRef.of(InjectConstructor.class, new SkywalkerLiteral())).get();

                assertSame(dependency, chosenOne.getDependency());
                assertSame(dependency, skywalker.getDependency());
            }

            // DONE: throw illegal component if illegal qualifier
            @Test
            public void should_throw_exception_if_illegal_qualifier_to_instance() {

                assertThrows(IllegalComponentException.class,
                        () -> config.bind(TestComponent.class, instance, new TestLiteral()));
            }

            @Test
            public void should_throw_exception_if_illegal_qualifier_to_component() {
                assertThrows(IllegalComponentException.class,
                        () -> config.bind(InjectConstructor.class, InjectConstructor.class, new TestLiteral()));
            }

            // DONE: Provider
            @Test
            public void should_retrieve_bind_type_as_provider() {
                config.bind(TestComponent.class, instance,
                        new NamedLiteral("ChoseOne"),
                        new SkywalkerLiteral());

                Optional<Provider<TestComponent>> provider = config.getContext().get(new ComponentRef<Provider<TestComponent>>(new SkywalkerLiteral()) {} );
                assertTrue(provider.isPresent());
            }

            @Test
            public void should_retrieve_empty_if_no_matched_qualifiers() {
                config.bind(TestComponent.class, instance);
                Optional<TestComponent> component = config.getContext().get(ComponentRef.of(TestComponent.class, new SkywalkerLiteral()));

                assertTrue(component.isEmpty());
            }
        }

        @Nested
        public class WithScope {
            // DONE: default scope should not be singleton
            static class NotSingleton {
            }

            @Test
            public void should_not_be_singleton_scope_by_default() {
                config.bind(NotSingleton.class, NotSingleton.class);
                Context context = config.getContext();
                assertNotSame(context.get(ComponentRef.of(NotSingleton.class)).get(),
                        context.get(ComponentRef.of(NotSingleton.class)));
            }

            @Nested
            public class WithQualifier {
                @Test
                public void should_not_be_singleton_scope_by_default() {
                    config.bind(NotSingleton.class, NotSingleton.class, new SkywalkerLiteral());
                    Context context = config.getContext();
                    assertNotSame(context.get(ComponentRef.of(NotSingleton.class, new SkywalkerLiteral())).get(),
                            context.get(ComponentRef.of(NotSingleton.class, new SkywalkerLiteral())));
                }
            }

            // TODO: bind component as singleton scoped

            @Test
            public void should_bind_component_as_singleton_scope() {
                config.bind(NotSingleton.class, NotSingleton.class, new SingletonLiteral());
                Context context = config.getContext();
                assertSame(context.get(ComponentRef.of(NotSingleton.class)).get(),
                        context.get(ComponentRef.of(NotSingleton.class)).get());
            }


            // TODO: bind component with qualifier as singleton scoped
            // TODO: get scope from component class
            // TODO: get scope from component with qualifier
            // TODO: bind component with customize scope annotation
        }
    }

    @Nested
    public class DependencyCheck {

        // DONE: dependencies not exist
        // 如果组件需要的依赖不存在，则抛出异常
        @ParameterizedTest
        @MethodSource
        public void should_throw_exception_if_dependency_not_found_para(Class<? extends TestComponent> component) {
            config.bind(TestComponent.class, component);

            DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

            assertEquals(Dependency.class, exception.getDependency().type());
            assertEquals(TestComponent.class, exception.getComponent().type());
        }

        // DONE: provider in inject field
        // DONE: provider in inject method
        public static Stream<Arguments> should_throw_exception_if_dependency_not_found_para() {
            return Stream.of(Arguments.of(Named.of("Inject Constructor", MissingDependencyConstructor.class)),
                    Arguments.of(Named.of("Inject Field", MissingDependencyField.class)),
                    Arguments.of(Named.of("Inject Method", MissingDependencyMethod.class)),
                    Arguments.of(Named.of("Provider in Inject Constructor", MissingDependencyProviderConstructor.class)),
                    Arguments.of(Named.of("Provider in Inject Field", MissingDependencyProviderField.class)),
                    Arguments.of(Named.of("Provider in Inject Method", MissingDependencyProviderMethod.class)));
        }

        // TODO: missing dependencies with scope


        static class MissingDependencyConstructor implements TestComponent {
            @Inject
            public MissingDependencyConstructor(Dependency dependency) {
            }
        }

        static class MissingDependencyField implements TestComponent {
            @Inject
            Dependency dependency;
        }

        static class MissingDependencyMethod implements TestComponent {
            @Inject
            void install(Dependency dependency) {
            }
        }

        static class MissingDependencyProviderConstructor implements TestComponent {
            @Inject
            public MissingDependencyProviderConstructor(Provider<Dependency> dependency) {
            }
        }

        static class MissingDependencyProviderField implements TestComponent {
            @Inject
            Provider<Dependency> dependency;
        }

        static class MissingDependencyProviderMethod implements TestComponent {
            @Inject
            void install(Provider<Dependency> dependency) {
            }
        }

        // 如果组件间存在循环依赖，则抛出异常
        @ParameterizedTest(name = "cyclic dependency between {0} and {1}")
        @MethodSource
        public void should_throw_exception_if_cyclic_dependencies_found(Class<? extends TestComponent> component,
                                                                        Class<? extends Dependency> dependency) {
            config.bind(TestComponent.class, component);
            config.bind(Dependency.class, dependency);

            CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

            Set<Class<?>> classes = Sets.newSet(exception.getComponents());

            assertEquals(2, classes.size());
            assertTrue(classes.contains(TestComponent.class));
            assertTrue(classes.contains(Dependency.class));
        }

        public static Stream<Arguments> should_throw_exception_if_cyclic_dependencies_found() {
            List<Arguments> arguments = new ArrayList<>();
            for (Named component : List.of(Named.of("Inject Constructor", CyclicComponentInjectConstructor.class),
                    Named.of("Inject Field", CyclicComponentInjectField.class),
                    Named.of("Inject Method", CyclicComponentInjectMethod.class))) {
                for (Named dependency : List.of(Named.of("Inject Constructor", CyclicDependencyInjectConstructor.class),
                        Named.of("Inject Field", CyclicDependencyInjectField.class),
                        Named.of("Inject Method", CyclicDependencyInjectMethod.class))) {
                    arguments.add(Arguments.of(component, dependency));
                }
            }
            return arguments.stream();
        }

        // TODO: cyclic dependencies with scope

        static class CyclicComponentInjectConstructor implements TestComponent {
            @Inject
            public CyclicComponentInjectConstructor(Dependency dependency) {
            }
        }

        static class CyclicComponentInjectField implements TestComponent {
            @Inject
            Dependency dependency;
        }

        static class CyclicComponentInjectMethod implements TestComponent {
            @Inject
            void install(Dependency dependency) {
            }
        }

        static class CyclicDependencyInjectConstructor implements Dependency {
            @Inject
            public CyclicDependencyInjectConstructor(TestComponent component) {
            }
        }

        static class CyclicDependencyInjectField implements Dependency {
            @Inject
            TestComponent component;
        }

        static class CyclicDependencyInjectMethod implements Dependency {
            @Inject
            void install(TestComponent component) {

            }
        }

        // A -> B -> C -> A
        @ParameterizedTest(name = "indirect cyclic dependency between {0}, {1} and {2}")
        @MethodSource
        public void should_throw_exception_if_transitive_cyclic_dependencies_found(
                Class<? extends TestComponent> component,
                Class<? extends Dependency> dependency,
                Class<? extends AnotherDependency> anotherDependency
        ) {
            config.bind(TestComponent.class, component);
            config.bind(Dependency.class, dependency);
            config.bind(AnotherDependency.class, anotherDependency);

            CyclicDependenciesFoundException exception = assertThrows(
                    CyclicDependenciesFoundException.class, () -> config.getContext());

            List<Class<?>> components = Arrays.asList(exception.getComponents());

            assertEquals(3, components.size());
            assertTrue(components.contains(TestComponent.class));
            assertTrue(components.contains(Dependency.class));
            assertTrue(components.contains(AnotherDependency.class));
        }

        public static Stream<Arguments> should_throw_exception_if_transitive_cyclic_dependencies_found() {
            List<Arguments> arguments = new ArrayList<>();
            for (Named component : List.of(Named.of("Inject Constructor", CyclicComponentInjectConstructor.class),
                    Named.of("Inject Field", CyclicComponentInjectField.class),
                    Named.of("Inject Method", CyclicComponentInjectMethod.class))) {
                for (Named dependency : List.of(Named.of("Inject Constructor", IndirectCyclicDependencyInjectConstructor.class),
                        Named.of("Inject Field", IndirectCyclicDependencyInjectField.class),
                        Named.of("Inject Method", IndirectCyclicDependencyInjectMethod.class))) {
                    for (Named anotherDependency : List.of(Named.of("Inject Constructor", IndirectCyclicAnotherDependencyInjectConstructor.class),
                            Named.of("Inject Field", IndirectCyclicAnotherDependencyInjectField.class),
                            Named.of("Inject Method", IndirectCyclicAnotherDependencyInjectMethod.class))) {
                        arguments.add(Arguments.of(component, dependency, anotherDependency));
                    }
                }
            }
            return arguments.stream();
        }

        public static class IndirectCyclicDependencyInjectConstructor implements Dependency {
            @Inject
            public IndirectCyclicDependencyInjectConstructor(AnotherDependency anotherDependency) {
            }
        }

        public static class IndirectCyclicDependencyInjectField implements Dependency {
            @Inject
            AnotherDependency anotherDependency;
        }

        public static class IndirectCyclicDependencyInjectMethod implements Dependency {
            @Inject
            void install(AnotherDependency anotherDependency) {
            }
        }

        public static class IndirectCyclicAnotherDependencyInjectConstructor implements AnotherDependency {
            @Inject
            public IndirectCyclicAnotherDependencyInjectConstructor(TestComponent component) {
            }
        }

        public static class IndirectCyclicAnotherDependencyInjectField implements AnotherDependency {
            @Inject
            TestComponent component;
        }

        public static class IndirectCyclicAnotherDependencyInjectMethod implements AnotherDependency {
            @Inject
            void install(TestComponent component) {

            }
        }

        static class CyclicDependencyProviderConstructor implements Dependency {
            @Inject
            public CyclicDependencyProviderConstructor(Provider<TestComponent> component) {
            }
        }

        @Test
        public void should_not_throw_exception_if_cyclic_dependency_with_provider() {
            config.bind(TestComponent.class, CyclicComponentInjectConstructor.class);
            config.bind(Dependency.class, CyclicDependencyProviderConstructor.class);
            Context context = config.getContext();
            assertTrue(context.get(ComponentRef.of(TestComponent.class)).isPresent());
        }

        @Nested
        public class WithQualifier {
            // TODO: dependency missing if qualifier not match

            @ParameterizedTest
            @MethodSource
            public void should_throw_exception_if_dependency_with_qualifier_not_found(Class<? extends TestComponent> component) {
                Dependency dependency = new Dependency() {
                };
                config.bind(Dependency.class, dependency);
                config.bind(TestComponent.class, component, new NamedLiteral("Owner"));
                // config.bind(InjectConstructor.class, InjectConstructor.class, new NamedLiteral("Owner"));

                DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());

                assertEquals(new Component(TestComponent.class, new NamedLiteral("Owner")), exception.getComponent());
                assertEquals(new Component(Dependency.class, new SkywalkerLiteral()), exception.getDependency());
            }

            public static Stream<Arguments> should_throw_exception_if_dependency_with_qualifier_not_found() {
                return Stream.of(Named.of("Inject Constructor with Qualifier", InjectConstructor.class),
                        Named.of("Inject Field with Qualifier", InjectField.class),
                        Named.of("Inject Method with Qualifier", InjectMethod.class),
                        Named.of("Provider in Inject Constructor with Qualifier", InjectConstructorProvider.class),
                        Named.of("Provider in Inject Field with Qualifier", InjectFieldProvider.class),
                        Named.of("Provider in Inject Method with Qualifier", InjectMethodProvider.class)
                        ).map(Arguments::of);
            }

            static class InjectConstructor implements TestComponent {
                @Inject
                public InjectConstructor(@Skywalker Dependency dependency) {
                }
            }

            static class InjectField implements TestComponent {
                @Inject
                @Skywalker
                Dependency dependency;
            }

            static class InjectMethod implements TestComponent {
                @Inject
                void install(@Skywalker Dependency dependency) {

                }
            }

            static class InjectConstructorProvider implements TestComponent {
                @Inject
                public InjectConstructorProvider(@Skywalker Provider<Dependency> dependencyProvider) {

                }
            }

            static class InjectFieldProvider implements TestComponent {
                @Inject
                @Skywalker
                Provider<Dependency> dependency;
            }

            static class InjectMethodProvider implements TestComponent {
                @Inject
                void install(@Skywalker Provider<Dependency> dependencyProvider) {

                }
            }




            // DONE: check cyclic dependencies with qualifier
            // @Skywalker A -> @Skywalker B -> @Skywalker A
            // A -> A
            // A -> @Skywalker A not cyclic dependencies
            // A -> @Skywalker A -> @Named A(instance) => not cyclic dependencies
            static class SkywalkerDependency implements Dependency {
                @Inject
                public SkywalkerDependency(@jakarta.inject.Named("ChosenOne") Dependency dependency) {
                }
            }

            static class NotCyclicDependency implements Dependency {
                @Inject
                public NotCyclicDependency(@Skywalker Dependency dependency) {
                }
            }

            @ParameterizedTest(name = "{1} -> @Skywalker({0}) -> @Named(\"ChosenOne\") not cyclic dependencies")
            @MethodSource
            public void should_not_throw_cyclic_exception_if_component_with_same_type_tagged_with_different_qualifier(
                    Class<? extends TestComponent> skywalker,
                    Class<? extends TestComponent> notCyclic
            ) {
                Dependency dependency = new Dependency() {
                };
                config.bind(Dependency.class, dependency, new NamedLiteral("ChosenOne"));
                config.bind(Dependency.class, SkywalkerDependency.class, new SkywalkerLiteral());
                config.bind(Dependency.class, NotCyclicDependency.class);

//                try {
//                    config.getContext();
//                } catch (DependencyNotFoundException e) {
//                    System.out.println(e.getDependency());
//                }

                assertDoesNotThrow(() -> config.getContext());
            }

            public static Stream<Arguments> should_not_throw_cyclic_exception_if_component_with_same_type_tagged_with_different_qualifier() {
                List<Arguments> arguments = new ArrayList<>();
                for (Named skywalker : List.of(Named.of("Inject Constructor", SkywalkerInjectConstructor.class),
                        Named.of("Inject Field", SkywalkerInjectField.class),
                        Named.of("Inject Method", SkywalkerInjectMethod.class)))
                    for  (Named notCyclic : List.of(Named.of("Inject Constructor", NotCyclicInjectConstructor.class),
                            Named.of("Inject Method", NotCyclicInjectMethod.class),
                            Named.of("Inject Field", NotCyclicInjectField.class))) {
                        arguments.add(Arguments.of(skywalker, notCyclic));
                    }
                return arguments.stream();
            }

            static class SkywalkerInjectConstructor implements Dependency {
                @Inject
                public SkywalkerInjectConstructor(@jakarta.inject.Named("ChosenONe") Dependency dependency) {

                }
            }

            static class SkywalkerInjectField implements Dependency {
                @Inject
                @jakarta.inject.Named("ChoseOne")
                Dependency dependency;
            }

            static class SkywalkerInjectMethod implements Dependency {
                @Inject
                void install(@jakarta.inject.Named("ChosenOne") Dependency dependency) {
                }
            }

            static class NotCyclicInjectConstructor implements Dependency {
                @Inject
                public NotCyclicInjectConstructor(@Skywalker Dependency dependency) {

                }
            }

            static class NotCyclicInjectField implements Dependency {
                @Inject
                @Skywalker
                Dependency dependency;
            }

            static class NotCyclicInjectMethod implements Dependency {
                @Inject
                void install(@Skywalker Dependency dependency) {

                }
            }
        }
    }
}