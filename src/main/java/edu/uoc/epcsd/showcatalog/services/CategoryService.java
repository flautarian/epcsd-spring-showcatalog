package edu.uoc.epcsd.showcatalog.services;

import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.pojos.CategoryData;

import java.util.List;

public interface CategoryService {

    public boolean afegirShow(Long idCat, Long idShow) throws Exception;

    public boolean destruirCategoria(Long idCat);

    public Category crearCategoria(CategoryData catData);

    public Category consultaCategoria(Long idCat);

    public List<Category> consultaTotesCategories();
}
