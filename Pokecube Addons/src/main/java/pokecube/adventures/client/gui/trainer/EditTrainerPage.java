package pokecube.adventures.client.gui.trainer;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.interfaces.PokecubeMod;
import thut.lib.CompatWrapper;

public class EditTrainerPage extends ListPage
{
    static class Button extends GuiButton
    {
        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }
    }

    static class PokemobEntry implements IGuiListEntry
    {
        final EditPokemobPage page;
        final GuiButton       button;
        final int             guiHeight;
        final int             yOffset;
        final int             xOffset;

        public PokemobEntry(EditPokemobPage page, int height, int xOffset, int yOffset)
        {
            this.page = page;
            ItemStack stack = page.parent.trainer.getPokemob(page.pokemobIndex);
            String name = I18n.format("traineredit.button.newpokemob");
            if (CompatWrapper.isValid(stack)) name = stack.getDisplayName();
            button = new Button(0, 0, 0, 80, 20, name);
            this.guiHeight = height;
            this.yOffset = yOffset;
            this.xOffset = xOffset;
        }

        @Override
        public void updatePosition(int p_192633_1_, int p_192633_2_, int p_192633_3_, float p_192633_4_)
        {
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                boolean isSelected, float partialTicks)
        {
            boolean fits = true;
            button.x = x - 2;
            button.y = y - 4;
            fits = fits && button.y + button.height <= yOffset + guiHeight;
            if (fits)
            {
                button.drawButton(page.parent.mc, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            boolean fits = true;
            fits = button.y >= yOffset;
            fits = fits && mouseX - button.x >= 0;
            fits = fits && mouseX - button.x <= button.width;
            fits = fits && button.y + button.height <= yOffset + guiHeight;
            if (button.isMouseOver())
            {
                button.playPressSound(this.page.mc.getSoundHandler());
                this.page.parent.setIndex(this.page.pageIndex);
            }
            return fits;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
        }

    }

    private static final int TOGGLEPREFIX = 2;
    private static final int PREVTYPE     = 3;
    private static final int NEXTYPE      = 4;
    private static final int TOGGLEGENDER = 5;
    private static final int KILL         = 6;

    boolean                  stationary   = false;
    boolean                  resetTeam    = false;

    private int              AIINDEX      = -1;
    private int              MESSAGEINDEX = -1;
    private int              REWARDSINDEX = -1;
    private int              TRADESINDEX  = -1;
    private int              ROUTESINDEX  = -1;

    public EditTrainerPage(GuiEditTrainer watch)
    {
        super(watch);
    }

    @Override
    protected void initList()
    {
        this.mc = parent.mc;
        // Pokemob page buttons
        int num = parent.trainer.countPokemon();
        List<IGuiListEntry> entries = Lists.newArrayList();
        int x = parent.width / 2;
        int y = parent.height / 2;
        int height = 160;
        int xOffset = x - 120;
        int yOffset = y - 50;
        /** If trainer has max pokemobs, don't try to add more to the list. */
        num = Math.min(num, parent.trainer.getMaxPokemobCount() - 1);
        // Less than or equal to to result in the "add new" page.
        for (int i = 0; i <= num; i++)
        {
            PokemobEntry entry = new PokemobEntry(parent.pokemobPages.get(i), height, xOffset, yOffset);
            entries.add(entry);
        }
        list = new ScrollGui(mc, 100, height, 20, xOffset, yOffset, entries);
    }

    @Override
    public void initGui()
    {
        super.initGui();
        int x = parent.width / 2;
        int y = parent.height / 2;
        textList.add(new GuiTextField(0, fontRenderer, x - 120, y - 70, 70, 10));
        String type = parent.trainer.getType().name;
        textList.get(0).setText(parent.entity.getName().replaceFirst(type + " ", ""));
        if (parent.entity instanceof EntityTrainer)
        {
            textList.add(new GuiTextField(1, fontRenderer, x + 50, y - 40, 70, 10));
            textList.get(1).setText((((EntityTrainer) parent.entity).urlSkin));
            textList.add(new GuiTextField(2, fontRenderer, x + 50, y - 18, 70, 10));
            textList.get(2).setText((((EntityTrainer) parent.entity).playerName));
        }
    }

    protected void onPageOpened()
    {
        int x = parent.width / 2;
        int y = parent.height / 2;
        int dx = 63;
        int dy = -5;
        if (parent.aiPage != null) parent.getButtons().add(new Button(AIINDEX = parent.aiPage.index, x + dx, y + dy, 60,
                12, I18n.format("traineredit.button.ai")));
        String next = ">";
        String prev = "<";
        // Cycle Trainer Type buttons
        int o = -20;
        parent.getButtons().add(new Button(PREVTYPE, x + o, y - 55, 20, 13, prev));
        parent.getButtons().add(new Button(NEXTYPE, x + 20 + o, y - 55, 20, 13, next));

        parent.getButtons().add(new Button(TOGGLEPREFIX, x - 30 + o, y - 72, 20, 13, "P"));
        // Gender button
        String gender = parent.trainer.getGender() == 1 ? "\u2642" : "\u2640";
        parent.getButtons().add(new Button(TOGGLEGENDER, x + 45, y - 75, 20, 13, gender));
        // Kill button
        parent.getButtons().add(new Button(KILL, x + dx, y + dy + 67, 60, 12, I18n.format("traineredit.button.kill")));

        // Messages and rewards buttons.
        if (parent.messagePage != null) parent.getButtons().add(new Button(MESSAGEINDEX = parent.messagePage.index,
                x + dx, y + dy + 12, 60, 12, I18n.format("traineredit.button.messages")));
        if (parent.rewardsPage != null) parent.getButtons().add(new Button(REWARDSINDEX = parent.rewardsPage.index,
                x + dx, y + dy + 24, 60, 12, I18n.format("traineredit.button.rewards")));
        if (parent.tradesPage != null) parent.getButtons().add(new Button(TRADESINDEX = parent.tradesPage.index, x + dx,
                y + dy + 36, 60, 12, I18n.format("traineredit.button.trades")));
        if (parent.routesPage != null) parent.getButtons().add(new Button(ROUTESINDEX = parent.routesPage.index, x + dx,
                y + dy + 48, 60, 12, I18n.format("traineredit.button.routes")));

    }

    @Override
    protected void onPageClosed()
    {
        super.onPageClosed();
        this.parent.getButtons().removeIf(new Predicate<GuiButton>()
        {
            @Override
            public boolean test(GuiButton t)
            {
                return t instanceof Button;
            }
        });
    }

    protected void drawTitle(int mouseX, int mouseY, float partialTicks)
    {
        int x = (parent.width) / 2;
        int y = (parent.height) / 2 - 70;
        drawCenteredString(fontRenderer, parent.trainer.getType().name, x, y, 0xFFFFFFFF);
        if (textList.size() > 1)
        {
            String name = I18n.format("traineredit.gui.urlskin");
            drawString(fontRenderer, name, x + 50, y + 20, 0xFFFFFFFF);
            name = I18n.format("traineredit.gui.playerskin");
            drawString(fontRenderer, name, x + 50, y + 42, 0xFFFFFFFF);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        if (textList.get(0).isFocused() && keyCode == Keyboard.KEY_RETURN)
        {
            String name = textList.get(0).getText();
            boolean hasPrefix = parent.entity.getName().startsWith(parent.trainer.getType().name + " ");
            if (hasPrefix) name = parent.trainer.getType().name + " " + name;
            PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            packet.data.setInteger("I", parent.entity.getEntityId());
            packet.data.setString("N", name);
            PokecubeMod.packetPipeline.sendToServer(packet);
        }
        if (textList.size() > 1 && keyCode == Keyboard.KEY_RETURN)
        {
            if (textList.get(1).isFocused())
            {
                String name = textList.get(1).getText();
                PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
                packet.data.setInteger("I", parent.entity.getEntityId());
                packet.data.setString("U", name);
                PokecubeMod.packetPipeline.sendToServer(packet);
            }
            if (textList.get(2).isFocused())
            {
                String name = textList.get(2).getText();
                PacketTrainer packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
                packet.data.setInteger("I", parent.entity.getEntityId());
                packet.data.setString("P", name);
                PokecubeMod.packetPipeline.sendToServer(packet);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        PacketTrainer packet;
        int dir = 0;
        if (button.id == AIINDEX)
        {
            parent.setIndex(AIINDEX);
            return;
        }
        if (button.id == MESSAGEINDEX)
        {
            parent.setIndex(MESSAGEINDEX);
            return;
        }
        if (button.id == REWARDSINDEX)
        {
            parent.setIndex(REWARDSINDEX);
            return;
        }
        if (button.id == TRADESINDEX)
        {
            parent.setIndex(TRADESINDEX);
            return;
        }
        if (button.id == ROUTESINDEX)
        {
            parent.setIndex(ROUTESINDEX);
            return;
        }
        switch (button.id)
        {
        case TOGGLEGENDER:
            byte sexe = parent.trainer.getGender();
            byte valid = parent.trainer.getType().genders;
            if (valid == 3)
            {
                sexe = (byte) (sexe == 1 ? 2 : 1);
            }
            else sexe = valid;
            parent.trainer.setGender(sexe);
            String gender = sexe == 1 ? "\u2642" : "\u2640";
            button.displayString = gender;
            packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            packet.data.setInteger("I", parent.entity.getEntityId());
            packet.data.setByte("X", sexe);
            PokecubeMod.packetPipeline.sendToServer(packet);
            break;
        case NEXTYPE:
            dir = 1;
            break;
        case PREVTYPE:
            dir = -1;
            break;
        case TOGGLEPREFIX:
            String name = textList.get(0).getText();
            boolean hasPrefix = parent.entity.getName().startsWith(parent.trainer.getType().name + " ");
            if (!hasPrefix) name = parent.trainer.getType().name + " " + name;
            packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            packet.data.setInteger("I", parent.entity.getEntityId());
            packet.data.setString("N", name);
            PokecubeMod.packetPipeline.sendToServer(packet);
            break;
        case KILL:
            packet = new PacketTrainer(PacketTrainer.MESSAGEKILLTRAINER);
            packet.data.setInteger("I", parent.entity.getEntityId());
            PokecubeMod.packetPipeline.sendToServer(packet);
            parent.mc.player.closeScreen();
            break;
        }
        // Change type
        if (dir != 0)
        {
            List<TypeTrainer> types = Lists.newArrayList();
            types.addAll(TypeTrainer.typeMap.values());
            types.sort(new Comparator<TypeTrainer>()
            {
                @Override
                public int compare(TypeTrainer o1, TypeTrainer o2)
                {
                    return o1.name.compareTo(o2.name);
                }
            });
            int index = -1;
            for (int i = 0; i < types.size(); i++)
            {
                if (types.get(i) == parent.trainer.getType())
                {
                    index = i;
                }
            }
            if (dir == -1)
            {
                if (index <= 0)
                {
                    index = types.size() - 1;
                }
                else
                {
                    index--;
                }
            }
            else
            {
                if (index >= types.size() - 1)
                {
                    index = 0;
                }
                else
                {
                    index++;
                }
            }
            TypeTrainer type = types.get(index);
            packet = new PacketTrainer(PacketTrainer.MESSAGEUPDATETRAINER);
            packet.data.setInteger("I", parent.entity.getEntityId());
            packet.data.setString("K", type.name);
            PokecubeMod.packetPipeline.sendToServer(packet);

        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        int x = (parent.width) / 2;
        int y = (parent.height) / 2 + 65;
        int zoom = 40;
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 50F);
        GL11.glScalef(-zoom, zoom, zoom);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
        EntityLivingBase entity = (EntityLivingBase) parent.entity;
        RenderHelper.enableStandardItemLighting();
        float turn = entity.ticksExisted + partialTicks - entity.renderYawOffset;
        GL11.glRotatef(turn, 0.0F, 1.0F, 0.0F);
        GL11.glColor3f(1, 1, 1);
        double dy = -0.123;
        double dx = 0;
        double dz = 0;
        Minecraft.getMinecraft().getRenderManager().renderEntity(entity, dx, dy, dz, 0, 1.5F, false);
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);

        super.drawScreen(mouseX, mouseY, partialTicks);
        drawTitle(mouseX, mouseY, partialTicks);
    }
}
