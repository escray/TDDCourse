package geektime.tdd.di;

import jakarta.inject.Provider;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;

import java.lang.annotation.Annotation;
import java.util.*;

public class ContextConfig {
    private final Map<Component, ComponentProvider<?>> components = new HashMap<>();

    // bind instance
    public <Type> void bind(Class<Type> type, Type instance) {
        components.put(new Component(type, null), (ComponentProvider<Type>) context -> instance);
    }

    public <Type> void bind(Class<Type> type, Type instance, Annotation... qualifiers) {
        if (Arrays.stream(qualifiers).anyMatch(q -> !q.annotationType().isAnnotationPresent(Qualifier.class))) {
            throw new IllegalComponentException();
        }
        for (Annotation qualifier : qualifiers) {
            components.put(new Component(type, qualifier), context -> instance);
        }
    }

    // bind component
    public <Type, Implementation extends Type>
    void bind(Class<Type> type, Class<Implementation> implementation) {
        components.put(new Component(type, null), new InjectionProvider<>(implementation));
    }

    public <Type, Implementation extends Type>
    void bind(Class<Type> type, Class<Implementation> implementation, Annotation... annotations) {
        if (Arrays.stream(annotations).map(Annotation::annotationType)
                .anyMatch(t -> !t.isAnnotationPresent(Qualifier.class)
                        && !t.isAnnotationPresent(Scope.class))) {
            throw new IllegalComponentException();
        }

        List<Annotation> qualifiers = Arrays.stream(annotations)
                .filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class)).toList();

        Optional<Annotation> scope = Arrays.stream(annotations).filter(a -> a.annotationType().isAnnotationPresent(Scope.class)).findFirst();


        ComponentProvider<Implementation> injectionProvider = new InjectionProvider<>(implementation);
        ComponentProvider<Implementation> provider = scope
                .map(s -> (ComponentProvider<Implementation>) new SingletonProvider<>(injectionProvider))
                .orElse(injectionProvider);

        if (qualifiers.isEmpty()) {
            components.put(new Component(type, null), provider);
        }

        for (Annotation qualifier : qualifiers) {
            components.put(new Component(type, qualifier), provider);
        }
    }

    static class SingletonProvider<T> implements ComponentProvider<T> {
        private T singleton;
        private ComponentProvider<T> provider;

        public SingletonProvider(ComponentProvider<T> provider) {
            this.provider = provider;
        }

        @Override
        public T get(Context context) {
            if (singleton == null) {
                singleton = provider.get(context);
            }
            return singleton;
        }
    }


    public Context getContext() {
        components.keySet().forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {
            @Override
            public <ComponentType> Optional<ComponentType> get(ComponentRef<ComponentType> componentRef) {
                if (componentRef.isContainer()) {
                    if (componentRef.getContainer() != Provider.class) {
                        return Optional.empty();
                    }
                    return (Optional<ComponentType>) Optional.ofNullable(getProvider(componentRef))
                            .map(provider -> (Provider<Object>) () -> provider.get(this));
                }

                return Optional.ofNullable(getProvider(componentRef))
                        .map(provider -> (ComponentType)provider.get(this));
            }
        };
    }

    private <ComponentType> ComponentProvider<?> getProvider(ComponentRef<ComponentType> componentRef) {
        return components.get(componentRef.component());
    }

    private void checkDependencies(Component component, Stack<Component> visiting) {
        for (ComponentRef dependency : components.get(component).getDependencies()) {
            if (!components.containsKey(dependency.component())) {
                throw new DependencyNotFoundException(component, dependency.component());
            }

            if (!dependency.isContainer()) {
                if (visiting.contains(dependency.component())) {
                    throw new CyclicDependenciesFoundException(visiting);
                }
                visiting.push(dependency.component());
                checkDependencies(dependency.component(), visiting);
                visiting.pop();
            }
        }
    }
}