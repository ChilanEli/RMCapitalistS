/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rmcapitalist;

import com.google.gson.Gson;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import generated.World;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;

/**
 * REST Web Service
 *
 * @author yukse
 */
@Path("")
public class GenericResource {

    @Context
    private UriInfo context;
    private Services service;

    /**
     * Creates a new instance of GenericResource
     */
    public GenericResource() {
        this.service = new Services();
    }

    /**
     * Retrieves representation of an instance of com.mycompany.rmcapitalist.GenericResource
     * @return an instance of java.lang.String
     */
    @GET
    @Path("world")
    @Produces(MediaType.APPLICATION_XML)
    public Response getXml(){
        World world;
        try
        {
                // On récupère le world courrant
            world = service.getWorld();
                // Génère un ResponseBuilder avec un "OK status" et le build
            return Response.ok(world).build();
        }
        catch(JAXBException e)
        {
                // On enregistre l'erreur dans le log ayant pour nom le nom de la classe s'il existe, sinon on le crée
                // dans lequel on associe un level soit du type : "FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE", le message est null mais on peut mettre celui de "e" et le Throwable information est "e".
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, e);
                
        }
            // Génère un ResponseBuilder avec un état not found.
        return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    @GET
    @Path("world")
    @Produces(MediaType.APPLICATION_JSON)
    // Pareil quet getXml mais retourne du Json depuis un objet complexe Java
    public Response getXmlGson(){
        World world;
        try
        {
                // On récupère le world courrant
            world = service.getWorld();
                // Génère un ResponseBuilder avec un "OK status" et le build
            return Response.ok(new Gson().toJson(world)).build();
        }
        catch(JAXBException e)
        {
                // On enregistre l'erreur dans le log ayant pour nom le nom de la classe s'il existe, sinon on le crée
                // dans lequel on associe un level soit du type : "FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE", le message est null mais on peut mettre celui de "e" et le Throwable information est "e".
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, e);
                
        }
            // Génère un ResponseBuilder avec un état not found.
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
