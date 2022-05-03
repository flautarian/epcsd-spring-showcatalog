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
import java.util.Objects;

@ToString
@Getter
@Setter
@Immutable
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Performance implements Serializable {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy", timezone = "Europe/Madrid")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Performance that = (Performance) o;
        return Objects.equals(date, that.date) && Objects.equals(time, that.time) && Objects.equals(streamingUrl, that.streamingUrl) && Objects.equals(remainingSeats, that.remainingSeats) && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, streamingUrl, remainingSeats, status);
    }
}
