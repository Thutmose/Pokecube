package pokecube.core.moves.animations.presets.parametric;

import org.nfunk.jep.JEP;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldEventListener;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;

@AnimPreset(getPreset = "sphFunc")
public class SphericalFunction extends MoveAnimationBase
{
    JEP     radial;
    JEP     theta;
    JEP     phi;

    Vector3 v        = Vector3.getNewVector();
    boolean reverse  = false;
    boolean absolute = false;
    Vector3 v1       = Vector3.getNewVector();

    public SphericalFunction()
    {
    }

    @Override
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
    {

    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        Vector3 source = reverse ? info.source : info.target;
        initColour((info.attacker.getEntityWorld().getWorldTime()) * 20, 0, info.move);
        Vector3 temp = Vector3.getNewVector();
        double scale = this.width;
        if (!absolute)
        {
            if (reverse && info.attacker != null)
            {
                scale *= info.attacker.width;
            }
            else if (!reverse && info.attacked != null)
            {
                scale *= info.attacked.width;
            }
        }
        for (double i = info.currentTick; i < info.currentTick + 1; i += density)
        {
            setVector(i, temp);
            temp.scalarMultBy(scale).addTo(source);
            PokecubeCore.proxy.spawnParticle(info.attacker.getEntityWorld(), particle, temp, null, rgba, particleLife);
        }
    }

    private void setVector(double t, Vector3 temp)
    {
        phi.setVarValue("t", t);
        double dPhi = phi.getValue();
        radial.setVarValue("t", t);
        double dR = radial.getValue();
        theta.setVarValue("t", t);
        double dTheta = theta.getValue();
        double sinTheta = MathHelper.sin((float) dTheta);
        double cosTheta = MathHelper.cos((float) dTheta);
        double rsinPhi = MathHelper.sin((float) dPhi) * dR;
        double rcosPhi = MathHelper.cos((float) dPhi) * dR;
        temp.set(rcosPhi * sinTheta, dR * cosTheta, rsinPhi * sinTheta);
    }

    @Override
    public IMoveAnimation init(String preset)
    {
        rgba = 0xFFFFFFFF;
        density = 0.5f;
        String[] args = preset.split(":");
        this.particle = "misc";
        String fr = "t";
        String fphi = "t*6.3";
        String fthe = "t*3.1-1.5";
        for (int i = 1; i < args.length; i++)
        {
            String ident = args[i].substring(0, 1);
            String val = args[i].substring(1);
            if (ident.equals("d"))
            {
                density = Float.parseFloat(val);
            }
            else if (ident.equals("w"))
            {
                width = Float.parseFloat(val);
            }
            else if (ident.equals("r"))
            {
                reverse = Boolean.parseBoolean(val);
            }
            else if (ident.equals("p"))
            {
                this.particle = val;
            }
            else if (ident.equals("l"))
            {
                particleLife = Integer.parseInt(val);
            }
            else if (ident.equals("a"))
            {
                absolute = Boolean.parseBoolean(val);
            }
            else if (ident.equals("c"))
            {
                initRGBA(val);
            }
            else if (ident.equals("f"))
            {
                String[] funcs = val.split(",");
                fr = funcs[0];
                fthe = funcs[1];
                fphi = funcs[2];
            }
            else if (ident.equals("d"))
            {
                density = Float.parseFloat(val);
            }
        }
        initJEP(fr, radial = new JEP());
        initJEP(fthe, theta = new JEP());
        initJEP(fphi, phi = new JEP());
        return this;
    }

    private void initJEP(String func, JEP jep)
    {
        jep.initFunTab();
        jep.addStandardFunctions();
        jep.initSymTab(); // clear the contents of the symbol table
        jep.addStandardConstants();
        jep.addComplex();
        // table
        jep.addVariable("t", 0);
        jep.parseExpression(func);
    }
}
