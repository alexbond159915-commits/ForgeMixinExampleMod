package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.item.ItemFluidIdentifier;
import com.example.examplemod.network.PacketFluidIdentifierSelection;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Fluid picker for the reusable identifier.
 * No world edits happen on the client: selection is validated by the server.
 */
public class GuiFluidIdentifier extends GuiScreen
{
    private static final int VISIBLE_ROWS = 7;
    private static final int ROW_HEIGHT = 19;
    private final EnumHand hand;
    private final List<Fluid> available = new ArrayList<Fluid>();
    private GuiTextField search;
    private int page;

    public GuiFluidIdentifier(EnumHand hand)
    {
        this.hand = hand;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        int left = (width - 254) / 2;
        int top = (height - 233) / 2;

        available.clear();
        available.addAll(FluidRegistry.getRegisteredFluids().values());
        Collections.sort(available, new Comparator<Fluid>()
        {
            @Override
            public int compare(Fluid a, Fluid b)
            {
                return fluidLabel(a).compareToIgnoreCase(fluidLabel(b));
            }
        });

        search = new GuiTextField(7, fontRenderer, left + 10, top + 35, 234, 18);
        search.setMaxStringLength(64);
        search.setFocused(true);

        buttonList.clear();
        buttonList.add(new GuiButton(0, left + 10, top + 200, 55, 20, "<"));
        buttonList.add(new GuiButton(1, left + 68, top + 200, 55, 20, ">"));
        buttonList.add(new GuiButton(2, left + 126, top + 200, 55, 20, "Сброс"));
        buttonList.add(new GuiButton(3, left + 184, top + 200, 60, 20, "Закрыть"));
        page = 0;
    }

    private static String fluidLabel(Fluid fluid)
    {
        try
        {
            return fluid.getLocalizedName(new FluidStack(fluid, 1000));
        }
        catch (RuntimeException ignored)
        {
            return fluid.getName();
        }
    }

    private List<Fluid> filtered()
    {
        String filter = search.getText().trim().toLowerCase(Locale.ROOT);
        List<Fluid> found = new ArrayList<Fluid>();
        for (Fluid fluid : available)
        {
            if (filter.isEmpty()
                    || fluidLabel(fluid).toLowerCase(Locale.ROOT).contains(filter)
                    || fluid.getName().toLowerCase(Locale.ROOT).contains(filter))
            {
                found.add(fluid);
            }
        }
        return found;
    }

    private void selectFluid(Fluid fluid)
    {
        ExampleMod.NETWORK.sendToServer(new PacketFluidIdentifierSelection(
                hand, fluid == null ? "" : fluid.getName()));
        mc.displayGuiScreen(null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();

        int left = (width - 254) / 2;
        int top = (height - 233) / 2;
        drawRect(left, top, left + 254, top + 228, 0xE8212429);
        drawRect(left + 4, top + 4, left + 250, top + 28, 0xFF343C46);
        drawCenteredString(fontRenderer, "Жидкостный идентификатор",
                width / 2, top + 12, 0xFFFFFF);

        search.drawTextBox();
        List<Fluid> shown = filtered();
        int maxPage = Math.max(0, (shown.size() - 1) / VISIBLE_ROWS);
        page = Math.min(page, maxPage);

        ItemStack held = mc.player.getHeldItem(hand);
        Fluid selected = ItemFluidIdentifier.getStoredFluid(held);

        for (int i = 0; i < VISIBLE_ROWS; ++i)
        {
            int index = page * VISIBLE_ROWS + i;
            if (index >= shown.size())
            {
                break;
            }

            Fluid fluid = shown.get(index);
            int rowTop = top + 59 + i * ROW_HEIGHT;
            boolean hover = mouseX >= left + 10 && mouseX < left + 244
                    && mouseY >= rowTop && mouseY < rowTop + ROW_HEIGHT - 1;
            boolean active = selected == fluid;
            drawRect(left + 10, rowTop, left + 244, rowTop + ROW_HEIGHT - 1,
                    active ? 0xFF505A66 : (hover ? 0xFF424B56 : 0xFF303740));
            drawRect(left + 14, rowTop + 3, left + 27, rowTop + 15,
                    0xFF000000 | (fluid.getColor() & 0xFFFFFF));
            String label = fluidLabel(fluid);
            if (fontRenderer.getStringWidth(label) > 208)
            {
                label = fontRenderer.trimStringToWidth(label, 197) + "...";
            }
            fontRenderer.drawString(label, left + 33, rowTop + 5,
                    active ? 0xA7FFB3 : 0xF0F0F0);

            if (hover)
            {
                drawHoveringText(fluid.getName(), mouseX, mouseY);
            }
        }

        fontRenderer.drawString("Найдено: " + shown.size()
                + "  Страница " + (page + 1) + "/" + (maxPage + 1),
                left + 10, top + 190, 0xD0D0D0);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        int maxPage = Math.max(0, (filtered().size() - 1) / VISIBLE_ROWS);
        if (button.id == 0) page = Math.max(0, page - 1);
        if (button.id == 1) page = Math.min(maxPage, page + 1);
        if (button.id == 2) selectFluid(null);
        if (button.id == 3) mc.displayGuiScreen(null);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
            throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        search.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) return;
        int left = (width - 254) / 2;
        int top = (height - 233) / 2;
        if (mouseX < left + 10 || mouseX >= left + 244
                || mouseY < top + 59 || mouseY >= top + 59 + ROW_HEIGHT * VISIBLE_ROWS)
        {
            return;
        }

        int index = page * VISIBLE_ROWS + (mouseY - top - 59) / ROW_HEIGHT;
        List<Fluid> shown = filtered();
        if (index < shown.size())
        {
            selectFluid(shown.get(index));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (search.textboxKeyTyped(typedChar, keyCode))
        {
            page = 0;
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0)
        {
            int maxPage = Math.max(0, (filtered().size() - 1) / VISIBLE_ROWS);
            page = Math.max(0, Math.min(maxPage, page + (wheel < 0 ? 1 : -1)));
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
