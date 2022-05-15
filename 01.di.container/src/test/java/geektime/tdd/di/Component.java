package geektime.tdd.di;

interface Component {
    default Dependency dependency() {
        return null;
    };
}
