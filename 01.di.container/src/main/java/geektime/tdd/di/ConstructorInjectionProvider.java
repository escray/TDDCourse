package geektime.tdd.di;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.util.Arrays.stream;

class ConstructorInjectionProvider<T> implements ComponentProvider<T> {
    private Class<?> componentType;
    private Constructor<T> injectConstructor;
    private boolean constructive = false;

    public ConstructorInjectionProvider(Class<?> componentType, Constructor<T> injectConstructor) {
        this.componentType = componentType;
        this.injectConstructor = injectConstructor;
    }

    @Override
    public T get(Context context) {
        if (constructive) throw new CyclicDependenciesFoundException(componentType);
        try {
            constructive = true;
            Object[] dependencies = stream(injectConstructor.getParameters())
                    .map(p -> context.get(p.getType())
                            .orElseThrow(() -> new DependencyNotFoundException(componentType, p.getType())))
                    .toArray(Object[]::new);
            return injectConstructor.newInstance(dependencies);
        } catch (CyclicDependenciesFoundException e) {
            throw new CyclicDependenciesFoundException(componentType, e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            constructive = false;
        }
    }
}