package sorazodia.survival.asm;

import static org.objectweb.asm.Opcodes.*;
import static sorazodia.survival.asm.Descriptor.*;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import sorazodia.survival.asm.main.SurvivalTweaksCore;
import sorazodia.survival.asm.patch.ItemBlockInteraction;

import com.sun.xml.internal.ws.org.objectweb.asm.Type;

public class ItemUseTranformer
{
	//net.minecraft.server.management.ItemInWorldManager
	//mx
	//activateBlockOrUseItem(EntityPlayer, World , ItemStack , int , int , int , int , float, float , float )
	//a 
	//(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIIIFFF)Z
	//(Lyz;Lahb;Ladd;IIIIFFF)Z
	//(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIIIFFF)Z
	public static void tranformItemManager(ClassNode itemManagerClass, boolean isObfuscated)
	{
		final String USE_ITEM = isObfuscated ? "a" : "activateBlockOrUseItem";
		final String USE_ITEM_DESC = String.format("(%s;%s;%s;%s%s%s%s%s%s%s)Z", ENTITY_PLAYER.getDesc(isObfuscated), WORLD.getDesc(isObfuscated), ITEMSTACK.getDesc(isObfuscated), INTS.getDesc(), INTS.getDesc(), INTS.getDesc(), INTS.getDesc(), FLOATS.getDesc(), FLOATS.getDesc(), FLOATS.getDesc());

		System.out.println(USE_ITEM_DESC);
		
		for (MethodNode method : itemManagerClass.methods)
		{
			if (method.name.equals(USE_ITEM) && method.desc.equals(USE_ITEM_DESC))
			{
				AbstractInsnNode targetNode = null;
				for (AbstractInsnNode instructions : method.instructions.toArray())
				{
					//mv.visitVarInsn(ISTORE, 15);
					//mv.visitJumpInsn(GOTO, l16);
					if (instructions.getOpcode() == ILOAD && ((VarInsnNode) instructions).var == 15)
					{
							targetNode = instructions;
							break;
					}
				}

				if (targetNode != null)
				{
					InsnList insertList = new InsnList();
					final String INSERT_DESC = String.format("(%s;%s;%s;%s%s%s%s%s)V",
							WORLD.getDesc(isObfuscated), ENTITY_PLAYER.getDesc(isObfuscated), ITEMSTACK.getDesc(isObfuscated),
							INTS.getDesc(), INTS.getDesc(), INTS.getDesc(), INTS.getDesc(), BOOLEANS.getDesc());
					
					//ALOAD, 2 World
					//ALOAD, 1 EntityPlayer
					//ALOAD, 3 ItemStack
					//ILOAD, 4 x
					//ILOAD, 5 y
					//ILOAD, 6 z
					//ILOAD, 7 side
					//ISTORE, 15 blockActivated
					
					insertList.add(new VarInsnNode(ALOAD, 2));
					insertList.add(new VarInsnNode(ALOAD, 1));
					insertList.add(new VarInsnNode(ALOAD, 3));
					insertList.add(new VarInsnNode(ILOAD, 4));
					insertList.add(new VarInsnNode(ILOAD, 5));
					insertList.add(new VarInsnNode(ILOAD, 6));
					insertList.add(new VarInsnNode(ILOAD, 7));
					insertList.add(new VarInsnNode(ILOAD, 15));
					insertList.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(ItemBlockInteraction.class), "placeBlocks", INSERT_DESC, false));
					
					method.instructions.insert(targetNode, insertList);
					
					SurvivalTweaksCore.getLogger().info("ItemInWorldManager Tranformed");
				} else
				{
					SurvivalTweaksCore.getLogger().error("ItemInWorldManager was not a Tranformer");
				}
			}
		}
	}
}
