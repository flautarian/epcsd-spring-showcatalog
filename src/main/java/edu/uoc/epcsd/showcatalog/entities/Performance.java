package edu.uoc.epcsd.showcatalog.entities;

import lombok.*;

import javax.persistence.*;
import java.sql.Time;
import java.util.Date;
import java.util.List;

@Entity
@ToString
@Getter
@Setter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    private Date date;

    @Column(name = "time")
    private Time time;

    @Column(name = "streamingurl")
    private String streamingUrl;

    @Column(name = "remainingseats")
    private Integer remainingSeats;

    @Column(name = "status")
    private String status;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "showid", nullable = false, updatable = false)
    private Show show;

}
