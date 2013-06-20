package battle;

import scripting.ThreadManager;
import voyagequest.GameState;
import voyagequest.Global;
import voyagequest.JsonReader;
import voyagequest.VoyageQuest;
import scripting.Thread;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Edmund
 * Date: 6/20/13
 */
public class BattleManager {

    private static HashMap<String, Battle> battles;
    public static ArrayList<Battle> loadBattleDefs;

    static
    {
        //Fill up loadBattleDefs
        JsonReader<BattleManager> reader =
                new JsonReader<>(BattleManager.class, "res/Battles.json");
        reader.readJson();

        //Load them into the HashMap
        battles = new HashMap<>();
        for (Battle battle: loadBattleDefs)
            battles.put(battle.battleID, battle);

    }



    public static void initBattle( String battleID )
    {
        Battle battle = battles.get(battleID);
        if (battle == null) return;

        String battleScriptID = battle.ScriptID;
        String rewardScriptID = battle.rewardScript;

        //First, switch to Combat State
        VoyageQuest.state = GameState.COMBAT;

        //IMPORTANT: Tell ScriptReader that now, we're using the
        //battleThreadManager. This must be changed back after combat is over
        ThreadManager battleThreadManager = VoyageQuest.battleThreadManager;
        VoyageQuest.scriptReader.setThreadHandle(battleThreadManager);

        //Now, wipe the battleThreadManager of all previous threads
        battleThreadManager.clear();

        //Find the reward
        Thread rewardThread = new Thread(rewardScriptID);
        battleThreadManager.addThread(rewardThread);
        battleThreadManager.act(0.0);

        //The reward thread will put the results in the globalMemory, so retrieve those values
        int goldReward = (int)Global.globalMemory.get("NEXT_GOLD").getDoubleValue();
        int expReward = (int)Global.globalMemory.get("NEXT_EXP").getDoubleValue();
        String[] itemReward = (String[])Global.globalMemory.get("NEXT_REWARD").getObjectArrayValue();

        //^ this stuff should be stored somewhere for after the battle is over, but I guess
        //we're more occupied with getting the battle to work first, so that's for later.

        //Finally, begin the fight by starting the script for it.
        battleThreadManager.addThread((new Thread (battleScriptID)));
    }

    public static void endBattle()
    {
        //Switch out of Combat state
        VoyageQuest.state = GameState.RPG;
        //Restore scriptReader focus on RPG threads
        VoyageQuest.scriptReader.setThreadHandle(VoyageQuest.threadManager);

    }


}
