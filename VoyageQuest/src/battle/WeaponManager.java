package battle;

import scripting.Scriptable;
import scripting.Thread;
import voyagequest.JsonReader;
import voyagequest.VoyageQuest;

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
        JsonReader<WeaponManager> reader =
                new JsonReader<>(WeaponManager.class, "res/Weapons.json");
        reader.readJson();

        //Load them into the HashMap
        weaponLookup = new HashMap<>();
        for (Weapon weapon: loadWeapons)
            weaponLookup.put(weapon.weaponID, weapon);
    }


    public static void init() {}

    /**
     * Launches the script of the Weapon indexed by weaponID
     * @param weaponID the String ID of the Weapon which is to be fired
     * @param scriptable the thing firing the weapon
     * @return
     */
    public static String fireWeapon(String weaponID, Scriptable scriptable)
    {
        System.out.println("FIRING WEAPON");
        Weapon selectedWeapon = weaponLookup.get(weaponID);
        String weaponThreadName = weaponID + BattleField.getInstanceNumber();

        Thread weaponThread = new Thread(selectedWeapon.weaponScript);
            weaponThread.setName(weaponThreadName);
            weaponThread.setLineNumber(0);
            weaponThread.setScriptable(scriptable);
            weaponThread.setRunningState(false);

        VoyageQuest.battleThreadManager.addThread(weaponThread);



        return "";
    }

}
