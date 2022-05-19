package geektime.tdd.di;

import jakarta.inject.Provider;

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
            public Optional<?> get(Ref ref) {
                if (ref.isContainer()) {
                    if (ref.getContainer() != Provider.class) {
                        return Optional.empty();
                    }
                    return Optional.ofNullable(providers.get(ref.getComponent()))
                            .map(provider -> (Provider<Object>) () -> provider.get(this));
                }

                return Optional.ofNullable(providers.get(ref.getComponent()))
                        .map(provider -> provider.get(this));
            }
        };
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Context.Ref dependency : providers.get(component).getDependencyRefs()) {

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