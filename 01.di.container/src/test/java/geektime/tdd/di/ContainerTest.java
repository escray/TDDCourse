package geektime.tdd.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContainerTest {
    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Nested
    public class ComponentConstruction {
        // TODO: instance
        @Test
        public void should_bind_type_to_a_specific_instance() {
            Component instance = new Component() {};
            config.bind(Component.class, instance);
            Context context = config.getContext();
            assertSame(instance, context.get(Component.class).get());
        }

        // TODO: abstract class
        // TODO: interface

        @Test
        public void should_return_empty_if_component_not_defined() {
            Optional<Component> component = config.getContext().get(Component.class);
            assertTrue(component.isEmpty());
        }

        @Nested
        // 构造函数注入
        public class ConstructorInjection {
            // DONE: No args constructor
            // 无依赖的组件应该通过默认构造函数生成组件实例
            @Test
            public void should_bind_type_to_a_class_with_default_constructor() {
                config.bind(Component.class, ComponentWithDefaultConstructor.class);
                Component instance = config.getContext().get(Component.class).get();
                assertNotNull(instance);
                assertTrue(instance instanceof ComponentWithDefaultConstructor);
            }

            // DONE: with dependencies
            // 有依赖的组件，通过 Inject 标注的构造函数生成组件实例
            @Test
            public void should_bind_type_to_a_class_with_inject_constructor() {
                Dependency dependency = new Dependency() {};

                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, dependency);

                Component instance = config.getContext().get(Component.class).get();
                assertNotNull(instance);
                assertSame(dependency, ((ComponentWithInjectConstructor) instance).getDependency());
            }

            // DONE: A -> B -> C
            // 如果所依赖的组件也存在依赖，那么需要对所依赖的组件也完成依赖注入
            @Test
            public void should_bind_type_to_a_class_with_transitive_dependencies() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyWithInjectConstructor.class);
                config.bind(String.class, "indirect dependency");

                Component instance = config.getContext().get(Component.class).get();
                assertNotNull(instance);

                Dependency dependency = ((ComponentWithInjectConstructor)instance).getDependency();
                assertNotNull(dependency);

                assertEquals("indirect dependency", ((DependencyWithInjectConstructor)dependency).getDependency());
            }

            // sad path, error condition
            // DONE: multi inject constructors
            // 如果组件有多于一个 Inject 标注的构造函数，则抛出异常
            @Test
            public void should_throw_exception_if_multi_inject_constructor_provided() {
                assertThrows(IllegalComponentException.class,
                        () -> config.bind(Component.class, ComponentWithMultiInjectConstructors.class));
            }

            // DONE: no default constructor and inject constructor
            // 如果组件没有 Inject 标注的构造函数，也没有默认构造函数（新增任务）
            @Test
            public void should_throw_exception_if_no_inject_nor_default_constructor_provider() {
                assertThrows(IllegalComponentException.class,
                        () -> config.bind(Component.class, ComponentWithNoInjectConstructorNorDefaultConstructor.class));
            }

            // TODO: dependencies not exist
            // 如果组件需要的依赖不存在，则抛出异常
            @Test
            public void should_throw_exception_if_dependency_not_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                assertThrows(DependencyNotFoundException.class, () -> config.getContext());

                DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());
                assertEquals(Dependency.class, exception.getDependency());
                assertEquals(Component.class, exception.getComponent());
            }

            @Test
            public void should_throw_exception_if_transitive_dependency_not_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyWithInjectConstructor.class);

                DependencyNotFoundException exception = assertThrows(DependencyNotFoundException.class, () -> config.getContext());
                assertEquals(String.class, exception.getDependency());
                assertEquals(Dependency.class, exception.getComponent());
            }

            // 如果组件间存在循环依赖，则抛出异常
            @Test
            public void should_throw_exception_if_cyclic_dependencies_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyDependedOnComponent.class);

                CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

                Set<Class<?>> classes = Sets.newSet(exception.getComponents());
                assertEquals(2, classes.size());
                assertTrue(classes.contains(Component.class));
                assertTrue(classes.contains(Dependency.class));
            }

            // A -> B -> C -> A
            @Test
            public void should_throw_exception_if_transitive_cyclic_dependencies_found() {
                config.bind(Component.class, ComponentWithInjectConstructor.class);
                config.bind(Dependency.class, DependencyDependedOnAnotherDependency.class);
                config.bind(AnotherDependency.class, AnotherDependencyDependedOnComponent.class);

                CyclicDependenciesFoundException exception = assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());

                List<Class<?>> components = Arrays.asList(exception.getComponents());

                assertEquals(3, components.size());
                assertTrue(components.contains(Component.class));
                assertTrue(components.contains(Dependency.class));
                assertTrue(components.contains(AnotherDependency.class));
            }
        }

        @Nested
        public class FieldInjection {

            static class ComponentWithFieldInjection {
                @Inject
                Dependency dependency;
//                public Dependency getDependency() {
//                    return dependency;
//                }
            }

            static class SubclassWithFieldInjection extends ComponentWithFieldInjection {}

            // TODO: inject field
            @Test
            public void should_inject_dependency_via_field() {
                Dependency dependency = new Dependency() {};
                config.bind(Dependency.class, dependency);
                config.bind(ComponentWithFieldInjection.class, ComponentWithFieldInjection.class);

                ComponentWithFieldInjection component = config.getContext().get(ComponentWithFieldInjection.class).get();

                assertSame(dependency, component.dependency);
            }

            @Test
            public void should_inject_dependency_via_superclass_inject_field() {
                Dependency dependency = new Dependency() {};
                config.bind(Dependency.class, dependency);
                config.bind(SubclassWithFieldInjection.class, SubclassWithFieldInjection.class);

                SubclassWithFieldInjection component = config.getContext().get(SubclassWithFieldInjection.class).get();
                assertSame(dependency, component.dependency);
            }

            @Test
            public void should_create_component_with_injection_field() {
                Context context = mock(Context.class);
                Dependency dependency = mock(Dependency.class);
                when(context.get(eq(Dependency.class)))
                        .thenReturn(Optional.of(dependency));

                ConstructorInjectionProvider<ComponentWithFieldInjection> provider = new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class);
                ComponentWithFieldInjection component = provider.get(context);

                assertSame(dependency, component.dependency);
            }

            // TODO: throw exception if field is final

            // TODO: provider dependency information for field injection

            @Test
            public void should_include_field_dependency_in_dependencies() {
                ConstructorInjectionProvider<ComponentWithFieldInjection> provider =
                        new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class);
                assertArrayEquals(new Class<?>[]{Dependency.class},
                        provider.getDependencies().toArray(Class<?>[]::new));
            }

            @Test
            public void should_throw_exception_when_field_dependency_missing() {
                config.bind(ComponentWithFieldInjection.class, ComponentWithFieldInjection.class);
                assertThrows(DependencyNotFoundException.class, () -> config.getContext());
            }


            class DependencyWithFieldInjection implements Dependency {
                @Inject
                ComponentWithFieldInjection component;
            }

//            @Test
//            public void should_throw_exception_when_field_has_cyclic_dependencies() {
//                config.bind(ComponentWithFieldInjection.class, ComponentWithFieldInjection.class);
//                config.bind(Dependency.class, DependencyWithFieldInjection.class);
//
//                assertThrows(CyclicDependenciesFoundException.class, () -> config.getContext());
//            }

            @Test
            public void should_include_field_dependency_in_dependencies_() {
                ConstructorInjectionProvider<ComponentWithFieldInjection> provider
                        = new ConstructorInjectionProvider<>(ComponentWithFieldInjection.class);
                assertArrayEquals(new Class<?>[]{Dependency.class},
                        provider.getDependencies().toArray(Class<?>[]::new));

            }
        }

        @Nested
        public class MethodInjection {

        }
    }

    @Nested
    public class DependenciesSelection {

    }

    @Nested
    public class LifecycleManagement {
    }
}