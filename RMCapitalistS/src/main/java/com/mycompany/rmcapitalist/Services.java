/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.rmcapitalist;

import generated.PallierType;
import generated.ProductType;
import generated.TyperatioType;
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
//            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
//            world = (World) u.unmarshal(input);
            world = (World) u.unmarshal(new File(username + "-world.xml"));
        } catch (JAXBException e) {
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            world = (World) u.unmarshal(input);
        }
        return world;
    }

    void saveWorldToXml(String username, World world) throws JAXBException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        m.marshal(world, new File(username + "-world.xml"));
    }

    public World getWorld(String username) throws JAXBException {
        World world = readWorldFromXml(username);
        //calcNewScore(world);
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
            double gainUnit;
            double croissance;
            double depense;
            try {
                gainUnit = product.getRevenu() / product.getQuantite();
            } catch (Exception e) {
                gainUnit = product.getRevenu();
            }
            croissance = product.getCroissance();
            depense = product.getCout() * Math.pow(croissance, product.getQuantite()) * ((1 - Math.pow(croissance, qtchange)) / (1 - croissance));
            world.setMoney(world.getMoney() - depense);
            product.setQuantite(product.getQuantite() + qtchange);
            if (product.getQuantite() > 1) {
                product.setRevenu(product.getRevenu() + (gainUnit * qtchange));
            }
            for (PallierType p : product.getPalliers().getPallier()) {
                if (!p.isUnlocked() && product.getQuantite() >= p.getSeuil()) {
                    p.setUnlocked(!p.isUnlocked());
                    TyperatioType type = p.getTyperatio();
                    if (p.getTyperatio() == type.fromValue("GAIN")) {
                        product.setRevenu(product.getRevenu() * p.getRatio());
                    } else if (p.getTyperatio() == type.fromValue("VITESSE")){

                        product.setVitesse((int) (product.getVitesse() / p.getRatio()));
                        product.setTimeleft((long) (product.getTimeleft() / p.getRatio()));
                    }
                }
            }
            for (PallierType unlock : world.getAllunlocks().getPallier()) {
                if (!unlock.isUnlocked() && product.getQuantite() >= unlock.getSeuil()) {
                    boolean isReached = true;
                    for (ProductType p : world.getProducts().getProduct()) {
                        if (p.getQuantite() < unlock.getSeuil()) {
                            isReached = false;
                            break;
                        }
                    }
                    if (isReached) {
                        unlock.setUnlocked(true);
                        TyperatioType type = unlock.getTyperatio();
                        if (unlock.getTyperatio() == type.fromValue("GAIN")) {
                            for (ProductType pr : world.getProducts().getProduct()) {
                                pr.setRevenu(pr.getRevenu() * unlock.getRatio());
                            }
                        } else if (unlock.getTyperatio() == type.fromValue("VITESSE")) {
                            for (ProductType pr : world.getProducts().getProduct()) {
                                pr.setVitesse((int) (pr.getVitesse() / unlock.getRatio()));
                                pr.setTimeleft((long) (pr.getTimeleft() / unlock.getRatio()));
                            }
                        }
                    }
                }
            }
        } else {
            // initialiser product.timeleft à product.vitesse
            // pour lancer la production
            product.setTimeleft(product.getVitesse());

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
            Logger.getLogger(GenericResource.class
                    .getName()).log(Level.SEVERE, null, e);
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
            Logger.getLogger(GenericResource.class
                    .getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    public void calcNewScore(World w) {
        long temp = System.currentTimeMillis();
        for (ProductType p : w.getProducts().getProduct()) {
            if ((p.isManagerUnlocked()) && (p.getQuantite() > 0)) {
                long t = Math.floorDiv(temp - w.getLastupdate() + p.getVitesse() - p.getTimeleft(), p.getVitesse());
                long tempRest = Math.floorDiv(temp - w.getLastupdate() + p.getVitesse() - p.getTimeleft(), p.getVitesse());

                w.setMoney(w.getMoney() + (p.getRevenu() * (1 + w.getActiveangels() * w.getAngelbonus())) * t);
                w.setScore(w.getMoney() + (p.getRevenu() * (1 + w.getActiveangels() * w.getAngelbonus())) * t);

                p.setTimeleft(tempRest);
                if (p.getTimeleft() < 0) {
                    p.setTimeleft(0);
                }
            } else {
                if (p.getTimeleft() > 0 && p.getTimeleft() <= temp - w.getLastupdate()) {
                    w.setMoney(w.getMoney() + (p.getRevenu() * (1 + w.getActiveangels() * w.getAngelbonus())));
                    w.setScore(w.getMoney() + (p.getRevenu() * (1 + w.getActiveangels() * w.getAngelbonus())));
                    p.setTimeleft(0);
                } else if (p.getTimeleft() > 0) {
                    p.setTimeleft(p.getTimeleft() - (temp - w.getLastupdate()));
                }
            }
        }
    }
}
