package battle;

import voyagequest.JsonReader;

import java.util.LinkedList;

/**
 * battle
 *
 * @author Brian
 * @version 06 2013
 */
public class EntityManager {

    //public LinkedList<Entity> entities;

    private static LinkedList<BattleEntity> entityData;

    static {

        JsonReader<EntityManager> reader =
                new JsonReader<>(EntityManager.class, "res/BattleEntities.json");
        reader.readJson();

    }

}
