package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.entities.Performance;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.pojos.ShowData;
import edu.uoc.epcsd.showcatalog.kafka.KafkaConstants;
import edu.uoc.epcsd.showcatalog.services.CategoryService;
import edu.uoc.epcsd.showcatalog.services.ShowService;
import javassist.tools.rmi.ObjectNotFoundException;
import lombok.extern.log4j.Log4j2;
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
    private ShowService showService;

    @Autowired
    private KafkaTemplate<String, Show> kafkaTemplate;

    @GetMapping(path = "/", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity consulta(@RequestParam(required = false) Long idShow) throws ObjectNotFoundException {
        log.trace("Executant endpoint: 'Consulta de detall d'un show'");
        Show showResult = showService.consultaShow(idShow);
        return new ResponseEntity<>(Objects.nonNull(showResult) ? showResult : "Show no trobat",
                    Objects.nonNull(showResult) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @GetMapping(path = "/consulta", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity consulta(@RequestParam(required = false) Long idShow, @RequestParam(required = false) String showName, @RequestParam(required = false) List<Long> idCategories) throws ObjectNotFoundException {
        log.trace("Executant endpoint: 'Consulta de shows per nom' | 'Consulta de shows per categoria' | 'Consulta de detall d'un show'");
        List<Show> showResults = Objects.nonNull(idShow) ? Arrays.asList(showService.consultaShow(idShow)).stream().filter(s -> (s != null)).collect(Collectors.toList()) :
                    Objects.nonNull(showName) ? showService.consultaShowPerNom(showName) :
                            Objects.nonNull(idCategories) ? showService.consultaShowPerCategoria(idCategories) :
                                    showService.consultaTotsShows();
        return new ResponseEntity<>(Objects.nonNull(showResults) && !showResults.isEmpty() ? showResults : "Show no trobat", Objects.nonNull(showResults) && !showResults.isEmpty() ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @PostMapping("/crear")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity crearShow(@RequestBody ShowData showData) {
        log.trace("Executant endpoint: 'Crear acte'");
        Show newShow = showService.crearShow(showData);
        if(Objects.nonNull(newShow)){
            // Emitim el missatge per el stream de kafka
            kafkaTemplate.send(KafkaConstants.SHOW_TOPIC + KafkaConstants.SEPARATOR + KafkaConstants.COMMAND_ADD, newShow);
            return new ResponseEntity<>(newShow, HttpStatus.CREATED);
        }
        else return new ResponseEntity<>("operacio invalida, reviseu parametres", HttpStatus.OK);
    }

    @DeleteMapping("/destruir/{idShow}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity destruir(@PathVariable Long idShow) {
        log.trace("Executant endpoint: 'Eliminar acte'");
        try{
            if(showService.destruirShow(idShow))
                return new ResponseEntity<>("Show eliminat", HttpStatus.OK);
            else
                return new ResponseEntity<>("Error: Show no existent", HttpStatus.OK);
        }catch(EmptyResultDataAccessException e){
            return new ResponseEntity<>("Error: Show no existent", HttpStatus.OK);
        }
    }

    @PostMapping("/{idShow}/crearActuacio")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity crearActuacio(@PathVariable Long idShow ,@RequestBody Performance performance) {
        log.trace("Executant endpoint: 'Crear actuació'");
        if(Objects.nonNull(idShow) && idShow != 0L){
            try {
                showService.crearActuacio(idShow, performance);
                return new ResponseEntity<>("actuacio creada correctament", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("error: " + e.getMessage(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("operacio invalida, reviseu parametres", HttpStatus.OK);
    }

    @GetMapping("/{idShow}/llistarActuacions")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity llistarActuacions(@PathVariable("idShow") Long idShow) {
        log.trace("Executant endpoint: 'Consulta d'actuacions d'un show'");
        Show showResult = showService.consultaShow(idShow);
        if(Objects.nonNull(showResult))
            return new ResponseEntity<>(showResult.getPerformances(), HttpStatus.OK);
        return new ResponseEntity<>("no s'han trobat actuacions", HttpStatus.OK);
    }

    @DeleteMapping("/{idShow}/destruirActuacio")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity destruirActuacio(@PathVariable Long idShow, @RequestBody Performance performance) {
        log.trace("Executant endpoint: 'Eliminar actuació'");
        if(Objects.nonNull(idShow) && idShow != 0L && Objects.nonNull(performance)){
            try {
                showService.destruirActuacio(idShow, performance);
                return new ResponseEntity<>("actuacio eliminada correctament", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("error: " + e.getMessage(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("operacio invalida, reviseu parametres", HttpStatus.OK);
    }
}
