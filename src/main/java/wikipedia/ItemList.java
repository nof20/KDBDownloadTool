package wikipedia;

import java.util.ArrayList;

public class ItemList {

	ArrayList<Item> items;
	
	ItemList() {
		this.items = new ArrayList<Item>();
	}
	
	ItemList(ArrayList<Item> items) {
		this.items = items;
	}
	
}
