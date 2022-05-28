package geektime.tdd.di;

import jakarta.inject.Inject;

class MultiInjectConstructors implements TestComponent {
    @Inject
    public MultiInjectConstructors(String name, Double value) {
    }

    @Inject
    public MultiInjectConstructors(String name) {
    }
}
