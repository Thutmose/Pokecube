# Pokécube
Pokémobs for Minecraft - page under construction.

For the List of missing models, please see https://github.com/Thutmose/Pokecube-Models

##How to setup devopment environment for Addons in eclipse

Step 1

Setup a forge environment in Addons, as well as one in Core, you can do this by placing the relevant gradle files from the forge MDK in the directory, then running the appropriate batch file, or running the gradle command for setup manually.

Step2

Make an eclipse project in Core, then via properties -> Java Build Path, link the source folders for Addons to the project.
the compat package in Addons with display several errors, these can usually be ignored, but if you want them to go away, import the contents of the Addons libs directory to the project, as well as thaumcraft, which should be somewhere in your gradle stuff.

Step 3

To run/test, do everything via the project made in core.  To build, build from the Addons directory.

##Related/Contained projects

Contains a modifed version of [NBTEdit](https://github.com/DavidGoldman/NBTEdit) which is currently used for debugging, will probably be removed eventually as well.

[Tabula Model loading code](https://github.com/Thutmose/Pokecube/tree/master/Pokecube%20Core/src/main/java/pokecube/modelloader/client/tabula) is a modified version of the one used for [Showcase](https://github.com/iLexiconn/Showcase), If the one in there, or [LLibrary](https://github.com/iLexiconn/LLibrary) gets the same functionailty, I will probably remove it and use theirs instead, adding them as a dependancy for Pokécube.

Contains JEP by Nathan Funk and Richard Morris for use with spawn logic, lisence [here](https://github.com/Thutmose/Pokecube/blob/master/Pokecube%20Core/src/main/java/org/nfunk/jep/license.txt)
