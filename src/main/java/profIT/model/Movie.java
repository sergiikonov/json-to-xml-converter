package profIT.model;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Movie {
    private String title;
    private int releaseYear;
    private List<String> genres;
    private String director;
}
