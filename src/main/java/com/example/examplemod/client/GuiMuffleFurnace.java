package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.container.ContainerMuffleFurnace;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiMuffleFurnace extends GuiContainer
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    ExampleMod.MODID,
                    "textures/gui/muffle_furnace.png"
            );

    private final ContainerMuffleFurnace container;

    public GuiMuffleFurnace(
            InventoryPlayer playerInventory,
            com.example.examplemod.tileentity.TileEntityMuffleFurnace furnace)
    {
        super(new ContainerMuffleFurnace(playerInventory, furnace));

        this.container =
                (ContainerMuffleFurnace) this.inventorySlots;

        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(
            float partialTicks,
            int mouseX,
            int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(TEXTURE);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        // Основная текстура GUI
        drawTexturedModalRect(
                x,
                y,
                0,
                0,
                xSize,
                ySize
        );

        // Прогресс плавки
        int cookTime = container.getCookTime();
        int totalCookTime = container.getTotalCookTime();

        if (cookTime > 0 && totalCookTime > 0)
        {
            int progress =
                    cookTime * 24 / totalCookTime;

            drawRect(
                    x + 89,
                    y + 40,
                    x + 89 + progress,
                    y + 44,
                    0xFFFFA500
            );
        }

        // Полоска горения топлива
        int burnTime =
                container.getBurnTime();

        int maxBurn =
                container.getCurrentItemBurnTime();

        if (burnTime > 0 && maxBurn > 0)
        {
            int flame =
                    burnTime * 13 / maxBurn;

            drawRect(
                    x + 60,
                    y + 71 - flame,
                    x + 64,
                    y + 71,
                    0xFFFFA500
            );
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(
            int mouseX,
            int mouseY)
    {
        fontRenderer.drawString(
                "Муфельная печь",
                8,
                6,
                0x404040
        );
    }
}