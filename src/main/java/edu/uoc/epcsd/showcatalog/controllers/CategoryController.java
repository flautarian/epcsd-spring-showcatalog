package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.pojos.CategoryData;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.repositories.CategoryRepository;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import edu.uoc.epcsd.showcatalog.services.CategoryService;
import edu.uoc.epcsd.showcatalog.services.ShowService;
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
    private CategoryService categoryService;

    @Autowired
    private ShowService showService;

    @PostMapping("/crear")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity crear(@RequestBody CategoryData cat) {
        log.trace("Executant endpoint: 'Crear categoria'");
        try{
            return new ResponseEntity<>(categoryService.crearCategoria(cat), HttpStatus.OK);
        }
        catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.OK);
        }
    }

    @PostMapping("/{idCat}/afegirShow/{idShow}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity afegirShow(@PathVariable Long idCat, @PathVariable Long idShow) {
        log.trace("Executant endpoint: 'Afegir acte a categoria'");
        try{
            if (Objects.nonNull(idShow) && idShow != 0L && Objects.nonNull(idCat) && idCat != 0L) {
                categoryService.afegirShow(idCat, idShow);
                return new ResponseEntity("Show " + idShow + " afegit a la categoria " + idCat, HttpStatus.OK);
            }
            else return new ResponseEntity("Error, reviseu parametres", HttpStatus.OK);
        }
        catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity<>("Error, " + e.getMessage(), HttpStatus.OK);
        }
    }

    @DeleteMapping("/destruir/{idCat}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity destruir(@PathVariable Long idCat) {
        log.trace("Executant endpoint: 'Eliminar categoria'");
        if(categoryService.destruirCategoria(idCat)){
            return new ResponseEntity<>("Categoria eliminada correctament", HttpStatus.OK);
        }
        else return new ResponseEntity<>("Error: Categoria no existent", HttpStatus.OK);
    }

    @GetMapping(path = "/consulta", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity consulta(@RequestParam(required = false) Long idCat) throws ObjectNotFoundException {
        log.trace("Executant endpoint: 'Consulta de categories'");
        if(Objects.nonNull(idCat)){
            Category categoria = categoryService.consultaCategoria(idCat);
            return new ResponseEntity<>(Objects.nonNull(categoria) ? categoria : "Categoria no trobada", HttpStatus.OK);
        }
        return new ResponseEntity<>(categoryService.consultaTotesCategories(), HttpStatus.OK);
    }
}
