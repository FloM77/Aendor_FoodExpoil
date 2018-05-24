package AT.MSev.FoodExpoil;

import AT.MSev.Mango.MangoUtils;
import AT.MSev.Mango.NBTManager;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Locale;

import static org.bukkit.Bukkit.getLogger;

public class Handler implements Listener {
    static  SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    @EventHandler
    public void OnFoodPickup(EntityPickupItemEvent e)
    {
        if(e.getItem().getItemStack().getType().isEdible())
        {
            Item pickedup = e.getItem();
            if(NBTManager.GetTag(pickedup.getItemStack(), "Expires") != null) return;

            final Calendar expDate = Calendar.getInstance();
            expDate.add(Calendar.SECOND, 5);

            ItemStack pickedupCopy = NBTManager.AddItemNBT(pickedup.getItemStack(), "Expires", new NBTTagString("" + sdf.format(expDate.getTime())));

            pickedup.remove();
            MangoUtils.ItemRelore(pickedupCopy,
                new ArrayList<String>() {{add("Expires" + sdf.format(expDate.getTime()));}});

            ((Player)e.getEntity()).getInventory().addItem(pickedupCopy);

            e.setCancelled(true);
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
}
