package geektime.tdd.di;

public class DependencyNotFoundException extends RuntimeException {
    public DependencyNotFoundException(Class<?> dependency) {
        this.dependency = dependency;
    }

    private Class<?> dependency;

    public Class<?> getDependency() {
        return dependency;
    }
}