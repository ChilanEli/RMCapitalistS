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
            world = (World) u.unmarshal(new File(username + "_world.xml"));
        } catch (JAXBException e) {
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            world = (World) u.unmarshal(input);
        }
        return world;
    }

    void saveWorldToXml(World world, String username) throws JAXBException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        m.marshal(world, new File(username + "_world.xml"));
    }

    public World getWorld(String username) throws JAXBException {
        World world = readWorldFromXml(username);
        world.setLastupdate(System.currentTimeMillis());
        saveWorldToXml(world, username);
        return world;
    }

    void updateProduct(ProductType product, String username) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void updateManager(PallierType manager, String username) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
