package geektime.tdd.di;

import jakarta.inject.Inject;

class InjectConstructor implements Component {
    private Dependency dependency;

    @Inject
    public InjectConstructor(Dependency dependency) {
        this.dependency = dependency;
    }

    public Dependency getDependency() {
        return dependency;
    }

    @Override
    public Dependency dependency() {
        return dependency;
    }
}
