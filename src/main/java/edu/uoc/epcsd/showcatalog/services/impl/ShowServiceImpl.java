package edu.uoc.epcsd.showcatalog.services.impl;

import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.entities.Performance;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.pojos.ShowData;
import edu.uoc.epcsd.showcatalog.repositories.CategoryRepository;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import edu.uoc.epcsd.showcatalog.services.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ShowServiceImpl implements ShowService {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public boolean destruirShow(Long idShow) {
        try{
            showRepository.deleteById(idShow);
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Show crearShow(ShowData showData) {
        try{
            Show show = new Show(showData);
            processarCategories(showData, show);
            return showRepository.saveAndFlush(show);
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public Show consultaShow(Long idShow) {
        return showRepository.findById(idShow).orElse(null);
    }

    @Override
    public List<Show> consultaShowPerNom(String nom) {
        return showRepository.findByName(nom);
    }

    @Override
    public List<Show> consultaShowPerCategoria(List<Long> catIds) {
        return showRepository.findByCategoriesIdIn(catIds);
    }

    @Override
    public List<Show> consultaTotsShows() {
        return showRepository.findAll();
    }

    @Override
    public boolean crearActuacio(Long idShow, Performance performance) throws Exception {
        try{
            Show show = consultaShow(idShow);
            if(Objects.nonNull(show)){
                if(Objects.isNull(show.getPerformances()))show.setPerformances(new ArrayList<Performance>());
                if(show.getPerformances().indexOf(performance) < 0){
                    show.getPerformances().add(performance);
                    showRepository.saveAndFlush(show);
                    return true;
                }
                else throw new Exception("operacio invalida, actuacio existent");
            }
            else throw new Exception("operacio invalida, show inexistent");
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }

    }

    @Override
    public boolean destruirActuacio(Long idShow, Performance performance) throws Exception {
        try{
            Show show = consultaShow(idShow);
            if(Objects.nonNull(show)){
                if(Objects.isNull(show.getPerformances()))
                    throw new Exception("operacio invalida, el show no conte actuacions");
                if(show.getPerformances().indexOf(performance) >= 0){
                    show.getPerformances().remove(performance);
                    showRepository.saveAndFlush(show);
                    return true;
                }
                else throw new Exception("operacio invalida, actuacio inexistent");
            }
            else throw new Exception("operacio invalida, show inexistent");
        }
        catch (Exception e){
            e.printStackTrace();
            throw e;
        }
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
}
