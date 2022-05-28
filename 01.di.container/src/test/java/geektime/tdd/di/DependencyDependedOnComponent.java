package geektime.tdd.di;

import jakarta.inject.Inject;

class DependencyDependedOnComponent implements Dependency {
    private TestComponent component;

    @Inject
    public DependencyDependedOnComponent(TestComponent component) {
        this.component = component;
    }
}
