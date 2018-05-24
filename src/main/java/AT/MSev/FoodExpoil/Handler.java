package AT.MSev.FoodExpoil;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagString;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
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

    @EventHandler
    public void OnFoodPickup(EntityPickupItemEvent e)
    {
        if(e.getItem().getItemStack().getType().isEdible())
        {
            net.minecraft.server.v1_12_R1.ItemStack nbt = CraftItemStack.asNMSCopy(e.getItem().getItemStack());
            if(nbt.hasTag() && nbt.getTag().hasKey("Expires")) return;

            final Calendar expDate = Calendar.getInstance();
            expDate.add(Calendar.SECOND, 5);
            final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            NBTTagCompound newNBT = new NBTTagCompound();
            newNBT.set("Expires", new NBTTagString("" + sdf.format(expDate.getTime())));
            nbt.setTag(newNBT);
            e.getItem().remove();
            ItemStack pickup = CraftItemStack.asBukkitCopy(nbt);
            ItemMeta im = pickup.getItemMeta();
            im.setLore(new ArrayList<String>() {{add("Expires" + sdf.format(expDate.getTime()));}});
            pickup.setItemMeta(im);
            ((Player)e.getEntity()).getInventory().addItem(pickup);
            getLogger().info("" +sdf.format(expDate.getTime()));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void OnFoodEat(PlayerItemConsumeEvent e)
    {
        net.minecraft.server.v1_12_R1.ItemStack nbt = CraftItemStack.asNMSCopy(e.getItem());
        if(nbt.hasTag() && nbt.getTag().hasKey("Expires"))
        {
            Calendar expDate = Calendar.getInstance();
            String expDateString = nbt.getTag().getString("Expires");
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            try {
                if (expDate.getTime().after(sdf.parse(expDateString))) {
                    e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 4, 2));
                }
            }catch (Exception ex) {getLogger().info(ex.getMessage());}
        }
    }
}
