package com.corrinedev.tacznpcs.common.entity.inventory;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

public class ScavInventory extends ChestMenu {
    public AbstractScavEntity attachedEntity;
    public ScavInventory(MenuType<?> pType, int pContainerId, Inventory pPlayerInventory, Container pContainer, int pRows, AbstractScavEntity entity) {
        super(pType, pContainerId, pPlayerInventory, pContainer, pRows);
        this.attachedEntity = entity;
    }
    public static ScavInventory generate(int pContainerId, Inventory pPlayerInventory, Container pContainer, AbstractScavEntity entity) {
        return new ScavInventory(MenuType.GENERIC_9x3, pContainerId, pPlayerInventory, pContainer, 3, entity);
    }
}
