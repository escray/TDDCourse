package geektime.tdd.di;

import jakarta.inject.Inject;

class InjectConstructor implements TestComponent {
    private Dependency dependency;

    @Inject
    public InjectConstructor(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return dependency;
    }
}
