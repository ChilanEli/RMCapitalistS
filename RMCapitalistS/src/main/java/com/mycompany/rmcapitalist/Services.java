/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rmcapitalist;

import generated.PallierType;
import generated.ProductType;
import generated.World;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author yukse
 */
public class Services {

    private World readWorldFromXml(String username) throws JAXBException {
        World world;
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        try {
            // lors du dev au début, il y a déjà un fichier world.xml côté client, il faut donc le supprimer et le créer via ces deux lignes
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            world = (World) u.unmarshal(input);
//            world = (World) u.unmarshal(new File(username + "_world.xml"));
        } catch (JAXBException e) {
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            world = (World) u.unmarshal(input);
        }
        return world;
    }

    void saveWorldToXml(String username, World world) throws JAXBException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        m.marshal(world, new File(username + "_world.xml"));
    }

    public World getWorld(String username) throws JAXBException {
        World world = readWorldFromXml(username);
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(username, world);
        return world;
    }

    // prend en paramètre le pseudo du joueur et le produit 
    // sur lequel une action a eu lieu (lancement manuel de production ou
    // achat d’une certaine quantité de produit)
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateProduct(String username, ProductType newproduct) throws JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);

        // trouver dans ce monde, le produit équivalent à celui passé 
        // en paramètre 
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) {
            return false;
        }

        // calculer la variation de quantité. Si elle est positive c'est
        // que le joueur a acheté une certaine quantité de ce produit
        // sinon c’est qu’il s’agit d’un lancement de production.
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            // soustraire de l'argent du joueur le cout de la quantité 
            // achetée et mettre à jour la quantité de product 

        } else {
            // initialiser product.timeleft à product.vitesse
            // pour lancer la production

        }
        // sauvegarder les changements du monde
        saveWorldToXml(username, world);
        return true;
    }

    // prend en paramètre le pseudo du joueur et le manager acheté.
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateManager(String username, PallierType newmanager) throws JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé
        // en paramètre 
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            return false;
        }
        // débloquer ce manager 

        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // débloquer le manager de ce produit

        // soustraire de l'argent du joueur le cout du manager
        // sauvegarder les changements au monde
        saveWorldToXml(username, world);
        return true;
    }

    private ProductType findProductById(World world, int id) {
        try {
            return world.getProducts().getProduct().get(id - 1);
        } catch (Exception e) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    private PallierType findManagerByName(World world, String name) {
        try {
            for (PallierType m : world.getManagers().getPallier()) {
                if (m.getName().equals(name)) {
                    return m;
                }
            }
            return null;
        } catch (Exception e) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
}
