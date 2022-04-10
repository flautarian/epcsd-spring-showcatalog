package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.entities.Performance;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/show")
public class ShowController {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private KafkaTemplate<String, Show> kafkaTemplate;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<Show> getAllShows() {
        log.trace("getAllShows");
        return showRepository.findAll();
    }

    @PostMapping("/crear")
    @ResponseStatus(HttpStatus.OK)
    public void crear(@RequestBody Show show) {
        log.trace("getAllCategories");
        showRepository.saveAndFlush(show);
    }

    @PostMapping("/destruir/{idShow}")
    @ResponseStatus(HttpStatus.OK)
    public void destruir(@PathVariable Long idShow) {
        log.trace("delete show : " + idShow);
        showRepository.deleteById(idShow);
    }

    @GetMapping(path = "/consulta/{idShow}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Show consulta(@PathVariable Long idShow) {
        log.trace("getting category : " + idShow);
        return showRepository.getById(idShow);
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
