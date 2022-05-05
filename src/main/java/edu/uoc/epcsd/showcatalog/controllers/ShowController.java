package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.entities.Performance;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.pojos.ShowData;
import edu.uoc.epcsd.showcatalog.kafka.KafkaConstants;
import edu.uoc.epcsd.showcatalog.repositories.CategoryRepository;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import javassist.tools.rmi.ObjectNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity consulta(@RequestParam(required = false) Long idShow, @RequestParam(required = false) String showName, @RequestParam(required = false) List<Long> idCategories) throws ObjectNotFoundException {
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
    public ResponseEntity crear(@RequestBody ShowData show) {
        Show newShow = new Show(show);
        processarCategories(show, newShow);
        Show newShowResult = showRepository.saveAndFlush(newShow);
        if(Objects.nonNull(newShowResult)){
            // Emitim el missatge per el stream de kafka
            kafkaTemplate.send(KafkaConstants.SHOW_TOPIC + KafkaConstants.SEPARATOR + KafkaConstants.COMMAND_ADD, newShowResult);
            return new ResponseEntity<>(newShowResult, HttpStatus.CREATED);
        }
        else return new ResponseEntity<>("operacio invalida, reviseu parametres", HttpStatus.OK);
    }

    @DeleteMapping("/destruir/{idShow}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity destruir(@PathVariable Long idShow) {
        log.trace("delete show : " + idShow);
        showRepository.deleteById(idShow);
        return new ResponseEntity<>("Show eliminat", HttpStatus.CREATED);
    }

    @PostMapping("/{idShow}/crearActuacio")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity crear(@PathVariable Long idShow ,@RequestBody Performance performance) {
        if(Objects.nonNull(idShow) && idShow != 0L){
            Show show = showRepository.findById(idShow).orElse(null);
            if(Objects.nonNull(show)){
                if(Objects.isNull(show.getPerformances()))show.setPerformances(new ArrayList<Performance>());
                if(show.getPerformances().indexOf(performance) < 0){
                    show.getPerformances().add(performance);
                    return new ResponseEntity<>(showRepository.saveAndFlush(show), HttpStatus.OK);
                }
                else
                    return new ResponseEntity<>("operacio invalida, actuacio existent", HttpStatus.OK);

            }
        }
        return new ResponseEntity<>("operacio invalida, reviseu parametres", HttpStatus.OK);
    }

    @GetMapping("/{idShow}/llistarActuacions")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity llistarActuacions(@PathVariable("idShow") Long idShow) {
        Show showResult = showRepository.findById(idShow).orElse(null);
        if(Objects.nonNull(showResult))
            return new ResponseEntity<>(showResult.getPerformances(), HttpStatus.OK);
        return new ResponseEntity<>("no s'han trobat actuacions", HttpStatus.OK);
    }

    private void processarCategories(ShowData show, Show newShow) {
        if(Objects.nonNull(show.getIdCategories())){
            show.getIdCategories().stream().forEach(catId -> {
                if(Objects.nonNull(catId) && Objects.nonNull(catId)){
                    Category c = categoryRepository.getById(catId);
                    if(Objects.nonNull(c)){
                        c.addShow(newShow);
                    }
                }
            });
        }
    }

    @DeleteMapping("/{idShow}/destruirActuacio")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity destruirActuacio(@PathVariable Long idShow, @RequestBody Performance performance) {
        Show show = showRepository.findById(idShow).orElse(null);
        log.trace("delete performance from: " + idShow);
        if(Objects.nonNull(show) && Objects.nonNull(show.getPerformances())){
            if(show.getPerformances().indexOf(performance) >= 0) {
                show.getPerformances().remove(performance);
                showRepository.saveAndFlush(show);
            }
            else
                return new ResponseEntity<>("operacio invalida, actuacio inexistent en el show proporcionat", HttpStatus.OK);
        }
        return new ResponseEntity<>("operacio invalida, show o actuacio inexistents", HttpStatus.OK);
    }
}
