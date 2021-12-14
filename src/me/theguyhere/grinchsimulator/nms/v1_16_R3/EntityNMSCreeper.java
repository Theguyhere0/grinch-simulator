package me.theguyhere.grinchsimulator.nms.v1_16_R3;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

import java.util.Objects;

public class EntityNMSCreeper extends EntityCreeper {

    public EntityNMSCreeper(Location location) {
        super(EntityTypes.CREEPER, ((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle());
        super.collides = false;
        super.setPosition(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public void tick() {
        // Disable normal ticking
    }

    @Override
    public void inactiveTick() {
        // Disable normal ticking
    }

    @Override
    public void saveData(NBTTagCompound nbtTagCompound) {
        // Prevent saving NBT
    }

    @Override
    public boolean a_(NBTTagCompound nbttagcompound) {
        // Prevent saving NBT
        return false;
    }

    @Override
    public boolean d(NBTTagCompound nbttagcompound) {
        // Prevent saving NBT
        return false;
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        // Prevent saving NBT
        return nbttagcompound;
    }

    @Override
    public void load(NBTTagCompound nbttagcompound) {
        // Prevent loading NBT
    }

    @Override
    public void loadData(NBTTagCompound nbttagcompound) {
        // Prevent loading NBT
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public void setCustomName(IChatBaseComponent ichatbasecomponent) {
        // Lock the custom name
    }

    @Override
    public void setCustomNameVisible(boolean visible) {
        // Lock the custom name
    }

    @Override
    public EnumInteractionResult a(EntityHuman human, Vec3D vec3d, EnumHand enumhand) {
        // Prevent stand being equipped
        return EnumInteractionResult.PASS;
    }

    @Override
    public boolean a_(int i, ItemStack item) {
        // Prevent stand being equipped
        return false;
    }

    @Override
    public void setSlot(EnumItemSlot enumitemslot, ItemStack itemstack) {
        // Prevent stand being equipped
    }

    @Override
    public void a(AxisAlignedBB boundingBox) {
        // Prevent changing alignment
    }

    public void forceSetBoundingBox(AxisAlignedBB boundingBox) {
        super.a(boundingBox);
    }

    @Override
    public void playSound(SoundEffect soundeffect, float f, float f1) {
        // Remove sounds.
    }

    @Override
    public void die() {
        // Prevent being killed.
    }

    public CraftNMSCreeper getBukkitEntity() {
        return new CraftNMSCreeper(super.world.getServer(), this);
    }
}
