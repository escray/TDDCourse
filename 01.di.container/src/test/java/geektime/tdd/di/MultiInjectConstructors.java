package geektime.tdd.di;

import jakarta.inject.Inject;

class MultiInjectConstructors implements Component {
    @Inject
    public MultiInjectConstructors(String name, Double value) {
    }

    @Inject
    public MultiInjectConstructors(String name) {
    }

    @Override
    public Dependency dependency() {
        return null;
    }
}
