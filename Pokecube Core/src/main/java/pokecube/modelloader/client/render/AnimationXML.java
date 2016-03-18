package pokecube.modelloader.client.render;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

public class AnimationXML
{
    @XmlRootElement(name = "model")
    public static class Model
    {
        @XmlElement(name = "phase")
        List<Phase> phases = Lists.newArrayList();
    }

    @XmlRootElement(name = "phase")
    public static class Phase
    {
        @XmlElement(name = "name")
        String             name;
        @XmlElement(name = "type")
        String             type;
        @XmlAnyAttribute
        Map<QName, String> values;
    }

    @XmlRootElement(name = "customTex")
    public static class CustomTex
    {
        @XmlAttribute(name = "default")
        String             defaults;
        @XmlAttribute(name = "smoothing")
        String             smoothing;
        @XmlAttribute(name = "animation")
        List<TexAnimation> texAnimations = Lists.newArrayList();
    }

    @XmlRootElement(name = "animation")
    public static class TexAnimation
    {
        @XmlAttribute(name = "part")
        String part;
        @XmlAttribute(name = "trigger")
        String trigger;
        @XmlAttribute(name = "diffs")
        String diffs;
        @XmlAttribute(name = "tex")
        String tex;
    }

    @XmlRootElement(name = "metadata")
    public static class Metadata
    {
        String headCap;
        int    headAxis  = 1;
        int    headAxis2 = 2;
        int    headDir   = 1;
        String head;
    }
}
