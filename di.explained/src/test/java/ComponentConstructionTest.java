import com.google.inject.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.inject.Qualifier;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

public class ComponentConstructionTest {

    interface Car {
        Engine getEngine();
    }

    interface Engine {
        String getName();
    }

    static class V6Engine implements Engine {
        @Override
        public String getName() {
            return "V6";
        }
    }

    static class V8Engine implements Engine {
        @Override
        public String getName() {
            return "V8";
        }
    }

    @Nested
    public class ConstructorInjection {
        static class CarInjectConstructor implements Car {
            private Engine engine;

            @Inject
            public CarInjectConstructor(Engine engine) {
                this.engine = engine;
            }

            @Override
            public Engine getEngine() {
                return engine;
            }
        }

        @Test
        public void construction_injection() {
            Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Engine.class).to(V8Engine.class);
                    bind(Car.class).to(CarInjectConstructor.class);
                }
            });

            Car car = injector.getInstance(Car.class);
            assertEquals("V8", car.getEngine().getName());
        }

        @Test
        public void selection() {
            Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Engine.class).annotatedWith(new NameLiteral("V8")).to(V8Engine.class);
                    bind(Engine.class).annotatedWith(new NameLiteral("V6")).to(V6Engine.class);
                    bind(Engine.class).annotatedWith(new LuxuryLiteral()).to(V8Engine.class);
                    bind(Car.class).to(LuxuryCar.calss);
                }
            });
        }

        // 强类型
        @Qualifier
        @Target({FIELD, PARAMETER, METHOD})
        @Retention(RUNTIME)
        public @interface Luxury {

        }

        record LuxuryLiteral() implements Luxury {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }
        }

        static class LuxuryCar implements Car {
            private @Inject @Luxury Engine engine;

            @Override
            public Engine getEngine() {
                return engine;
            }
        }

        interface Window {
        }
        interface Wheel {
        }



        // 一个对象存在多个注入点
        // 可行，但不会有人这么干
        static class CarWithInjectionPoints implements Car {
            @Inject
            private Window window;

            private Engine engine;

            public CarWithInjectionPoints(Engine engine) {
                this.engine = engine;
            }

            @Inject
            public void install(Wheel wheel) {

            }

            @Override
            public Engine getEngine() {
                return engine;
            }
        }

        record NameLiteral(String value) implements Named {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Named.class;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Object getPayload() {
                return null;
            }
        }



        static class CarInjectField implements Car {
            @Inject
            private Engine engine;

            @Override
            public Engine getEngine() {
                return engine;
            }
        }

        static class CarInjectMethod implements Car {
            @Inject
            private Engine engine;

            @Override
            public Engine getEngine() {
                return engine;
            }

            @Inject
            public void install(Engine engine) {
                this.engine = engine;
            }
        }
    }

    // 循环依赖
    @Nested
    public class DependencySelection {
        static class A {
            private B b;
            // private Provider<B> b;

//            public A(Provider<B> b) {
//                this.b = b;
//            }

            @Inject
            public A(B b) {
                this.b = b;
            }

            public B getB() {
                return b;
            }
        }

        static class B {
            private A a;

            @Inject
            public B(A a) {
                this.a = a;
            }

            public A getA() {
                return a;
            }
        }

        @Test
        public void cyclic_dependencies() {
            Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(A.class);
                    bind(B.class);
                }
            });

            A a = injector.getInstance(A.class);
            assertNotNull(a.getB());

        }
    }

    @Nested
    public class ContextInScope {
        @Test
        public void singleton() {
            Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
//                    bindScope(BatchScoped.class, new SingletonScope());
                    bind(Engine.class).annotatedWith(new DependencySelection.NameLiteral("V8")).to(V8Engine.class).in(Singleton.class);
                    bind(Engine.class).annotatedWith(new DependencySelection.NameLiteral("V6")).to(V6Engine.class);
                    bind(Car.class).to(DependencySelection.V8Car.class);
                }
            });

            Car car1 = injector.getInstance(Car.class);
            Car car2 = injector.getInstance(Car.class);

            assertNotSame(car1, car2);

            assertSame(car1.getEngine(), car2.getEngine());
        }

        @Target({TYPE, METHOD})
        @Retention(RUNTIME)
        @jakarta.inject.Scope
        public @interface BatchScoped {}
    }
}

// JSR 330
// 1. 不同类型的注入点的支持
// 2. 对于依赖的选择
// 3. 对于 Scope 的判断
// 开发任何一个需求之前，要了解需求是什么样子的。

// 怎么形成配置文件
// Guice 使用 DSL

// 生命周期回调
