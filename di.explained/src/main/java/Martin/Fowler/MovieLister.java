package Martin.Fowler;

import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class MovieLister {
    private MovieFinder finder;

    // origin
//    public MovieLister() {
//        finder = new ColonDelimitedMovieFinder("movies1.txt");
//    }

    // Constructor Injection
    public MovieLister(MovieFinder finder) {
        this.finder = finder;
    }

    // Setter Injection
    public void setFinder(MovieFinder finder) {
        this.finder = finder;
    }

    public Movie[] moviesDirectedBy(String arg) {
        List allMovies = finder.findAll();
        for (Iterator it = allMovies.iterator(); it.hasNext(); ) {
            Movie movie = (Movie) it.next();
            if (!movie.getDirector().equals(arg)) it.remove();
        }
        return (Movie[]) allMovies.toArray(new Movie[allMovies.size()]);
    }
}

