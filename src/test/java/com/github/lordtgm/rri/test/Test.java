package com.github.lordtgm.rri.test;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

public class Test {
    public static void main(String[] args) {
        Tag<String> tag = Tag.Transient("test");
        Block block = Block.STONE.withTag(tag, "test value");
        ItemStack itemstack = ItemStack.of(Material.STONE).withTag(tag, "test value");
        System.out.println(block.getTag(tag));
        System.out.println(itemstack.getTag(tag));
    }
}
