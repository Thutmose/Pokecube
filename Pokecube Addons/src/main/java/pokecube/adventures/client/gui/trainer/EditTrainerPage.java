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
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import pokecube.adventures.client.gui.trainer.GuiEditTrainer.Page;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.interfaces.PokecubeMod;
import thut.lib.CompatWrapper;

public class EditTrainerPage extends Page
{
    static class Button extends GuiButton
    {
        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }
    }

    private static final int AIPAGE       = 1;
    private static final int TOGGLEPREFIX = 2;
    private static final int PREVTYPE     = 3;
    private static final int NEXTYPE      = 4;
    private static final int TOGGLEGENDER = 5;
    private static final int KILL         = 6;

    private static final int POKE0        = 7;
    private static final int POKE1        = 8;
    private static final int POKE2        = 9;
    private static final int POKE3        = 10;
    private static final int POKE4        = 11;
    private static final int POKE5        = 12;

    private static final int MESSAGEPAGE  = 13;
    private static final int REWARDSPAGE  = 14;

    boolean                  stationary   = false;
    boolean                  resetTeam    = false;

    public EditTrainerPage(GuiEditTrainer watch)
    {
        super(watch);
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
        if (parent.aiStates != null)
            parent.getButtons().add(new Button(AIPAGE, x + dx, y + dy, 60, 20, I18n.format("traineredit.button.ai")));
        String next = ">";
        String prev = "<";
        // Cycle Trainer Type buttons
        int o = -20;
        parent.getButtons().add(new Button(PREVTYPE, x + o, y - 55, 20, 20, prev));
        parent.getButtons().add(new Button(NEXTYPE, x + 20 + o, y - 55, 20, 20, next));

        parent.getButtons().add(new Button(TOGGLEPREFIX, x - 30 + o, y - 75, 20, 20, "P"));
        // Gender button
        String gender = parent.trainer.getGender() == 1 ? "\u2642" : "\u2640";
        parent.getButtons().add(new Button(TOGGLEGENDER, x + 45, y - 75, 20, 20, gender));
        // Kill button
        parent.getButtons().add(new Button(KILL, x + dx, y + dy + 60, 60, 20, I18n.format("traineredit.button.kill")));

        // Pokemob page buttons
        int num = parent.trainer.countPokemon();
        num = Math.min(5, num);
        for (int i = POKE0; i <= POKE0 + num; i++)
        {
            int index = (i - POKE0);
            ItemStack stack = parent.trainer.getPokemob(index);
            String name = I18n.format("traineredit.button.newpokemob");
            if (CompatWrapper.isValid(stack)) name = stack.getDisplayName();
            parent.getButtons().add(new Button(i, x - 120, y - 50 + 20 * index, 80, 20, name));
        }
        if (parent.messages != null) parent.getButtons()
                .add(new Button(MESSAGEPAGE, x + dx, y + dy + 20, 60, 20, I18n.format("traineredit.button.messages")));
        if (parent.rewards != null) parent.getButtons()
                .add(new Button(REWARDSPAGE, x + dx, y + dy + 40, 60, 20, I18n.format("traineredit.button.rewards")));

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
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        PacketTrainer packet;
        int dir = 0;
        switch (button.id)
        {
        case AIPAGE:
            parent.setIndex(9);
            break;
        case MESSAGEPAGE:
            parent.setIndex(8);
            break;
        case REWARDSPAGE:
            parent.setIndex(7);
            break;
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
        case POKE0:
            parent.setIndex(1);
            break;
        case POKE1:
            parent.setIndex(2);
            break;
        case POKE2:
            parent.setIndex(3);
            break;
        case POKE3:
            parent.setIndex(4);
            break;
        case POKE4:
            parent.setIndex(5);
            break;
        case POKE5:
            parent.setIndex(6);
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
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
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
        Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0, -0.123456, 0, 0, 1.5F, false);
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
