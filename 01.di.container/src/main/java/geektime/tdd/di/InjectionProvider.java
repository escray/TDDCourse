package geektime.tdd.di;

import jakarta.inject.Inject;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;

class InjectionProvider<T> implements ComponentProvider<T> {
    private final Constructor<T> injectConstructor;
    private final List<Field> injectFields;
    private final List<Method> injectMethods;

    public InjectionProvider(Class<T> component) {
        if (Modifier.isAbstract(component.getModifiers())) {
            throw new IllegalComponentException();
        }

        this.injectConstructor = getInjectConstructor(component);
        this.injectFields = getInjectFields(component);
        this.injectMethods = getInjectMethods(component);

        if (injectFields.stream().anyMatch(f -> Modifier.isFinal(f.getModifiers()))) {
            throw new IllegalComponentException();
        }

        if (injectMethods.stream().anyMatch(m -> m.getTypeParameters().length != 0)) {
            throw new IllegalComponentException();
        }
    }

    @Override
    public T get(Context context) {
        try {
            Constructor<T> constructor = this.injectConstructor;
            Object[] dependencies = toDependencies(context, constructor);
            T instance = this.injectConstructor.newInstance(dependencies);
            for (Field field : injectFields) {
                field.set(instance, context.get(field.getType()).get());
            }
            for (Method method : injectMethods) {
                method.invoke(instance,
                        stream(method.getParameterTypes())
                        .map(t -> context.get(t).get())
                                .toArray(Object[]::new));
            }
            return instance;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] toDependencies(Context context, Constructor<T> constructor) {
        return stream(constructor.getParameterTypes())
                .map(t -> context.get(t).get())
                .toArray(Object[]::new);
    }

    @Override
    public List<Class<?>> getDependencies() {
        return concat(concat(stream(injectConstructor.getParameters())
                                .map(Parameter::getType),
                        injectFields.stream().map(Field::getType)),
                injectMethods.stream()
                        .flatMap(m -> stream(m.getParameterTypes())))
                .toList();
    }

    private static <T> List<Field> getInjectFields(Class<T> component) {
        List<Field> injectFields = new ArrayList<>();
        Class<?> current = component;
        while (current != Object.class) {
            injectFields.addAll(injectable(current.getDeclaredFields()).toList());
            current = current.getSuperclass();
        }
        return injectFields;
    }

    private static <T> List<Method> getInjectMethods(Class<T> component) {
        List<Method> injectMethods = new ArrayList<>();
        Class<?> current = component;
        while (current != Object.class) {
            injectMethods.addAll(injectable(current.getDeclaredMethods())
                    .filter(m -> isOverrideByInjectMethod(injectMethods, m))
                    .filter(m -> isOverrideByNoInjectMethod(component, m))
                    .toList());
            current = current.getSuperclass();
        }
        Collections.reverse(injectMethods);
        return injectMethods;
    }

    private static <Type> Constructor<Type> getInjectConstructor(Class<Type> implementation) {
        List<Constructor<?>> injectConstructors =
                injectable(implementation.getConstructors()).toList();

        if (injectConstructors.size() > 1) {
            throw new IllegalComponentException();
        }

        return (Constructor<Type>) injectConstructors.stream()
                .findFirst().orElseGet(() -> {
                    try {
                        return implementation.getDeclaredConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new IllegalComponentException();
                    }
                });
    }

    private static <T extends AnnotatedElement> Stream<T> injectable(T[] declaredFields) {
        return stream(declaredFields)
                .filter(f -> f.isAnnotationPresent(Inject.class));
    }

    private static boolean isOverride(Method o, Method m) {
        return o.getName().equals(m.getName()) &&
                Arrays.equals(o.getParameterTypes(), m.getParameterTypes());
    }

    private static <T> boolean isOverrideByNoInjectMethod(Class<T> component, Method m) {
        return stream(component.getDeclaredMethods())
                .filter(n -> !n.isAnnotationPresent(Inject.class))
                .noneMatch(o -> isOverride(o, m));
    }

    private static boolean isOverrideByInjectMethod(List<Method> injectMethods, Method m) {
        return injectMethods.stream().noneMatch(o -> isOverride(o, m));
    }
}