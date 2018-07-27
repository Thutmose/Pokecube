package pokecube.adventures.client.gui.trainer;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class GuiEditTrainer extends GuiScreen
{
    public static abstract class Page extends GuiScreen
    {
        protected final GuiEditTrainer parent;
        protected List<GuiTextField>   textList = Lists.newArrayList();

        public Page(GuiEditTrainer watch)
        {
            this.parent = watch;
        }

        @Override
        public void initGui()
        {
            super.initGui();
            this.mc = parent.mc;
            this.fontRenderer = parent.fontRenderer;
        }

        //Allows access below
        @Override
        protected void actionPerformed(GuiButton button) throws IOException
        {
            super.actionPerformed(button);
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            for (GuiTextField text : textList)
            {
                text.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }

        //Allows access below
        @Override
        protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
        {
            super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }

        //Allows access below
        @Override
        protected void mouseReleased(int mouseX, int mouseY, int state)
        {
            super.mouseReleased(mouseX, mouseY, state);
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException
        {
            super.keyTyped(typedChar, keyCode);
            for (GuiTextField text : textList)
            {
                text.textboxKeyTyped(typedChar, keyCode);
            }
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks)
        {
            for (GuiTextField text : textList)
            {
                text.drawTextBox();
            }
        }

        protected void onPageOpened()
        {

        }

        protected void onPageClosed()
        {

        }

        protected void initList()
        {

        }
    }

    public static final ResourceLocation TEXTURE      = new ResourceLocation(PokecubeAdv.ID,
            "textures/gui/traineredit.png");

    final List<Page>                     pages        = Lists.newArrayList();
    protected Page                       mainPage;
    protected EditAIPage                 aiPage;
    protected EditRewardsPage            rewardsPage;
    protected EditMessagesPage           messagePage;
    protected EditTradesPage             tradesPage;
    protected EditRoutePage              routesPage;
    protected List<EditPokemobPage>      pokemobPages = Lists.newArrayList();
    public final Entity                  entity;
    public final IGuardAICapability      guard;
    public final IHasPokemobs            trainer;
    public final IHasRewards             rewards;
    public final IHasMessages            messages;
    public final IHasNPCAIStates         aiStates;
    public final IPokemob                pokemob;
    private int                          index        = 0;

    public GuiEditTrainer(Entity target)
    {
        this.entity = target;
        trainer = CapabilityHasPokemobs.getHasPokemobs(target);
        rewards = CapabilityHasRewards.getHasRewards(target);
        messages = CapabilityNPCMessages.getMessages(target);
        aiStates = CapabilityNPCAIStates.getNPCAIStates(target);
        pokemob = CapabilityPokemob.getPokemobFor(target);
        if (entity != null) guard = entity.getCapability(EventsHandler.GUARDAI_CAP, null);
        else guard = null;
    }

    public List<GuiButton> getButtons()
    {
        return buttonList;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        pages.clear();
        pokemobPages.clear();

        int num = 1;
        if (trainer != null)
        {
            pages.add(mainPage = new EditTrainerPage(this));
            for (int i = 0; i < trainer.getMaxPokemobCount(); i++)
            {
                EditPokemobPage page = new EditPokemobPage(this, i, num++);
                pages.add(page);
                pokemobPages.add(page);
            }
            if (rewards != null) pages.add(rewardsPage = new EditRewardsPage(this, num++));
            if (messages != null) pages.add(messagePage = new EditMessagesPage(this, num++));
            if (aiStates != null) pages.add(aiPage = new EditAIPage(this, num++));
        }
        else if (pokemob != null)
        {
            pages.add(mainPage = new EditLivePokemobPage(this, pokemob));
        }
        else
        {
            pages.add(mainPage = new SpawnTrainerPage(this));
        }
        if (entity instanceof IMerchant)
        {
            pages.add(tradesPage = new EditTradesPage(this, num++));
        }
        if (guard != null) pages.add(routesPage = new EditRoutePage(this, num++));
        for (Page page : pages)
            page.initGui();
        pages.get(getIndex()).onPageOpened();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        Minecraft minecraft = (Minecraft) PokecubeCore.getMinecraftInstance();
        this.mc = minecraft;
        minecraft.renderEngine.bindTexture(new ResourceLocation(PokecubeAdv.ID, "textures/gui/traineredit.png"));
        int j2 = (width - 256) / 2;
        int k2 = (height - 160) / 2;
        drawTexturedModalRect(j2, k2, 0, 0, 256, 160);
        super.drawScreen(mouseX, mouseY, partialTicks);
        pages.get(getIndex()).drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        pages.get(getIndex()).handleMouseInput();
        super.handleMouseInput();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        pages.get(getIndex()).actionPerformed(button);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        pages.get(getIndex()).mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        pages.get(getIndex()).mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        pages.get(getIndex()).mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        pages.get(getIndex()).keyTyped(typedChar, keyCode);
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        pages.get(this.index).onPageClosed();
        this.index = index;
        pages.get(this.index).onPageOpened();
    }
}
