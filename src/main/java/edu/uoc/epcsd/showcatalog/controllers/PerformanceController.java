package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.entities.Performance;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{idShow}/")
    @ResponseStatus(HttpStatus.OK)
    public List<Performance> getAllPerformancesFromShow(@PathVariable Long idShow) {
        Show showResult = showRepository.findById(idShow).orElse(null);
        if(Objects.nonNull(showResult))
            return showResult.getPerformances();
        return null;
    }

}
