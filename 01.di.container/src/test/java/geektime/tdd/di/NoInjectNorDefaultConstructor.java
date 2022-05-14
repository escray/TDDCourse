package geektime.tdd.di;

public class NoInjectNorDefaultConstructor implements Component {
    public NoInjectNorDefaultConstructor(String name) {
    }

    @Override
    public Dependency dependency() {
        return null;
    }
}
