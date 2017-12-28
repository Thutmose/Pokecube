package pokecube.core.moves.animations.presets.parametric;

import org.nfunk.jep.JEP;

import net.minecraft.world.IWorldEventListener;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;
import thut.api.maths.Vector3;

@AnimPreset(getPreset = "cylFunc")
public class CylindricalFunction extends MoveAnimationBase
{
    JEP     radial;
    JEP     angular;

    Vector3 v  = Vector3.getNewVector();
    Vector3 v1 = Vector3.getNewVector();

    public CylindricalFunction()
    {
    }

    @Override
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
    {

    }

    @Override
    public void spawnClientEntities(MovePacketInfo info)
    {
        Vector3 source = info.source;
        Vector3 target = info.target;
        initColour((info.attacker.getEntityWorld().getWorldTime()) * 20, 0, info.move);
        double dist = source.distanceTo(target);
        double frac2 = info.currentTick / (float) getDuration();
        double frac = dist * frac2;
        double frac3 = dist * (info.currentTick + 1) / getDuration();
        Vector3 temp = Vector3.getNewVector().set(target).subtractFrom(source).norm();
        Vector3 temp1 = Vector3.getNewVector();
        Vector3 angleF = temp.horizonalPerp().norm();
        for (double i = frac; i < frac3; i += 0.1)
        {
            if (density < 1 && Math.random() > density) continue;
            if (i / dist > 1) return;
            setVector(angleF, temp, i / dist, temp1);
            PokecubeCore.proxy.spawnParticle(info.attacker.getEntityWorld(), particle,
                    source.add(temp.scalarMult(i).addTo(temp1)), null, rgba, particleLife);
        }
    }

    private void setVector(Vector3 horizonalPerp, Vector3 dir, double z, Vector3 temp)
    {
        angular.setVarValue("z", z);
        double angle = angular.getValue();
        horizonalPerp.rotateAboutLine(dir, angle, temp);
        temp.norm();
        radial.setVarValue("z", z);
        double r = radial.getValue();
        temp.scalarMultBy(r);
    }

    @Override
    public IMoveAnimation init(String preset)
    {
        rgba = 0xFFFFFFFF;
        String[] args = preset.split(":");
        this.particle = "misc";
        String fr = "z";
        String fphi = "0";
        for (int i = 1; i < args.length; i++)
        {
            String ident = args[i].substring(0, 1);
            String val = args[i].substring(1);
            if (ident.equals("p"))
            {
                this.particle = val;
            }
            else if (ident.equals("l"))
            {
                particleLife = Integer.parseInt(val);
            }
            else if (ident.equals("c"))
            {
                initRGBA(val);
            }
            else if (ident.equals("f"))
            {
                String[] funcs = val.split(",");
                fr = funcs[0];
                fphi = funcs[1];
            }
            else if (ident.equals("d"))
            {
                density = Float.parseFloat(val);
            }
        }
        initJEP(fr, radial = new JEP());
        initJEP(fphi, angular = new JEP());
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
        jep.addVariable("z", 0);
        jep.parseExpression(func);
    }
}
