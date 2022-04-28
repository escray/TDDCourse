package geektime.tdd.di;

import jakarta.inject.Provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.util.Arrays.stream;

class ConstructorInjectionProvider<T> implements Provider<T> {
    private final Context context;
    private Constructor<T> injectConstructor;
    private boolean constructive = false;

    public ConstructorInjectionProvider(Context context, Constructor<T> injectConstructor) {
        this.context = context;
        this.injectConstructor = injectConstructor;
    }

    @Override
    public T get() {
        if (constructive) throw new CyclicDependenciesFound();
        try {
            constructive = true;
            Object[] dependencies = stream(injectConstructor.getParameters())
                    .map(p -> context.get(p.getType()).orElseThrow(DependencyNotFoundException::new))
                    .toArray(Object[]::new);
            return injectConstructor.newInstance(dependencies);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            constructive = false;
        }
    }
}
