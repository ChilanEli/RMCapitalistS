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
        calcNewScore(world);
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
                    switch (p.getTyperatio()) {
                        case GAIN:
                            product.setRevenu(product.getRevenu() * p.getRatio());
                            break;
                        default:
                            product.setVitesse((int) (product.getVitesse() / p.getRatio()));
                            product.setTimeleft((long) (product.getTimeleft() / p.getRatio()));
                            break;
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
                        unlock.setUnlocked(true);;
                        switch (unlock.getTyperatio()) {
                            case GAIN:
                                for (ProductType pr : world.getProducts().getProduct()) {
                                    pr.setRevenu(pr.getRevenu() * unlock.getRatio());
                                }
                                break;
                            default:
                                for (ProductType pr : world.getProducts().getProduct()) {
                                    pr.setVitesse((int) (pr.getVitesse() / unlock.getRatio()));
                                    pr.setTimeleft((long) (pr.getTimeleft() / unlock.getRatio()));
                                }
                                break;
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
        manager.setUnlocked(true);
        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // débloquer le manager de ce produit
        product.setManagerUnlocked(true);
        // soustraire de l'argent du joueur le cout du manager
        world.setMoney(world.getMoney() - manager.getSeuil());
        // sauvegarder les changements au monde
        world.setLastupdate(System.currentTimeMillis());
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
        long tempDiff = System.currentTimeMillis() - w.getLastupdate();
        for (ProductType p : w.getProducts().getProduct()) {
            if ((p.isManagerUnlocked()) && (p.getQuantite() > 0)) {
                long t = (tempDiff - p.getVitesse() + p.getTimeleft()) / p.getVitesse();
                long tempRest = (tempDiff - p.getVitesse() + p.getTimeleft()) % p.getVitesse();

                w.setMoney(w.getMoney() + (p.getRevenu() * (1 + w.getActiveangels() * w.getAngelbonus())) * t);
                w.setScore(w.getMoney() + (p.getRevenu() * (1 + w.getActiveangels() * w.getAngelbonus())) * t);
                //ça marche 1 fois sur 3
                //p.setTimeleft(tempRest);
                p.setTimeleft(0);
                if (p.getTimeleft() < 0 || p.getTimeleft() >= p.getVitesse()) {
                    p.setTimeleft(0);
                }
            } else {
                if (p.getTimeleft() >= 0 && p.getTimeleft() <= tempDiff) {
                    p.setTimeleft(0);
                } else if (p.getTimeleft() > 0) {
                    //p.setTimeleft(p.getTimeleft() - tempDiff);
                    p.setTimeleft(0);
                }
            }
        }
    }

    public Boolean updateUpgrade(String username, PallierType upgrade) throws JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);

        // trouver dans ce monde, le pallier équivalent à celui passé 
        // en paramètre 
        PallierType u = getUpgrade(world, upgrade.getName());
        if (u == null) {
            return false;
        }
        upgrade.setUnlocked(!upgrade.isUnlocked());
        ProductType p = findProductById(world, u.getIdcible());
        switch (u.getTyperatio()) {
            case GAIN:
                p.setRevenu(p.getRevenu() * upgrade.getRatio());
                break;
            default:
                p.setVitesse((int) (p.getVitesse() / upgrade.getRatio()));
                p.setTimeleft((long) (p.getTimeleft() / upgrade.getRatio()));
                break;
        }
        world.setMoney(world.getMoney() - upgrade.getSeuil());
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(username, world);
        return true;
    }

    public Boolean updateAngelUpgrade(String username, PallierType angelupgrade) throws JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);

        // trouver dans ce monde, le pallier équivalent à celui passé 
        // en paramètre 
        PallierType a = getAngel(world, angelupgrade.getName());
        if (a == null) {
            return false;
        }
        a.setUnlocked(!a.isUnlocked());
        switch (a.getTyperatio()) {
            case GAIN:
                for (ProductType p : world.getProducts().getProduct()) {
                    p.setRevenu(p.getRevenu() * a.getRatio());
                }
                break;
            case VITESSE:
                for (ProductType p : world.getProducts().getProduct()) {
                    p.setVitesse((int) (p.getVitesse() / a.getRatio()));
                    p.setTimeleft((long) (p.getTimeleft() / a.getRatio()));
                }
                break;
            default:
                world.setAngelbonus((int) (world.getAngelbonus() + a.getRatio()));
                break;
        }
        world.setActiveangels(world.getActiveangels() - angelupgrade.getSeuil());
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(username, world);
        return true;
    }

    private PallierType getUpgrade(World world, String name) {
        try {
            for (PallierType u : world.getUpgrades().getPallier()) {
                if (u.getName().equals(name)) {
                    return u;
                }
            }
            return null;
        } catch (Exception e) {
            Logger.getLogger(GenericResource.class
                    .getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    private PallierType getAngel(World world, String name) {
        try {
            for (PallierType angelupgrade : world.getAngelupgrades().getPallier()) {
                if (angelupgrade.getName().equals(name)) {
                    return angelupgrade;
                }
            }
            return null;
        } catch (Exception e) {
            Logger.getLogger(GenericResource.class
                    .getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    public void resetWorld(String username) throws JAXBException {
        World w1 = getWorld(username);
        double cptAnge = Math.round(150 * Math.sqrt(w1.getScore() / Math.pow(10, 15)) - w1.getTotalangels());

        w1.setActiveangels(w1.getActiveangels() + cptAnge);
        w1.setTotalangels(w1.getTotalangels() + cptAnge);

        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
        World w2 = (World) u.unmarshal(input);
        
        w2.setScore(w1.getScore());
        w2.setTotalangels(w1.getTotalangels());
        w2.setActiveangels(w1.getActiveangels());

        saveWorldToXml(username, w2);
    }
}
