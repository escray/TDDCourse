package geektime.tdd.di;

import jakarta.inject.Provider;

import java.lang.annotation.Annotation;
import java.util.*;

public class ContextConfig {
    private final Map<Class<?>, ComponentProvider<?>> providers = new HashMap<>();
    private Map<Component, ComponentProvider<?>> components = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        providers.put(type, (ComponentProvider<T>) context -> instance);
    }

    public <T, Implementation extends T>
    void bind(Class<T> type, Class<Implementation> implementation) {
        providers.put(type, new InjectionProvider<>(implementation));
    }

    public <Type> void bind(Class<Type> type, Type instance, Annotation qualifier) {
        components.put(new Component(type, qualifier), context -> instance);
    }

    record Component(Class<?> type, Annotation qualifier) {}

    public Context getContext() {
        providers.keySet()
                .forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {
            @Override
            public <ComponentType> Optional<ComponentType> get(Ref<ComponentType> ref) {
                if (ref.getQualifier() != null) {
                    return Optional.ofNullable(components
                                    .get(new Component(ref.getComponent(), ref.getQualifier())))
                            .map(provider -> (ComponentType)provider.get(this));
                }

                if (ref.isContainer()) {
                    if (ref.getContainer() != Provider.class) {
                        return Optional.empty();
                    }
                    return (Optional<ComponentType>) Optional.ofNullable(providers.get(ref.getComponent()))
                            .map(provider -> (Provider<Object>) () -> provider.get(this));
                }

                return Optional.ofNullable(providers.get(ref.getComponent()))
                        .map(provider -> (ComponentType)provider.get(this));
            }
        };
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Context.Ref dependency : providers.get(component).getDependencies()) {

            Class<?> componentType = dependency.getComponent();

            if (!providers.containsKey(componentType)) {
                throw new DependencyNotFoundException(component, componentType);
            }

            if (!dependency.isContainer()) {
                if (visiting.contains(componentType)) {
                    throw new CyclicDependenciesFoundException(visiting);
                }
                visiting.push(componentType);
                checkDependencies(componentType, visiting);
                visiting.pop();
            }
        }
    }
}