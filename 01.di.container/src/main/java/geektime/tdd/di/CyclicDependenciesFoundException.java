package geektime.tdd.di;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CyclicDependenciesFoundException extends RuntimeException {
    private Set<Component> components = new HashSet<>();

    public CyclicDependenciesFoundException(List<Component> visiting) {
        components.addAll(visiting);
    }

    public Class<?>[] getComponents() {
//        return components.stream().toArray(Class<?>[]::new);
        return components.stream().map(Component::type).toArray(Class<?>[]::new);
    }
}
