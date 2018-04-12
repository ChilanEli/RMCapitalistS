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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import generated.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;

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
     * Retrieves representation of an instance of
     * com.mycompany.rmcapitalist.GenericResource
     *
     * @param request
     * @return an instance of java.lang.String
     */
    @GET
    @Path("world")
    @Produces(MediaType.APPLICATION_XML)
    public Response getXml(@Context HttpServletRequest request) {
        String username = request.getHeader("X-user");
        World world;
        try {
            // On récupère le world courrant
            world = service.getWorld(username);
            // Génère un ResponseBuilder avec un "OK status" et le build
            return Response.ok(world).build();
        } catch (JAXBException e) {
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
    public Response getXmlGson(@Context HttpServletRequest request) {
        String username = request.getHeader("X-user");
        World world;
        try {
            System.out.println(username);
            // On récupère le world courrant
            world = service.getWorld(username);
            // Génère un ResponseBuilder avec un "OK status" et le build
            return Response.ok(new Gson().toJson(world)).build();
        } catch (JAXBException e) {
            // On enregistre l'erreur dans le log ayant pour nom le nom de la classe s'il existe, sinon on le crée
            // dans lequel on associe un level soit du type : "FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE", le message est null mais on peut mettre celui de "e" et le Throwable information est "e".
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, e);

        }
        // Génère un ResponseBuilder avec un état not found.
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("product")
    @Consumes(MediaType.APPLICATION_JSON)
    public void putProduct(String data, @Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        ProductType product = new Gson().fromJson(data, ProductType.class);
        service.updateProduct(username, product);
    }

    @PUT
    @Path("manager")
    @Consumes(MediaType.APPLICATION_JSON)
    public void putManager(String data, @Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        PallierType manager = new Gson().fromJson(data, PallierType.class);
        service.updateManager(username, manager);
    }

    @PUT
    @Path("upgrade")
    @Consumes(MediaType.APPLICATION_JSON)
    public void putUpgrade(String data, @Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        PallierType upgrade = new Gson().fromJson(data, PallierType.class);
        service.updateUpgrade(username, upgrade);
    }

    @PUT
    @Path("angelupgrade")
    @Consumes(MediaType.APPLICATION_JSON)
    public void putAngelUpgrade(String data, @Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        PallierType angelupgrade = new Gson().fromJson(data, PallierType.class);
        service.updateAngelUpgrade(username, angelupgrade);
    }    

    @DELETE
    @Path("world")
    @Consumes(MediaType.APPLICATION_JSON)
    public void removeWorld(@Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        service.resetWorld(username);
        System.out.println("DELETE WORLD");
    }
}
