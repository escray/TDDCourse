package geektime.tdd.di;

import jakarta.inject.Inject;

abstract class AbstractComponent implements TestComponent {
    @Inject
    public AbstractComponent() {

    }
}
