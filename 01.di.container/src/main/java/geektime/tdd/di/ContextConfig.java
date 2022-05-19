package geektime.tdd.di;

import jakarta.inject.Provider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ContextConfig {
    private final Map<Class<?>, ComponentProvider<?>> providers = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        providers.put(type, (ComponentProvider<T>) context -> instance);
    }

    public <T, Implementation extends T>
    void bind(Class<T> type, Class<Implementation> implementation) {
        providers.put(type, new InjectionProvider<>(implementation));
    }

    public Context getContext() {
        providers.keySet()
                .forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {
            @Override
            public Optional get(Type type) {
                if (isContainerType(type)) {
                    return getContainer((ParameterizedType) type);
                }
                return getComponent((Class<?>) type);
            }

            private <T> Optional<T> getComponent(Class<T> type) {
                return Optional.ofNullable(providers.get(type))
                        .map(provider -> (T) provider.get(this));
            }

            private Optional getContainer(ParameterizedType type) {
                if (type.getRawType() != Provider.class) {
                    return Optional.empty();
                }
                Class<?> componentType = getComponentType(type);
                return Optional.ofNullable(providers.get(componentType))
                        .map(provider -> (Provider<Object>) () -> provider.get(this));
            }
        };
    }

    private Class<?> getComponentType(Type type) {
        return (Class<?>)((ParameterizedType) type).getActualTypeArguments()[0];
    }

    private boolean isContainerType(Type type) {
        return type instanceof ParameterizedType;
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Type dependency : providers.get(component).getDependencies()) {
            if (isContainerType(dependency)) {
                checkContainerTypeDependency(component, dependency);
            } else {
                checkComponentDependency(component, visiting, (Class<?>)dependency);
            }
        }
    }

    private void checkContainerTypeDependency(Class<?> component, Type dependency) {
        if (!providers.containsKey(getComponentType(dependency))) {
            throw new DependencyNotFoundException(component, getComponentType(dependency));
        }
    }

    private void checkComponentDependency(Class<?> component, Stack<Class<?>> visiting, Class<?> dependency) {
        if (!providers.containsKey(dependency)) {
            throw new DependencyNotFoundException(component, dependency);
        }
        if (visiting.contains(dependency)) {
            throw new CyclicDependenciesFoundException(visiting);
        }
        visiting.push(dependency);
        checkDependencies(dependency, visiting);
        visiting.pop();
    }
}