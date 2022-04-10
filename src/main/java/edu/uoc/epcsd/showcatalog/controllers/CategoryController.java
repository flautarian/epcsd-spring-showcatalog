package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.repositories.CategoryRepository;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
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
    public List<Category> getAllCategories() {
        log.trace("getAllCategories");
        return categoryRepository.findAll();
    }

    @PostMapping("/crear")
    @ResponseStatus(HttpStatus.OK)
    public Category crear(@RequestBody Category cat) {
        log.trace("getAllCategories");
        return categoryRepository.saveAndFlush(cat);
    }

    @PostMapping("/{idCat}/afegirShow")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Show> subscriure(@PathVariable Long idCat, @RequestBody Show show) {
        log.trace("subscribint show");
        Show processedShow = categoryRepository.findById(idCat).map(category -> {
            long showId = show.getId();

            // tag is existed
            if (showId != 0L) {
                Show _show = showRepository.findById(showId)
                        .orElseThrow(() -> new ResourceNotFoundException("Not found Show with id = " + showId));
                category.addShow(_show);
                categoryRepository.save(category);
                return _show;
            }

            // add and create new Tag
            category.addShow(show);
            return showRepository.save(show);
        }).orElseThrow(() -> new ResourceNotFoundException("Not found Tutorial with id = " + idCat));
        return new ResponseEntity<>(processedShow, HttpStatus.CREATED);
    }

    @PostMapping("/destruir/{idCat}")
    @ResponseStatus(HttpStatus.OK)
    public void destruir(@PathVariable Long idCat) {
        log.trace("delete category : " + idCat);
        categoryRepository.deleteById(idCat);
    }

    @GetMapping(path = "/consulta/{idCat}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Category consulta(@PathVariable Long idCat) {
        log.trace("getting category : " + idCat);
        return categoryRepository.getById(idCat);
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
