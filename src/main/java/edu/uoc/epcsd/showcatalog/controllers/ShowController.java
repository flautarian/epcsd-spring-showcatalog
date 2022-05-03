package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.entities.Performance;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.kafka.KafkaConstants;
import edu.uoc.epcsd.showcatalog.repositories.CategoryRepository;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import javassist.tools.rmi.ObjectNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.PathParam;
import java.lang.reflect.Parameter;
import java.sql.Time;
import java.util.*;

@Log4j2
@RestController
@RequestMapping("/show")
public class ShowController {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private KafkaTemplate<String, Show> kafkaTemplate;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<Show> getAllShows() {
        log.trace("getAllShows");
        return showRepository.findAll();
    }

    @GetMapping(path = "/consulta", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Show>> consulta(@RequestParam(required = false) Long idShow, @RequestParam(required = false) String showName, @RequestParam(required = false) List<Long> idCategories) throws ObjectNotFoundException {
        List<Show> showResults = null;
        if(Objects.nonNull(idShow) || Objects.nonNull(showName) || Objects.nonNull(idCategories)){
            showResults = Objects.nonNull(idShow) ? Arrays.asList(showRepository.findById(idShow).orElse(null)) :
                    Objects.nonNull(showName) ? showRepository.findFirstByName(showName) :
                            Objects.nonNull(idCategories) ? showRepository.findByCategoriesIdIn(idCategories) :
                                    null;
        }
        return new ResponseEntity<>(showResults, HttpStatus.OK);
    }

    @PostMapping("/crear")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Show> crear(@RequestBody Show show) {
        processarCategories(show);
        Show showResult = showRepository.saveAndFlush(show);
        if(Objects.nonNull(showResult)){
            // Emitim el missatge per el stream de kafka
            kafkaTemplate.send(KafkaConstants.SHOW_TOPIC + KafkaConstants.SEPARATOR + KafkaConstants.COMMAND_ADD, showResult);
            return new ResponseEntity<>(showResult, HttpStatus.CREATED);
        }
        else throw new ResourceNotFoundException("operacio invalida, reviseu parametres");
    }

    @DeleteMapping("/destruir/{idShow}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> destruir(@PathVariable Long idShow) {
        log.trace("delete show : " + idShow);
        showRepository.deleteById(idShow);
        return new ResponseEntity<>("Show eliminat", HttpStatus.CREATED);
    }

    @PostMapping("/{idShow}/crearActuacio")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Show> crear(@PathVariable Long idShow ,@RequestBody Performance performance) {
        if(Objects.nonNull(idShow) && idShow != 0L){
            Show show = showRepository.findById(idShow).orElse(null);
            if(Objects.nonNull(show)){
                if(Objects.isNull(show.getPerformances()))show.setPerformances(new ArrayList<Performance>());
                if(show.getPerformances().indexOf(performance) < 0)
                    show.getPerformances().add(performance);
                else
                    throw new ResourceNotFoundException("operacio invalida, actuacio existent");
                return new ResponseEntity<>(showRepository.saveAndFlush(show), HttpStatus.CREATED);
            } else throw new ResourceNotFoundException("operacio invalida, reviseu parametres");
        }
        else throw new ResourceNotFoundException("operacio invalida, reviseu parametres");
    }

    @GetMapping("/{idShow}/llistarActuacions")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Performance>> llistarActuacions(@PathVariable("idShow") Long idShow) {
        Show showResult = showRepository.findById(idShow).orElse(null);
        if(Objects.nonNull(showResult))
            return new ResponseEntity<>(showResult.getPerformances(), HttpStatus.OK);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    private void processarCategories(Show show) {
        Set<Category> list = show.getCategories();
        show .setCategories(new HashSet<Category>());
        list.stream().forEach(cat -> {
            if(Objects.nonNull(cat) && Objects.nonNull(cat.getId())){
                Category c = categoryRepository.getById(cat.getId());
                if(Objects.nonNull(c)){
                    c.addShow(show);
                }
            }
        });
    }

    @DeleteMapping("/{idShow}/destruirActuacio")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> destruirActuacio(@PathVariable Long idShow, @RequestBody Performance performance) {
        Show show = showRepository.findById(idShow).orElse(null);
        log.trace("delete performance from: " + idShow);
        if(Objects.nonNull(show) && Objects.nonNull(show.getPerformances())){
            if(show.getPerformances().indexOf(performance) >= 0) {
                show.getPerformances().remove(performance);
                showRepository.saveAndFlush(show);
            }
            else {
                return new ResponseEntity<>("operacio invalida, actuacio inexistent en el show proporcionat", HttpStatus.OK);
            }
            return new ResponseEntity<>("Actuacio eliminada", HttpStatus.OK);
        }
        return new ResponseEntity<>("operacio invalida, show o actuacio inexistents", HttpStatus.OK);
    }

//    @GetMapping(path = "/shows/{idShow}", produces = "application/json")
//    @ResponseStatus(HttpStatus.OK)
//    public List<Performance> consultaShows(@PathVariable Long idShow) {
//        log.trace("getting category shows of category : " + idShow);
//        Show show = showRepository.getById(idShow);
//        if(show!= null)
//            return show.getPerformances();
//        return null;
//    }

    // add the code for the missing system operations here
}
