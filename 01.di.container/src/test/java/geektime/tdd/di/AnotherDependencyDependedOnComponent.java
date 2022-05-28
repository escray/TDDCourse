package geektime.tdd.di;

import jakarta.inject.Inject;

class AnotherDependencyDependedOnComponent implements AnotherDependency {
    private TestComponent component;

    @Inject
    public AnotherDependencyDependedOnComponent(TestComponent component) {
        this.component = component;
    }
}
