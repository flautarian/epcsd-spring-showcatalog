package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.pojos.CategoryData;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.repositories.CategoryRepository;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import javassist.tools.rmi.ObjectNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Log4j2
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ShowRepository showRepository;

    @PostMapping("/crear")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity crear(@RequestBody CategoryData cat) {
        log.trace("Executant endpoint: 'Crear categoria'");
        Category newCategory = new Category(cat);
        return new ResponseEntity<>(categoryRepository.saveAndFlush(newCategory), HttpStatus.CREATED);
    }

    @PostMapping("/{idCat}/afegirShow/{idShow}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity afegirShow(@PathVariable Long idCat, @PathVariable Long idShow) {
        log.trace("Executant endpoint: 'Afegir acte a categoria'");
        try{
            Show processedShow = categoryRepository.findById(idCat).map(category -> {
                if (Objects.nonNull(idShow) && idShow != 0L) {
                    Show _show = showRepository.findById(idShow)
                            .orElseThrow(() -> new ResourceNotFoundException("Not found Show with id = " + idShow));
                    category.addShow(_show);
                    categoryRepository.save(category);
                    return _show;
                }
                else throw new ResourceNotFoundException("Invalid show with id = " + idCat);
            }).orElseThrow(() -> new ResourceNotFoundException("Not found category with id = " + idCat));
            return new ResponseEntity<>(processedShow, HttpStatus.CREATED);
        }
        catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
        }
    }

    @DeleteMapping("/destruir/{idCat}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity destruir(@PathVariable Long idCat) {
        log.trace("Executant endpoint: 'Eliminar categoria'");
        try{
            categoryRepository.deleteById(idCat);
            return new ResponseEntity<>("Categoria eliminada correctament", HttpStatus.OK);
        }catch(EmptyResultDataAccessException e){
            return new ResponseEntity<>("Error: Categoria no existent", HttpStatus.OK);
        }
    }

    @GetMapping(path = "/consulta", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity consulta(@RequestParam(required = false) Long idCat) throws ObjectNotFoundException {
        log.trace("Executant endpoint: 'Consulta de categories'");
        if(Objects.nonNull(idCat)){
            Category categoryResult = categoryRepository.findById(idCat).orElse(null);
            return new ResponseEntity<>(Objects.nonNull(categoryResult)? categoryResult : "Categoria no trobada", HttpStatus.OK);
        }
        return new ResponseEntity<>(categoryRepository.findAll(), HttpStatus.OK);
    }
}
