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

            private Optional getComponent(Class type) {
                Ref ref = Ref.of(type);

                return Optional.ofNullable(providers.get(ref.getComponent()))
                        .map(provider -> provider.get(this));
            }

            private Optional getContainer(ParameterizedType type) {
                Ref ref = Ref.of(type);

                if (ref.getContainer() != Provider.class) {
                    return Optional.empty();
                }
                return Optional.ofNullable(providers.get(ref.getComponent()))
                        .map(provider -> (Provider<Object>) () -> provider.get(this));
            }
        };
    }

    // Concept missing
    // ComponentRef, ContainerRef -> Ref
    // 数据封装
    static class Ref {
        private Type container;
        private Class<?> component;

        Ref(ParameterizedType container) {
            this.container = container.getRawType();
            this.component = (Class<?>) container.getActualTypeArguments()[0];
        }

        Ref(Class<?> component) {
            this.component = component;
        }

        static Ref of(Type type) {
            if (type instanceof ParameterizedType container) {
                return new Ref(container);
            }
            return new Ref((Class<?>) type);
        }

        public Type getContainer() {
            return container;
        }

        public Class<?> getComponent() {
            return component;
        }
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
        Class<?> componentType = getComponentType(dependency);

        if (!providers.containsKey(componentType)) {
            throw new DependencyNotFoundException(component, componentType);
        }
    }

    private void checkComponentDependency(Class<?> component, Stack<Class<?>> visiting, Class<?> dependency) {Type containerType = null;
        Class<?> componentType = dependency;

        if (!providers.containsKey(componentType)) {
            throw new DependencyNotFoundException(component, componentType);
        }
        if (visiting.contains(componentType)) {
            throw new CyclicDependenciesFoundException(visiting);
        }
        visiting.push(componentType);
        checkDependencies(componentType, visiting);
        visiting.pop();
    }
}