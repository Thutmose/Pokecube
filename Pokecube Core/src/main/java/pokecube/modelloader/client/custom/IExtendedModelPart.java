package pokecube.modelloader.client.custom;

import java.util.HashMap;

import pokecube.core.utils.Vector4;
import pokecube.modelloader.client.custom.oldforgestuff.IModelCustom;
import thut.api.maths.Vector3;

public interface IExtendedModelPart extends IModelCustom{
	public int[] getRGBAB();
	public void setRGBAB(int[] arrays);
	public void setPreRotations(Vector4 rotations);
	public void setPreTranslations(Vector3 translations);
	public void setPostRotations(Vector4 rotations);
	public void setPostRotations2(Vector4 rotations);
	public void setPostTranslations(Vector3 translations);
	public Vector3 getDefaultTranslations();
	public Vector4 getDefaultRotations();
	public String getName();
	public IExtendedModelPart getParent();
	public HashMap<String, IExtendedModelPart> getSubParts();
	public void addChild(IExtendedModelPart child);
	public void setParent(IExtendedModelPart parent);
    String getType();
}
