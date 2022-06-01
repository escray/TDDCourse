package geektime.tdd.di;

import jakarta.inject.Inject;
import jakarta.inject.Qualifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;

class InjectionProvider<T> implements ComponentProvider<T> {
    private final List<Field> injectFields;

    private Injectable<Constructor<T>> injectConstructor;
    private List<Injectable<Method>> injectMethods;

    public InjectionProvider(Class<T> component) {
        if (Modifier.isAbstract(component.getModifiers())) {
            throw new IllegalComponentException();
        }

        Injectable<Constructor<T>> injectable = getInjectable(getInjectConstructor(component));
        this.injectConstructor = injectable;

        this.injectMethods = getInjectMethods(component).stream().map(m -> getInjectable(m)).toList();
        this.injectFields = getInjectFields(component);


        if (injectFields.stream().anyMatch(f -> Modifier.isFinal(f.getModifiers()))) {
            throw new IllegalComponentException();
        }

        if (injectMethods.stream().map(Injectable::element).anyMatch(m -> m.getTypeParameters().length != 0)) {
            throw new IllegalComponentException();
        }

        List<ComponentRef<?>> dependencies = getDependencies();
    }

    private <Element extends Executable> Injectable<Element> getInjectable(Element constructor) {

        return new Injectable<>(constructor, stream(constructor.getParameters())
                .map(InjectionProvider::toComponentRef)
                .toArray(ComponentRef<?>[]::new));
    }

    @Override
    public T get(Context context) {
        try {
            T instance = injectConstructor.element()
                    .newInstance(injectConstructor.toDependencies(context));

            for (Field field : injectFields) {
                field.set(instance, toDependency(context, field));
            }
            for (Injectable<Method> method : injectMethods) {
                method.element().invoke(instance, method.toDependencies(context));
            }
            return instance;
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ComponentRef<?>> getDependencies() {
        return concat(concat(stream(injectConstructor.required()),
                        injectFields.stream().map(InjectionProvider::toComponentRef)),
                injectMethods.stream().flatMap(m -> stream(m.required()))).toList();
//                injectMethods.stream().flatMap(m -> stream(m.getParameters()).map(InjectionProvider::toComponentRef))).toList();
    }

    static record Injectable<Element extends AccessibleObject>(Element element, ComponentRef<?>[] required) {
        Object[] toDependencies(Context context) {
            return stream(required).map(context::get).map(Optional::get).toArray();
        }
    }

    private static <T> List<Field> getInjectFields(Class<T> component) {
        return traverse(component,
                (fields, current) -> injectable(current.getDeclaredFields()).toList());
    }

    private static <T> List<Method> getInjectMethods(Class<T> component) {
        List<Method> injectMethods = traverse(component,
                (methods, current) -> injectable(current.getDeclaredMethods())
                        .filter(m -> isOverrideByInjectMethod(methods, m))
                        .filter(m -> isOverrideByNoInjectMethod(component, m))
                        .toList());
        Collections.reverse(injectMethods);
        return injectMethods;
    }

    private static <Type> Constructor<Type> getInjectConstructor(Class<Type> implementation) {
        List<Constructor<?>> injectConstructors =
                injectable(implementation.getConstructors()).toList();

        if (injectConstructors.size() > 1) {
            throw new IllegalComponentException();
        }

        return (Constructor<Type>) injectConstructors.stream().findFirst()
                .orElseGet(() -> defaultConstructor(implementation));
    }

    private static <Type> Constructor<Type> defaultConstructor(Class<Type> implementation) {
        try {
            return implementation.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalComponentException();
        }
    }

    private static <T> List<T> traverse(Class<?> component, BiFunction<List<T>, Class<?>, List<T>> finder) {
        List<T> members = new ArrayList<>();
        Class<?> current = component;

        while (current != Object.class) {
            members.addAll(finder.apply(members, current));
            current = current.getSuperclass();
        }
        return members;
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

    private static Object[] toDependencies(Context context, Executable executable) {
        return stream(executable.getParameters())
                .map(p -> toDependency(context, toComponentRef(p)))
                .toArray(Object[]::new);
    }

    private static Object toDependency(Context context, Field field) {
        return toDependency(context, toComponentRef(field));
    }

    private static Object toDependency(Context context, ComponentRef of) {
        return context.get(of).get();
    }

    // TODO: 2 -> 1 ?
    private static ComponentRef<?> toComponentRef(Field field) {
        return ComponentRef.of(field.getGenericType(), getQualifier(field));
    }

    private static ComponentRef<?> toComponentRef(Parameter parameter) {
        return ComponentRef.of(parameter.getParameterizedType(), getQualifier(parameter));
    }

    private static Annotation getQualifier(AnnotatedElement element) {
        List<Annotation> qualifiers = stream(element.getAnnotations())
                .filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class)).toList();
        if (qualifiers.size() > 1) {
            throw new IllegalComponentException();
        }
        return qualifiers.stream().findFirst().orElse(null);
    }
}