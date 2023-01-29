package com.encraft.dz;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

import com.encraft.dz.inventory.InventoryBuildingKit;

public class ExtendedPlayer implements IExtendedEntityProperties {

    public final static String EXT_PROP_NAME = "ExtendedPlayerProp";

    private final EntityPlayer player;

    /** Custom inventory slots */

    public final InventoryBuildingKit inventorybk = new InventoryBuildingKit();

    public ExtendedPlayer(EntityPlayer player) {
        this.player = player;
    }

    public static final void register(EntityPlayer player) {
        player.registerExtendedProperties(ExtendedPlayer.EXT_PROP_NAME, new ExtendedPlayer(player));
    }

    public static final ExtendedPlayer get(EntityPlayer player) {
        return (ExtendedPlayer) player.getExtendedProperties(EXT_PROP_NAME);
    }

    public void copy(ExtendedPlayer props) {

        inventorybk.copy(props.inventorybk);
    }

    @Override
    public final void saveNBTData(NBTTagCompound compound) {

        NBTTagCompound properties = new NBTTagCompound();
        inventorybk.writeToNBT(properties);
        compound.setTag(EXT_PROP_NAME, properties);
    }

    @Override
    public final void loadNBTData(NBTTagCompound compound) {

        NBTTagCompound properties = (NBTTagCompound) compound.getTag(EXT_PROP_NAME);
        inventorybk.readFromNBT(properties);
    }

    public void onUpdate() {
        // only want to update the timer on the server:
        if (!player.worldObj.isRemote) {

        }
    }

    @Override
    public void init(Entity entity, World world) {}

}
