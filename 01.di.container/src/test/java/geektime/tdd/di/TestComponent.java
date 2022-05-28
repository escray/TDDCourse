package geektime.tdd.di;

interface TestComponent {
    default Dependency dependency() {
        return null;
    };
}
