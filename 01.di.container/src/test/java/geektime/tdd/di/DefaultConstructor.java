package geektime.tdd.di;

class DefaultConstructor implements Component {
    public DefaultConstructor() {
    }

    @Override
    public Dependency dependency() {
        return null;
    }
}
