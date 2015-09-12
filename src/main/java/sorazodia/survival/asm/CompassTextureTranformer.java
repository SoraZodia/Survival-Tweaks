package sorazodia.survival.asm;

import static org.objectweb.asm.Opcodes.*;
import static sorazodia.survival.asm.Descriptor.*;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import sorazodia.survival.asm.main.SurvivalTweaksCore;
import sorazodia.survival.asm.patch.CompassStructureDetection;

public class CompassTextureTranformer 
{

	public static void tranformCompassTextureClass(ClassNode compassTextureClass, boolean isObfuscated)
	{
		final String UPDATE_COMPASS = isObfuscated ? "a" : "updateCompass";
		final String UPDATE_COMPASS_DESC = String.format("(%s;DDDZZ)V", WORLD.getDesc(isObfuscated));

		for (MethodNode method : compassTextureClass.methods)
		{
			if (method.name.equals(UPDATE_COMPASS) && method.desc.equals(UPDATE_COMPASS_DESC))
			{
				AbstractInsnNode targetNode = null;
				for (AbstractInsnNode instruction : method.instructions.toArray())
				{
					if (instruction.getOpcode() == DSTORE)
					{
						if (((VarInsnNode) instruction).var == 10 && instruction.getPrevious().getOpcode() == DMUL)
						{
							targetNode = instruction.getNext();
							break;
						}
					}
				}

				if (targetNode != null)
				{
					//Order
					//ALOAD, 1 - world instance
					//DLOAD, 2 - double x pos of player
					//DLOAD, 4 - double z pos of player
					//DLOAD, 10 - the compass needle angle
					//DLOAD, 6 - double angle
					//ILOAD, 8 - a boolean value
					
					InsnList insnList = new InsnList();
					final String DESC = String.format("(%s;%s%s%s%s%s)D", WORLD.getDesc(isObfuscated), DOUBLES.getDesc(), DOUBLES.getDesc(), DOUBLES.getDesc(), DOUBLES.getDesc(), BOOLEANS.getDesc());

					insnList.add(new VarInsnNode(ALOAD, 1));
					insnList.add(new VarInsnNode(DLOAD, 2));
					insnList.add(new VarInsnNode(DLOAD, 4));
					insnList.add(new VarInsnNode(DLOAD, 10));
					insnList.add(new VarInsnNode(DLOAD, 6));
					insnList.add(new VarInsnNode(ILOAD, 8));
					insnList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(CompassStructureDetection.class), "findStructure", DESC, false));
					insnList.add(new VarInsnNode(DSTORE, 10));

					method.instructions.insert(targetNode, insnList);

					SurvivalTweaksCore.getLogger().info("TextureCompass Tranformed");
				} else
				{
					SurvivalTweaksCore.getLogger().error("TextureCompass was not a Tranformer");
				}

			}
		}

	}

}
