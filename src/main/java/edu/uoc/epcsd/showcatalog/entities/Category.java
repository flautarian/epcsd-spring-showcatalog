package edu.uoc.epcsd.showcatalog.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.uoc.epcsd.showcatalog.pojos.CategoryData;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@ToString(exclude = "shows")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "categories")
    private Set<Show> shows = new HashSet<>();

    public Category(CategoryData cat) {
        this.name = cat.getName();
        this.description = cat.getDescription();
    }

    // getters and setters
    public void addShow(Show show) {
        this.shows.add(show);
        show.getCategories().add(this);
    }

    public void removeShow(long showId) {
        Show show = this.shows.stream().filter(sh -> sh.getId() == showId).findFirst().orElse(null);
        if (show != null) this.shows.remove(show);
        show.getCategories().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id) && Objects.equals(name, category.name) && Objects.equals(description, category.description) && Objects.equals(shows, category.shows);
    }
}
