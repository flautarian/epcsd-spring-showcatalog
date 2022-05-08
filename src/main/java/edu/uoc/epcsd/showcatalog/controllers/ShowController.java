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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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

    @GetMapping(path = "/consulta", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity consulta(@RequestParam(required = false) Long idShow, @RequestParam(required = false) String showName, @RequestParam(required = false) List<Long> idCategories) throws ObjectNotFoundException {
        log.trace("Executant endpoint: 'Consulta de shows per nom' | 'Consulta de shows per categoria' | 'Consulta de detall d'un show'");
        List<Show> showResults = Objects.nonNull(idShow) ? Arrays.asList(showRepository.findById(idShow).orElse(null)).stream().filter(s -> (s != null)).collect(Collectors.toList()) :
                    Objects.nonNull(showName) ? showRepository.findFirstByName(showName) :
                            Objects.nonNull(idCategories) ? showRepository.findByCategoriesIdIn(idCategories) :
                                    showRepository.findAll();
        return new ResponseEntity<>(Objects.nonNull(showResults) && !showResults.isEmpty() ? showResults : "Show no trobat", HttpStatus.OK);
    }

    @PostMapping("/crear")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity crearShow(@RequestBody ShowData show) {
        log.trace("Executant endpoint: 'Crear acte'");
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
        log.trace("Executant endpoint: 'Eliminar acte'");
        try{
            showRepository.deleteById(idShow);
            return new ResponseEntity<>("Show eliminat", HttpStatus.OK);
        }catch(EmptyResultDataAccessException e){
            return new ResponseEntity<>("Error: Show no existent", HttpStatus.OK);
        }
    }

    @PostMapping("/{idShow}/crearActuacio")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity crearActuacio(@PathVariable Long idShow ,@RequestBody Performance performance) {
        log.trace("Executant endpoint: 'Crear actuació'");
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
        log.trace("Executant endpoint: 'Consulta d'actuacions d'un show'");
        Show showResult = showRepository.findById(idShow).orElse(null);
        if(Objects.nonNull(showResult))
            return new ResponseEntity<>(showResult.getPerformances(), HttpStatus.OK);
        return new ResponseEntity<>("no s'han trobat actuacions", HttpStatus.OK);
    }

    private void processarCategories(ShowData show, Show newShow) {
        if(Objects.nonNull(show.getIdCategories())){
            show.getIdCategories().stream().forEach(catId -> {
                if(Objects.nonNull(catId) && Objects.nonNull(catId)){
                    Category c = categoryRepository.findById(catId).orElse(null);
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
        log.trace("Executant endpoint: 'Eliminar actuació'");
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
        return new ResponseEntity<>("operacio invalida, show inexistent", HttpStatus.OK);
    }
}
