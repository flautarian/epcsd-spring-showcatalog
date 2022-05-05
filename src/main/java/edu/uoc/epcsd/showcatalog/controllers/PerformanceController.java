package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.entities.Performance;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.PathParam;
import java.util.List;
import java.util.Objects;

@Log4j2
@RestController
@RequestMapping("/performance")
public class PerformanceController {

    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private KafkaTemplate<String, Show> kafkaTemplate;

    @GetMapping("/performances/")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity getAllPerformancesFromShow(@PathParam("idShow") Long idShow) {
        Show showResult = showRepository.findById(idShow).orElse(null);
        if(Objects.nonNull(showResult))
            return new ResponseEntity<>(showResult.getPerformances(), HttpStatus.OK);
        return new ResponseEntity<>("no s'han trobat performances en el show donat", HttpStatus.OK);
    }

}
