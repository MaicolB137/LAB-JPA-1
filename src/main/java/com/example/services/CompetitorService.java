/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.services;

import com.example.PersistenceManager;
import com.example.models.Competitor;
import com.example.models.CompetitorDTO;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.NotAuthorizedException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.List;
import javax.persistence.NoResultException;

/**
 *
 * @author Mauricio
 */
@Path("/competitors")
@Produces(MediaType.APPLICATION_JSON)
public class CompetitorService {

    @PersistenceContext(unitName = "CompetitorsPU")
    EntityManager entityManager;

    @PostConstruct
    public void init() {
        entityManager = (EntityManager) PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {

        /*List<Competitor> competitors = new ArrayList<Competitor>();
        Competitor competitorTmp= new Competitor("Carlos", "Alvarez", 35, "7658463", "3206574839 ", "carlos.alvarez@gmail.com", "Bogota", "Colombia", false);
        Competitor competitorTmp2= new Competitor("Gustavo", "Ruiz", 55, "2435231", "3101325467", "gustavo.ruiz@gmail.com", "Buenos Aires", "Argentina", false);
        competitors.add(competitorTmp);
        competitors.add(competitorTmp2);*/
        Query q = entityManager.createQuery("select u from Competitor u order by u.surname ASC");
        List<Competitor> competitors = q.getResultList();

        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(competitors).build();
    }

    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCompetitor(CompetitorDTO competitor) {
        JSONObject rta = new JSONObject();
        Competitor competitorTmp = new Competitor(competitor.getName(), competitor.getSurname(), competitor.getAge(), competitor.getTelephone(), competitor.getCellphone(), competitor.getAddress(), competitor.getCity(), competitor.getCountry(), false);
        competitorTmp.setEmail(competitor.getEmail());
        competitorTmp.setPassword(competitor.getPassword());
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(competitorTmp);
            entityManager.getTransaction().commit();
            entityManager.refresh(competitorTmp);
            rta.put("Competitor id", competitorTmp.getId());
        } catch (Throwable t) {
            t.printStackTrace();
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            competitorTmp = null;
        } finally {
            entityManager.clear();
            entityManager.close();
        }
        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(competitorTmp).build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(CompetitorDTO competitorDTO) {
        Query q = entityManager.createQuery(
                "SELECT c FROM Competitor c WHERE c.email = :email"
        );
        q.setParameter("email", competitorDTO.getEmail());

        try {
            Competitor competitor = (Competitor) q.getSingleResult();

            if (competitor != null && competitor.getPassword().equals(competitorDTO.getPassword())) {
                return Response.status(Response.Status.OK)
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(competitor)
                        .build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Invalid credentials")
                        .build();
            }
        } catch (NoResultException e) {
            // Maneja el caso donde no se encuentra el competidor
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid credentials")
                    .build();
        } catch (Exception e) {
            // Manejo de excepciones generales
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred: " + e.getMessage())
                    .build();
        }
    }
}
