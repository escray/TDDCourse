package geektime.tdd.di;

public class DependencyNotFoundException extends RuntimeException {

    private final Component component;
    private final Component dependency;

    public DependencyNotFoundException(Component component, Component dependency) {
        this.dependency = dependency;
        this.component = component;
    }

    public Component getDependency() {
        return dependency;
    }

    public Component getComponent() {
        return component;
    }
}