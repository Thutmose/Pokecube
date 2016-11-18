package pokecube.compat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CompatClass
{
    public static enum Phase
    {
        PRE, INIT, POST, POSTPOST;
    }

    Phase phase() default Phase.INIT;

    boolean takesEvent() default false;
}
