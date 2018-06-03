package AT.MSev.FoodExpoil;

import AT.MSev.Mango_Core.Utils.MangoUtils;
import AT.MSev.Mango_Core.Utils.NBTManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Locale;

import static org.bukkit.Bukkit.getLogger;

public class Handler implements Listener {
    static  SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH z yyyy", Locale.ENGLISH);
    @EventHandler
    public void OnFoodPickup(EntityPickupItemEvent e)
    {
        Item pickedup = e.getItem();
        if(pickedup.getItemStack().getType().isEdible())
        {
            if(NBTManager.GetTag(pickedup.getItemStack(), "Expires") != null) return;

            ItemStack pickedupCopy = ExpirableFood(pickedup.getItemStack(), Calendar.HOUR, 2);
            pickedup.remove();
            ((Player)e.getEntity()).getInventory().addItem(pickedupCopy);

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnFoodMove(InventoryClickEvent e)
    {
        ItemStack moved = e.getCurrentItem();
        if(e.getAction().equals(InventoryAction.NOTHING) || e.getAction().equals(InventoryAction.DROP_ONE_CURSOR) || e.getAction().equals(InventoryAction.DROP_ALL_CURSOR)) return;
        if(moved.getType().isEdible())
        {
            if(NBTManager.GetTag(moved, "Expires") != null) return;
            ItemStack movedCopy = ExpirableFood(moved, Calendar.HOUR, 2);
            e.setCurrentItem(movedCopy);
        }
    }

    @EventHandler
    public void OnFoodEat(PlayerItemConsumeEvent e)
    {
        String eatenExpDate = NBTManager.FromNBTString((NBTTagString) NBTManager.GetTag(e.getItem(), "Expires"));
        if(eatenExpDate != null)
        {
            Calendar expDate = Calendar.getInstance();
            try {
                if (expDate.getTime().after(sdf.parse(eatenExpDate))) {
                    e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 2));
                    e.getPlayer().sendMessage(ChatColor.RED + "You ate expired food!");
                }
            }catch (Exception ex) {getLogger().info(ex.getMessage());}
        }
    }

    ItemStack ExpirableFood(ItemStack food, int field, int amount)
    {
        final Calendar expDate = Calendar.getInstance();
        expDate.add(field, amount);

        ItemStack expFood = NBTManager.AddItemNBT(food, "Expires", new NBTTagString("" + sdf.format(expDate.getTime())));

        MangoUtils.ItemRelore(expFood,
                new ArrayList<String>() {{add("Expires " + sdf.format(expDate.getTime()));}});
        return expFood;
    }
}
