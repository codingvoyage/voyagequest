package item;

import voyagequest.JsonReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Edmund
 * Date: 6/19/13
 */
public class ItemManager {

    public static HashMap<String, Item> items;
    public static ArrayList<Item> loadItemDefs;

    public static void init() {
        items = new HashMap<>();
        loadItemDefs = new ArrayList<>();
        JsonReader<ItemManager> reader = new JsonReader<>(ItemManager.class, "res/items/itemDefinitions.json");
        reader.readJson();

        for (Item item: loadItemDefs)
            items.put(item.itemID, item);
    }

    public static Item getItem(String key)
    {
        return items.get(key);
    }
}
