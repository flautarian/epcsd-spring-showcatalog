package edu.uoc.epcsd.showcatalog.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.uoc.epcsd.showcatalog.pojos.ShowData;
import lombok.*;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.*;

@Entity
@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "image")
    private String image;

    @Column(name = "price")
    private float price;

    @Column(name = "duration")
    private int duration;

    @Column(name = "capacity")
    private int capacity;

    @Column(name = "onsaledate")
    private Date onSaleDate;

    @Column(name = "status")
    private String status;

    @JsonIgnore
    @ElementCollection(fetch = FetchType.LAZY)
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @CollectionTable(name="performance", joinColumns = @JoinColumn(name = "showid"))
    private List<Performance> performances;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST,
                    CascadeType.MERGE})
    @JoinTable(
            name = "show_categories",
            joinColumns = @JoinColumn(name = "id_show"),
            inverseJoinColumns = @JoinColumn(name = "id_category")
    )
    private List<Category> categories;

    public Show(ShowData show) {
        this.name = show.getName();
        this.description = show.getDescription();
        this.image = show.getImage();
        this.price = show.getPrice();
        this.duration = show.getDuration();
        this.capacity = show.getCapacity();
        this.onSaleDate = show.getOnSaleDate();
        this.status = show.getStatus();
        this.categories = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Show show = (Show) o;
        return Float.compare(show.price, price) == 0 && duration == show.duration && capacity == show.capacity && Objects.equals(id, show.id) && Objects.equals(name, show.name) && Objects.equals(description, show.description) && Objects.equals(image, show.image) && Objects.equals(onSaleDate, show.onSaleDate) && Objects.equals(status, show.status) && Objects.equals(performances, show.performances) && Objects.equals(categories, show.categories);
    }
}
