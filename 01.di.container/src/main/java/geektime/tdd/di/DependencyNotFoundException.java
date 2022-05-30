package geektime.tdd.di;

public class DependencyNotFoundException extends RuntimeException {

    private final Component componentComponent;
    private final Component dependencyComponent;

    public DependencyNotFoundException(Component componentComponent, Component dependencyComponent) {
        this.dependencyComponent = dependencyComponent;
        this.componentComponent = componentComponent;
    }

    public Component getDependencyComponent() {
        return dependencyComponent;
    }

    public Component getComponentComponent() {
        return componentComponent;
    }
}