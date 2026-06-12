package com.encraft.dz;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

import com.encraft.dz.inventory.InventoryOreFinder;

public class OreFinderPlayerData implements IExtendedEntityProperties {

    public final static String EXT_PROP_NAME = "ExtendedPlayerProp";

    private final EntityPlayer player;

    /** Custom inventory slots */

    public final InventoryOreFinder filterInventory = new InventoryOreFinder();

    private boolean hasLastScan = false;
    private int lastScanX;
    private int lastScanY;
    private int lastScanZ;
    private ItemStack lastScanFilter;

    public OreFinderPlayerData(EntityPlayer player) {
        this.player = player;
    }

    public static final void register(EntityPlayer player) {
        player.registerExtendedProperties(OreFinderPlayerData.EXT_PROP_NAME, new OreFinderPlayerData(player));
    }

    public static final OreFinderPlayerData get(EntityPlayer player) {
        return (OreFinderPlayerData) player.getExtendedProperties(EXT_PROP_NAME);
    }

    public void copy(OreFinderPlayerData props) {

        filterInventory.copy(props.filterInventory);
    }

    public boolean isSameScan(int x, int y, int z, ItemStack filter) {
        return hasLastScan && x == lastScanX
                && y == lastScanY
                && z == lastScanZ
                && ItemStack.areItemStacksEqual(lastScanFilter, filter);
    }

    public void rememberScan(int x, int y, int z, ItemStack filter) {
        hasLastScan = true;
        lastScanX = x;
        lastScanY = y;
        lastScanZ = z;
        lastScanFilter = filter == null ? null : filter.copy();
    }

    public void clearLastScan() {
        hasLastScan = false;
        lastScanFilter = null;
    }

    @Override
    public final void saveNBTData(NBTTagCompound compound) {

        NBTTagCompound properties = new NBTTagCompound();
        filterInventory.writeToNBT(properties);
        compound.setTag(EXT_PROP_NAME, properties);
    }

    @Override
    public final void loadNBTData(NBTTagCompound compound) {

        NBTTagCompound properties = (NBTTagCompound) compound.getTag(EXT_PROP_NAME);
        filterInventory.readFromNBT(properties);
    }

    @Override
    public void init(Entity entity, World world) {}

}
