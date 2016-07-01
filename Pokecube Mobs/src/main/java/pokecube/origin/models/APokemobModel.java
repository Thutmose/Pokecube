/**
 * 
 */
package pokecube.origin.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Overrides methods to give actual names to the parameters and provides some basic animations tools. 
 * 
 * @author Manchou
 * @author Oracion
 *
 */
public class APokemobModel extends ModelBase {

	public final static float pi = (float) Math.PI;
	
	/**
	 * 
	 */
	public APokemobModel() {
		super();
		
	}

    /**
     * Sets the models various rotation angles then renders the model.
     */
	@Override
	public void render(Entity entity, float walktime, float walkspeed, float time, float rotationYaw, float rotationPitch, float scale) {
		super.render(entity, walktime, walkspeed, time, rotationYaw, rotationPitch, scale);
	}
	
    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     * 
     * source for doc: http://schwarzeszeux.tumblr.com/post/14176343101/minecraft-modeling-animation-part-2-of-x
     * 
     * @param walktime clock for the walk animation
     * @param walkspeed speed for the walk animation
     * @param time clock to use for breathe or floating effect
     * @param rotationYaw for head rotation
     * @param rotationPitch for head rotation
     * @param scale rarely used in animation
     * @param entity the Entity
     */
	@Override
    public void setRotationAngles(float walktime, float walkspeed, float time, float rotationYaw, float rotationPitch, float scale, Entity entity) {
		super.setRotationAngles(walktime, walkspeed, time, rotationYaw, rotationPitch, scale, entity);
	}

	protected static float radToDegree(float value){
		return value * 180F / pi;
	}
	
	protected static float degreeToRad(float value){
		return value * pi / 180F;
	}
	
	protected void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
	
	/**
	 * Like setRotation but gives a floating aspect with pseudo-random oscillation.
	 * 
	 * @param model
	 * @param f2
	 * @param x
	 * @param y
	 * @param z
	 * @param speed
	 */
	protected void setRotationFloating(ModelRenderer model, float time, float x, float y, float z, float factor, float speed) {
		model.rotateAngleX = x + factor * MathHelper.sin(time * speed);
		model.rotateAngleY = y + factor * MathHelper.cos(time * speed +3);
		model.rotateAngleZ = z - factor * MathHelper.sin(time * speed +5);
	}
	
	/**
	 * Like setRotation but gives a floating aspect with pseudo-random oscillation.
	 * 
	 * @param model
	 * @param f2
	 * @param x
	 * @param y
	 * @param z
	 */
	protected void setRotationFloating(ModelRenderer model, float time, float x, float y, float z) {
		setRotationFloating(model, time, x, y, z, 0.05F, 0.12F);
	}

	/**
	 * Like setRotationPoint but gives a floating aspect with oscillation
	 * on the Y axis.
	 * 
	 * @param model
	 * @param f2
	 * @param x
	 * @param y
	 * @param z
	 * @param speed
	 */
	protected void setRotationPointFloating(ModelRenderer model, float time, float x, float y, float z, float factor, float speed) {
		model.setRotationPoint(x, y + factor * MathHelper.cos(time * speed), z);
	}
	
	/**
	 * Like setRotation but gives a floating aspect with pseudo-random oscillation.
	 * 
	 * @param model
	 * @param f2
	 * @param x
	 * @param y
	 * @param z
	 */
	protected void setRotationPointFloating(ModelRenderer model, float time, float x, float y, float z) {
		setRotationPointFloating(model, time, x, y, z, 1F, 0.12F);
	}
	
	protected void walkQuadruped(
			ModelRenderer backRight, ModelRenderer backLeft, 
			ModelRenderer frontRight, ModelRenderer frontLeft, 
			float time, float walkspeed){
		
		walkQuadruped(backRight, backLeft, frontRight, frontLeft, time, walkspeed, 0.6662F, 1.4F);
	}
	
	protected void walkQuadruped(
			ModelRenderer backRight, ModelRenderer backLeft, 
			ModelRenderer frontRight, ModelRenderer frontLeft, 
			float walktime, float walkspeed, float speed, float amplitude){
		
        backRight.rotateAngleX 	= MathHelper.cos(walktime * speed) * amplitude * walkspeed;
        backLeft.rotateAngleX 	= MathHelper.cos(walktime * speed + pi) * amplitude * walkspeed;
        frontRight.rotateAngleX = MathHelper.cos(walktime * speed + pi) * amplitude * walkspeed;
        frontLeft.rotateAngleX 	= MathHelper.cos(walktime * speed) * amplitude * walkspeed;
	}
	
	protected void walkBiped(
			ModelRenderer rightLeg, ModelRenderer leftLeg, 
			float walktime, float walkspeed){
		
		walkBiped(rightLeg, leftLeg, walktime, walkspeed, 0.6662F, 1.4F);
	}
	
	protected void walkBiped(
			ModelRenderer rightLeg, ModelRenderer leftLeg, 
			float walktime, float walkspeed, float speed, float amplitude){
		
        rightLeg.rotateAngleX = MathHelper.cos(walktime * speed) * amplitude * walkspeed;
        leftLeg.rotateAngleX = MathHelper.cos(walktime * speed + pi) * amplitude * walkspeed;
	}
	
	/**
	 * Simple animates the head.
	 * 
	 * @param head
	 * @param rotationPitch
	 * @param rotationYaw
	 */
	protected void animateHead(ModelRenderer head, float rotationPitch, float rotationYaw){
		animateHead(head, rotationPitch, rotationYaw, 0, 0);
	}
	
	/**
	 * Animate only the X-rotation of the head.
	 * @param head
	 * @param rotationPitch
	 * @param modifier
	 */
	protected static void animateHeadX(ModelRenderer head, float rotationPitch, float modifier)
	{
	    head.rotateAngleX = degreeToRad(rotationPitch) + modifier;
	}

	/**
	 * Animate only the Y-rotation of the head.
	 * @param head
	 * @param rotationYaw
	 * @param modifier
	 */
	protected static void animateHeadY(ModelRenderer head, float rotationYaw, float modifier)
	{
		head.rotateAngleY = degreeToRad(rotationYaw) + modifier;
	}
	
	/**
	 * Animate both the x and y rotations of the head.
	 * 
	 * @param head
	 * @param rotationPitch
	 * @param rotationYaw
	 * @param xmod
	 * @param ymod
	 */
	protected static void animateHead(ModelRenderer head, float rotationPitch, float rotationYaw, float xmod, float ymod)
	{
	    animateHeadX(head, rotationPitch, xmod);
	    animateHeadY(head, rotationYaw, ymod);
	}
	
	/**
	 * Animates the wings when the mob is flying.
	 * 
	 * @param rightWing
	 * @param leftWing
	 * @param time
	 * @param amplitudeAngle
	 * @param offsetAngle
	 */
	protected void animateFlyZ(ModelRenderer rightWing, ModelRenderer leftWing, float time, float speed, float amplitudeAngle, float offsetAngle){
		if (rightWing!=null)
			rightWing.rotateAngleZ = (MathHelper.cos(time * speed)+1) * amplitudeAngle + offsetAngle;
		if (leftWing != null) 
			leftWing.rotateAngleZ = -((MathHelper.cos(time * speed)+1) * amplitudeAngle + offsetAngle);
	}
	
	/**
	 * Used by Mareep.
	 * 
	 * @param woolMeta
	 * @return
	 */
	public static int[] getColours(int woolMeta)
	{
		int [] ret = {255,255,255};
		switch (woolMeta) 
		{
		case 0:
			ret[0] = 29; ret[1] = 29; ret[2] = 29;
			return ret;
		case 1:
			ret[0] = 220; ret[1] = 38; ret[2] = 2;
			return ret;
		case 2:
			ret[0] = 44; ret[1] = 100; ret[2] = 2;
			return ret;
		case 3:
			ret[0] = 172; ret[1] = 100; ret[2] = 45;
			return ret;
		case 4:
			ret[0] = 63; ret[1] = 130; ret[2] = 253;
			return ret;
		case 5:
			ret[0] = 124; ret[1] = 67; ret[2] = 253;
			return ret;
		case 6:
			ret[0] = 2; ret[1] = 175; ret[2] = 190;
			return ret;
		case 7:
			ret[0] = 117; ret[1] = 117; ret[2] = 117;
			return ret;
		case 8:
			ret[0] = 84; ret[1] = 84; ret[2] = 84;
			return ret;
		case 9:
			ret[0] = 252; ret[1] = 110; ret[2] = 254;
			return ret;
		case 10:
			ret[0] = 138; ret[1] = 224; ret[2] = 80;
			return ret;
		case 11:
			ret[0] = 253; ret[1] = 254; ret[2] = 108;
			return ret;
		case 12:
			ret[0] = 110; ret[1] = 254; ret[2] = 211;
			return ret;
		case 13:
			ret[0] = 171; ret[1] = 110; ret[2] = 254;
			return ret;
		case 14:
			ret[0] = 255; ret[1] = 104; ret[2] = 45;
			return ret;
		case 15:
			ret[0] = 255; ret[1] = 255; ret[2] = 255;
			return ret;
			
		}
		return ret;
	}
	
}
