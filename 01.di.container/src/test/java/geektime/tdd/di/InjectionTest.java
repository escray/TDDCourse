package geektime.tdd.di;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

import static geektime.tdd.di.Context.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Nested
public class InjectionTest {
    private final Dependency dependency = mock(Dependency.class);
    private Provider<Dependency> dependencyProvider = mock(Provider.class);
    private final Context context = mock(Context.class);
    private ParameterizedType dependencyProviderType;

    @BeforeEach
    public void setup() throws NoSuchFieldException {
        dependencyProviderType = (ParameterizedType) InjectionTest.class.getDeclaredField("dependencyProvider").getGenericType();
        when(context.get(eq(Ref.of(Dependency.class))))
                .thenReturn(Optional.of(dependency));
        when(context.get(eq(Ref.of(dependencyProviderType)))).thenReturn(Optional.of(dependencyProvider));
    }

    @Nested
    // 构造函数注入
    public class ConstructorInjection {

        @Nested
        class Injection {
            // DONE: No args constructor
            // 无依赖的组件应该通过默认构造函数生成组件实例
            @Test
            public void should_call_default_constructor_if_no_inject_constructor() {
                DefaultConstructor instance = new InjectionProvider<>(DefaultConstructor.class).get(context);

                assertNotNull(instance);
            }

            // DONE: with dependencies
            // 有依赖的组件，通过 Inject 标注的构造函数生成组件实例
            @Test
            public void should_inject_dependency_via_inject_constructor() {
                InjectConstructor instance = new InjectionProvider<>(InjectConstructor.class).get(context);

                assertNotNull(instance);
                assertSame(dependency, instance.getDependency());
            }

            @Test
            public void should_include_dependency_from_inject_constructor() {
                InjectionProvider<InjectConstructor> provider =
                        new InjectionProvider<>(InjectConstructor.class);
                assertArrayEquals(new Ref[]{Ref.of(Dependency.class)},
                        provider.getDependencies().toArray(Ref[]::new));
            }

            // DONE: include dependency type from inject constructor
            @Test
            public void should_include_provider_type_from_inject_constructor() {
                InjectionProvider<ProviderInjectConstructor> provider = new InjectionProvider<>(ProviderInjectConstructor.class);
                assertArrayEquals(new Ref[]{Ref.of(dependencyProviderType)}, provider.getDependencies().toArray(Ref[]::new));
            }

            // InjectionProvider
            // DONE: support inject constructor
            static class ProviderInjectConstructor {
                Provider<Dependency> dependency;
                @Inject
                public ProviderInjectConstructor(Provider<Dependency> dependency) {
                    this.dependency = dependency;
                }
            }

            @Test
            public void should_inject_provider_via_inject_constructor() {
                ProviderInjectConstructor instance = new InjectionProvider<>(ProviderInjectConstructor.class).get(context);

                assertNotNull(instance.dependency);
                assertSame(dependencyProvider, instance.dependency);
            }
        }

        @Nested
        class IllegalInjectConstructors {
            // sad path, error condition
            // DONE: abstract class
            @Test
            public void should_throw_exception_if_component_is_abstract() {
                assertThrows(IllegalComponentException.class,
                        () -> new InjectionProvider<>(AbstractComponent.class));
            }

            // DONE: interface
            @Test
            public void should_throw_exception_if_component_is_interface() {
                assertThrows(IllegalComponentException.class,
                        () -> new InjectionProvider<>(Component.class));
            }

            // DONE: multi inject constructors
            // 如果组件有多于一个 Inject 标注的构造函数，则抛出异常
            @Test
            public void should_throw_exception_if_multi_inject_constructor_provided() {
                assertThrows(IllegalComponentException.class,
                        () -> new InjectionProvider<>(MultiInjectConstructors.class));
            }

            // DONE: no default constructor and inject constructor
            // 如果组件没有 Inject 标注的构造函数，也没有默认构造函数（新增任务）
            @Test
            public void should_throw_exception_if_no_inject_nor_default_constructor_provider() {
                assertThrows(IllegalComponentException.class,
                        () -> new InjectionProvider<>(NoInjectNorDefaultConstructor.class));
            }
        }
    }

    @Nested
    public class FieldInjection {

        @Nested
        class Injection {
            static class ComponentWithFieldInjection {
                @Inject
                Dependency dependency;
            }

            static class SubclassWithFieldInjection extends ComponentWithFieldInjection {
            }

            // DONE: inject field
            @Test
            public void should_inject_dependency_via_field() {
                ComponentWithFieldInjection component = new InjectionProvider<>(ComponentWithFieldInjection.class).get(context);
                assertSame(dependency, component.dependency);
            }

            @Test
            public void should_inject_dependency_via_superclass_inject_field() {
                SubclassWithFieldInjection component = new InjectionProvider<>(SubclassWithFieldInjection.class).get(context);
                assertSame(dependency, component.dependency);
            }

            // DONE provider dependency information for field injection
            @Test
            public void should_include_dependency_from_field_dependency() {
                InjectionProvider<ComponentWithFieldInjection> provider =
                        new InjectionProvider<>(ComponentWithFieldInjection.class);
                assertArrayEquals(new Ref[]{Ref.of(Dependency.class)},
                        provider.getDependencies().toArray(Ref[]::new));
            }

            // DONE: include dependency type from inject field
            @Test
            public void should_include_provider_type_from_inject_field() {
                InjectionProvider<ProviderInjectField> provider = new InjectionProvider<>(ProviderInjectField.class);
                assertArrayEquals(new Ref[]{Ref.of(dependencyProviderType)}, provider.getDependencies().toArray(Ref[]::new));
            }

            // DONE: support inject field
            static class ProviderInjectField {
                @Inject
                Provider<Dependency> dependency;
            }

            @Test
            public void should_inject_provider_via_inject_field() {
                ProviderInjectField instance = new InjectionProvider<>(ProviderInjectField.class).get(context);
                assertSame(dependencyProvider, instance.dependency);
            }
        }

        @Nested
        class IllegalInjectFields {
            // DONE: throw exception if field is final
            static class FinalInjectField {
                @Inject
                final Dependency dependency = null;
            }

            @Test
            public void should_throw_exception_if_inject_field_is_final() {
                assertThrows(IllegalComponentException.class,
                        () -> new InjectionProvider<>(FinalInjectField.class));
            }
        }
    }

    @Nested
    public class MethodInjection {

        @Nested
        class Injection {
            static class InjectMethodWithNoDependency {
                boolean called = false;

                @Inject
                void install() {
                    this.called = true;
                }
            }

            // DONE: inject method with no dependencies will be called
            @Test
            public void should_call_inject_method_even_if_no_dependency_declared() {
                InjectMethodWithNoDependency component = new InjectionProvider<>(InjectMethodWithNoDependency.class).get(context);
                assertTrue(component.called);
            }

            static class InjectMethodWithDependency {
                Dependency dependency;

                @Inject
                void install(Dependency dependency) {
                    this.dependency = dependency;
                }
            }

            // DONE: inject method with dependencies will be injected
            @Test
            public void should_inject_dependency_via_inject_method() {
                InjectMethodWithDependency component = new InjectionProvider<>(InjectMethodWithDependency.class).get(context);
                assertSame(dependency, component.dependency);
            }

            // DONE: include dependency type from inject method
            @Test
            public void should_include_provider_type_from_inject_method() {
                InjectionProvider<ProviderInjectMethod> provider = new InjectionProvider<>(ProviderInjectMethod.class);
                assertArrayEquals(new Ref[]{Ref.of(dependencyProviderType)}, provider.getDependencies().toArray(Ref[]::new));

            }

            // DONE: override inject method from superclass
            static class SuperClassWithInjectMethod {
                int superCalled = 0;

                @Inject
                void install() {
                    this.superCalled++;
                }
            }

            static class SubclassWithInjectMethod extends SuperClassWithInjectMethod {
                int subCalled = 0;

                @Inject
                void installAnother() {
                    this.subCalled = superCalled + 1;
                }
            }

            @Test
            public void should_inject_dependencies_via_inject_method_from_superclass() {
                SubclassWithInjectMethod component = new InjectionProvider<>(SubclassWithInjectMethod.class).get(context);

                assertEquals(1, component.superCalled);
                assertEquals(2, component.subCalled);
            }

            static class SubclassOverrideSuperClassWithInject extends SuperClassWithInjectMethod {
                @Inject
                void install() {
                    super.install();
                }
            }

            @Test
            public void should_only_call_once_if_subclass_override_inject_method_with_inject() {
                SubclassOverrideSuperClassWithInject component = new InjectionProvider<>(SubclassOverrideSuperClassWithInject.class).get(context);
                assertEquals(1, component.superCalled);
            }

            static class SubclassOverrideSuperClassWithNoInject extends SuperClassWithInjectMethod {
                void install() {
                    super.install();
                }
            }

            @Test
            public void should_not_call_inject_method_if_override_with_no_inject() {
                SubclassOverrideSuperClassWithNoInject component = new InjectionProvider<>(SubclassOverrideSuperClassWithNoInject.class).get(context);

                assertEquals(0, component.superCalled);
            }

            // DONE: include dependencies from inject method
            @Test
            public void should_include_dependencies_from_inject_method() {
                InjectionProvider<InjectMethodWithDependency> provider
                        = new InjectionProvider<>(InjectMethodWithDependency.class);
                assertArrayEquals(new Ref[]{Ref.of(Dependency.class)},
                        provider.getDependencies().toArray(Ref[]::new));
            }

            // DONE: support inject method
            static class ProviderInjectMethod {
                Provider<Dependency> dependency;

                @Inject
                void install(Provider<Dependency> dependency) {
                    this.dependency = dependency;
                }
            }

            @Test
            public void should_inject_provider_via_inject_constructor() {
                ProviderInjectMethod instance = new InjectionProvider<>(ProviderInjectMethod.class).get(context);
                assertSame(dependencyProvider, instance.dependency);
            }
        }

        @Nested
        class IllegalInjectMethods {

            // DONE: throw exception if type parameter defined
            static class InjectMethodWithTypeParameter {
                @Inject
                <T> void install() {
                }
            }

            @Test
            public void should_throw_exception_if_inject_method_has_type_parameter() {
                assertThrows(IllegalComponentException.class,
                        () -> new InjectionProvider<>(InjectMethodWithTypeParameter.class));
            }
        }
    }
}
