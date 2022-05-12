package edu.uoc.epcsd.showcatalog.services.impl;

import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.pojos.CategoryData;
import edu.uoc.epcsd.showcatalog.repositories.CategoryRepository;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import edu.uoc.epcsd.showcatalog.services.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ShowRepository showRepository;

    @Override
    public boolean afegirShow(Long idCat, Long idShow) throws Exception {
        try{
            Category category = categoryRepository.findById(idCat).orElse(null);
            Show show = showRepository.findById(idShow).orElse(null);
            if(Objects.isNull(category))throw new Exception("Categoria no trobada");
            if(Objects.isNull(show)) throw new Exception("Show no trobat");
            if(Objects.nonNull(category.getShows()) && !category.getShows().isEmpty()){
                if(category.getShows().stream().anyMatch(s->s.getName().equals(show.getName())))
                    throw new Exception("La categoria ja conte un show amb el mateix nom");
            }

            category.addShow(show);
            categoryRepository.saveAndFlush(category);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public boolean destruirCategoria(Long idCat) {
        try{
            categoryRepository.deleteById(idCat);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Category crearCategoria(CategoryData catData) {
        Category newCategory = new Category(catData);
        return categoryRepository.saveAndFlush(newCategory);
    }

    @Override
    public Category consultaCategoria(Long idCat) {
        return categoryRepository.findById(idCat).orElse(null);
    }

    @Override
    public List<Category> consultaTotesCategories() {
        return categoryRepository.findAll();
    }
}
