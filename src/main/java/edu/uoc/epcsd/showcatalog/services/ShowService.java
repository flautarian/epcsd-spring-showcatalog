package edu.uoc.epcsd.showcatalog.services;

import edu.uoc.epcsd.showcatalog.entities.Performance;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.pojos.ShowData;

import java.util.List;

public interface ShowService {

    public boolean destruirShow(Long idShow);

    public Show crearShow(ShowData showData);

    public Show consultaShow(Long idShow);

    public List<Show> consultaShowPerNom(String nom);

    public List<Show> consultaShowPerCategoria(List<Long> catIds);

    public List<Show> consultaTotsShows();
    public boolean crearActuacio(Long idShow, Performance performance) throws Exception;

    public boolean destruirActuacio(Long idShow, Performance performance) throws Exception;
}
