package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.repositories.CategoryRepository;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import javassist.tools.rmi.ObjectNotFoundException;
import lombok.extern.log4j.Log4j2;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ShowRepository showRepository;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Category>> getAllCategories() {
        log.trace("getAllCategories");
        return new ResponseEntity<>(categoryRepository.findAll(), HttpStatus.OK);
    }

    @PostMapping("/crear")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Category> crear(@RequestBody Category cat) {
        log.trace("Create category");
        return new ResponseEntity<>(categoryRepository.saveAndFlush(cat), HttpStatus.CREATED);
    }

    @PostMapping("/{idCat}/afegirShow/{idShow}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Show> afegirShow(@PathVariable Long idCat, @PathVariable Long idShow) {
        log.trace("subscribint show");
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

    @DeleteMapping("/destruir/{idCat}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> destruir(@PathVariable Long idCat) {
        log.trace("delete category : " + idCat);
        categoryRepository.deleteById(idCat);
        return new ResponseEntity<>("Categoria eliminada correctament", HttpStatus.OK);
    }

    @GetMapping(path = "/consulta/{idCat}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Category> consulta(@PathVariable Long idCat) throws ObjectNotFoundException {
        log.trace("getting category : " + idCat);
        Category categoryResult = categoryRepository.findById(idCat).orElse(null);
        return new ResponseEntity<>(categoryResult, HttpStatus.OK);
    }

    @GetMapping(path = "/shows/{idCat}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Set<Show> consultaShows(@PathVariable Long idCat) {
        log.trace("getting category shows of category : " + idCat);
        Category categoria = categoryRepository.getById(idCat);
        if(categoria!= null)
                return categoria.getShows();
        return null;
    }
}
