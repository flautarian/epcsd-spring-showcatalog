package edu.uoc.epcsd.showcatalog.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.uoc.epcsd.showcatalog.deserializers.SqlTimeDeserializer;
import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Time;
import java.util.Date;
import java.util.List;

@ToString
@Getter
@Setter
@Immutable
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Performance implements Serializable {

    @JsonFormat(pattern = "dd/MM/YYYY")
    @Column(name = "date")
    private Date date;

    @JsonFormat(pattern = "HH:mm")
    @JsonDeserialize(using = SqlTimeDeserializer.class)
    @Column(name = "time")
    private Time time;


    @Column(name = "streamingurl")
    private String streamingUrl;

    @Column(name = "remainingseats")
    private Integer remainingSeats;

    @Column(name = "status")
    private String status;

}
