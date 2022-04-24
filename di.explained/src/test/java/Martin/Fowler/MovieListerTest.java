package Martin.Fowler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MovieListerTest {
    MutablePicoContainer pico = configureContainer();

    private MutablePicoContainer configureContainer() {
        MutablePicoContainer pico = new DefaultPicoContainer();
        Pamameter[] finderParams = {new ConstantParameter("movie1.txt")};
        pico.registerComponentImplementation(MovieFinder.class, ColonMovieFinder.class, finderParams);
        pico.registerComponentImplementation(MovieLister.class);
        return pico;
    }

    private class MutablePicoContainer {
        public void registerComponentImplementation(Class<MovieFinder> movieFinderClass, Class<ColonMovieFinder> colonMovieFinderClass, Pamameter[] finderParams) {
        }

        public void registerComponentImplementation(Class<MovieLister> movieListerClass) {
        }

        public Object getComponentInstance(Class<MovieLister> movieListerClass) {
            return null;
        }
    }

    private class DefaultPicoContainer extends MutablePicoContainer {
    }

    private class Pamameter {
    }

    private class ConstantParameter extends Pamameter {
        public ConstantParameter(String filename) {
            super();
        }
    }

    @Test
    public void testWithPico() {
        MutablePicoContainer pico = configureContainer();
        MovieLister lister = (MovieLister) pico.getComponentInstance(MovieLister.class);
        Movie[] movies = lister.moviesDirectedBy("Sergio Leone");
        assertEquals("Once Upon a Time in the West", movies[0].getTitle());
    }

    @Test
    public void testWithSpring() throws Exception {
        ApplicationContext ctx = new FileSystemXmlApplication("spring.xml");
        MovieLister lister = (MovieLister) ctx.getBean("MovieLister");
        Movie[] movies = lister.moviesDirectedBy("Sergio Leone");
        assertEquals("Once Upon a Time in the West", movies[0].getTitle());
    }

    private class ApplicationContext {
        public Object getBean(String movieLister) {
            return null;
        }
    }

    private class FileSystemXmlApplication extends ApplicationContext {
        public FileSystemXmlApplication(String filename) {
            super();
        }
    }
}
