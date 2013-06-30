package battle;

import scripting.Scriptable;
import voyagequest.JsonReader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Edmund
 * Date: 6/27/13
 */
public class WeaponManager {
    public static HashMap<String, Weapon> weaponLookup;
    public static ArrayList<Weapon> loadWeapons;

    static
    {
        //Fill up loadBattleDefs
        JsonReader<BattleManager> reader =
                new JsonReader<>(BattleManager.class, "res/Weapons.json");
        reader.readJson();

        //Load them into the HashMap
        weaponLookup = new HashMap<>();
        for (Weapon weapon: loadWeapons)
            weaponLookup.put(weapon.weaponID, weapon);
    }


    public String fireWeapon(String weaponID, Scriptable scriptable)
    {



        return "";
    }

}
