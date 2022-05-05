package edu.uoc.epcsd.showcatalog.pojos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ShowData {
    private String name;
    private String description;
    private String image;
    private float price;
    private int duration;
    private int capacity;
    private Date onSaleDate;
    private String status;
    private List<Long> idCategories;
}
