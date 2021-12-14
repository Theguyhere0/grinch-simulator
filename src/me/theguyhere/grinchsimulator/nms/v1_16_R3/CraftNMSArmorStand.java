package me.theguyhere.grinchsimulator.nms.v1_16_R3;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Collection;

public class CraftNMSArmorStand extends CraftArmorStand {

    public CraftNMSArmorStand(CraftServer server, EntityNMSArmorStand entity) {
        super(server, entity);
        super.setInvisible(true);
        super.setInvulnerable(true);
        super.setArms(false);
        super.setSmall(true);
        super.setBasePlate(true);
        super.setCollidable(false);
    }

    // Disallow all the bukkit methods.

    @Override
    public void remove() {
        // Cannot be removed, this is the most important to override.
    }

    // Methods from ArmorStand class
    @Override public void setArms(boolean arms) { }
    @Override public void setBasePlate(boolean basePlate) { }
    @Override public void setBodyPose(EulerAngle pose) { }
    @Override public void setBoots(ItemStack item) { }
    @Override public void setChestplate(ItemStack item) { }
    @Override public void setHeadPose(EulerAngle pose) { }
    @Override public void setHelmet(ItemStack item) { }
    @Override public void setItemInHand(ItemStack item) { }
    @Override public void setLeftArmPose(EulerAngle pose) { }
    @Override public void setLeftLegPose(EulerAngle pose) { }
    @Override public void setLeggings(ItemStack item) { }
    @Override public void setRightArmPose(EulerAngle pose) { }
    @Override public void setRightLegPose(EulerAngle pose) { }
    @Override public void setSmall(boolean small) { }
    @Override public void setVisible(boolean visible) { }
    @Override public void setMarker(boolean marker) { }

    // Methods from LivingEntity class
    @Override public boolean addPotionEffect(PotionEffect effect) { return false; }
    @Override public boolean addPotionEffect(PotionEffect effect, boolean param) { return false; }
    @Override public boolean addPotionEffects(Collection<PotionEffect> effects) { return false; }
    @Override public void setRemoveWhenFarAway(boolean remove) { }
    @Override public void setAI(boolean ai) { }
    @Override public void setCanPickupItems(boolean pickup) { }
    @Override public void setCollidable(boolean collidable) { }
    @Override public void setGliding(boolean gliding) {	}
    @Override public boolean setLeashHolder(Entity holder) { return false; }
    @Override public void setSwimming(boolean swimming) { }

    // Methods from Entity class
    @Override public void setVelocity(Vector vel) { }
    @Override public boolean teleport(Location loc) { return false; }
    @Override public boolean teleport(Entity entity) { return false; }
    @Override public boolean teleport(Location loc, PlayerTeleportEvent.TeleportCause cause) { return false; }
    @Override public boolean teleport(Entity entity, PlayerTeleportEvent.TeleportCause cause) { return false; }
    @Override public void setFireTicks(int ticks) { }
    @Override public boolean setPassenger(Entity entity) { return false; }
    @Override public boolean eject() { return false; }
    @Override public boolean leaveVehicle() { return false; }
    @Override public void playEffect(EntityEffect effect) { }
    @Override public void setCustomName(String name) { }
    @Override public void setCustomNameVisible(boolean flag) { }
    @Override public void setGlowing(boolean flag) { }
    @Override public void setGravity(boolean gravity) { }
    @Override public void setInvulnerable(boolean flag) { }
    @Override public void setMomentum(Vector value) { }
    @Override public void setSilent(boolean flag) { }
    @Override public void setTicksLived(int value) { }
    @Override public void setPersistent(boolean flag) { }
    @Override public void setRotation(float yaw, float pitch) { }
}
