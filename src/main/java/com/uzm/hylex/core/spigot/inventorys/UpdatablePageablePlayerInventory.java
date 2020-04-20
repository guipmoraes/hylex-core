package com.uzm.hylex.core.spigot.inventorys;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.uzm.hylex.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class UpdatablePageablePlayerInventory implements Listener {

    private Player viewer;

    private Inventory baseInventory;

    private String title;

    private int[] occupiedSlots;

    private ItemStack occupyItem = new ItemStack(Material.AIR);

    private int currentPage;
    private int[] availableSlots;
    private int endSlot;

    private ItemStack[] baseinventoryContent;

    private Object[][] actionItems;

    private Object[] emptyItem;


    public UpdatablePageablePlayerInventory(Player viewer, Inventory base, String title) {
        this.viewer = viewer;
        this.baseInventory = base;
        this.title = title.replace("&", "§");
        Bukkit.getServer().getPluginManager().registerEvents(this, Core.getInstance());

    }

    public UpdatablePageablePlayerInventory config(ItemStack ocupeItem, int[] occupiedSlots) {
        this.occupyItem = ocupeItem;
        this.occupiedSlots = occupiedSlots;
        this.availableSlots = Ints.toArray(Ints.asList(IntStream.range(0, this.baseInventory.getSize()).toArray()).stream().filter(value -> !Ints.asList(occupiedSlots).contains(value)).collect(Collectors.toList()));

        return this;
    }

    public UpdatablePageablePlayerInventory fill(ItemStack[] content, Object[][] actionItems, Object[][] fixedContent, Object[] emptyItem, int endSlot) {
        int totalPages = getTotalPages(content);
        int inventorySize = (int) Math.ceil(baseInventory.getSize() / 9.0D) * 9;
        int currentSlot= availableSlots[0];
        this.endSlot=endSlot;


       baseinventoryContent = new ItemStack[inventorySize];

        for (int occupy : occupiedSlots) {
            baseinventoryContent[occupy] = occupyItem;
        }
        for (Object[] o : fixedContent) {
            baseinventoryContent[(Integer) o[1]] = (ItemStack) o[0];
        }
        ItemStack[] inventoryContent = baseinventoryContent.clone();

        if (content.length == 0) {
            Inventory inv = Bukkit.createInventory(null, inventoryContent.length, title.replace("{c}", (pages.size() + 1) + "").replace("{m}",  totalPages+ ""));
            inv.setContents(inventoryContent);
            inv.setItem((int)emptyItem[0],(ItemStack)emptyItem[1]);
            pages.add(inv);
            return this;
        }
        for (int in = 0; in < content.length; in++) {
            if(inventoryContent[currentSlot]  == null) {
                inventoryContent[currentSlot] = content[in];
            }
            currentSlot++;
            if (currentSlot ==  endSlot || (in + 1) == content.length) {
                Inventory inv = Bukkit.createInventory(null, inventoryContent.length,
                        title.replace("{c}", (this.pages.size() + 1) + "").replace("{m}", totalPages + ""));
                inv.setContents(inventoryContent);
                if (pages.size() > 0) {
                    inv.setItem((int)actionItems[0][0], (ItemStack) actionItems[0][1]);
                    pages.get((pages.size() - 1)).setItem((int)actionItems[1][0], (ItemStack) actionItems[1][1]);
                }
                this.pages.add(inv);
                inventoryContent= baseinventoryContent.clone();
                currentSlot=availableSlots[0];
            }
        }

        return this;
    }

    public void update(ItemStack[] newContent) {
        int currentSlot= availableSlots[0];
        int totalPages = getTotalPages(newContent);
        int inventorySize = (int) Math.ceil(baseInventory.getSize() / 9.0D) * 9;
        ItemStack[] inventoryContent = baseinventoryContent.clone();

        if (newContent.length == 0) {
            Inventory inv = Bukkit.createInventory(null, inventoryContent.length, title.replace("{c}", (pages.size() + 1) + "").replace("{m}",  totalPages+ ""));
            inv.setContents(inventoryContent);
            inv.setItem((int)emptyItem[0],(ItemStack)emptyItem[1]);
            pages.add(inv);
            return ;
        }
        for (int in = 0; in < newContent.length; in++) {
            if(inventoryContent[currentSlot]  == null) {
                inventoryContent[currentSlot] = newContent[in];
            }
            currentSlot++;
            if (currentSlot ==  endSlot || (in + 1) == newContent.length) {
                Inventory inv = Bukkit.createInventory(null, inventoryContent.length,
                        title.replace("{c}", (this.pages.size() + 1) + "").replace("{m}", totalPages + ""));
                inv.setContents(inventoryContent);
                if (pages.size() > 0) {
                    inv.setItem((int)actionItems[0][0], (ItemStack) actionItems[0][1]);
                    pages.get((pages.size() - 1)).setItem((int)actionItems[1][0], (ItemStack) actionItems[1][1]);
                }
                this.pages.add(inv);
                inventoryContent= baseinventoryContent.clone();
                currentSlot=availableSlots[0];
            }
        }
    }

    public int getTotalPages(ItemStack[] items) {
        if (items.length < availableSlots.length) {
            return 1;
        } else {
            return items.length % availableSlots.length == 0
                    ? (Math.round((float)items.length / availableSlots.length) == 0 ? 1 : Math.round((float)items.length / availableSlots.length))
                    : (Math.round((float)items.length / availableSlots.length) + 1);
        }
    }

    public UpdatablePageablePlayerInventory open(Player player, int page) {
        if (page > 0 && page <= this.pages.size()) {
            if (player != null) {
                currentPage = page;
                player.openInventory(this.pages.get(page - 1));
            }
        }
        return this;

    }

    public boolean exists(Inventory inv) {
        return ((Inventory) this.pages.get(this.currentPage - 1)).equals(inv);
    }

    public UpdatablePageablePlayerInventory destroy() {
        pages.clear();
        return this;
    }

    public int getCurrent() {
        return currentPage;
    }

    public Player getPlayer() {
        return viewer;
    }

    private List<Inventory> pages = Lists.newArrayList();
}
