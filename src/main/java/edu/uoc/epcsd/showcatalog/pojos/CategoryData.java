package edu.uoc.epcsd.showcatalog.pojos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CategoryData {
    private String name;
    private String description;
}
