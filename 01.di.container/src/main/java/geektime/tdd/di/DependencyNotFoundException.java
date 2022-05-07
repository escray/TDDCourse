package geektime.tdd.di;

public class DependencyNotFoundException extends RuntimeException {
    public DependencyNotFoundException(Class<?> dependency) {
        this.dependency = dependency;
    }

    public DependencyNotFoundException(Class<?> component, Class<?> dependency) {
        this.dependency = dependency;
        this.component = component;
    }

    private Class<?> dependency;
    private Class<?> component;

    public Class<?> getDependency() {
        return dependency;
    }

    public Class<?> getComponent() {
        return component;
    }
}