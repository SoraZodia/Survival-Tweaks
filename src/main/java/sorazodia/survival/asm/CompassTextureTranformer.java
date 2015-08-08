package sorazodia.survival.asm;

import java.util.Arrays;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class CompassTextureTranformer implements IClassTransformer
{
	private static final String[] CLASSTOTRANSFORM = {
			"net.minecraft.client.renderer.texture.TextureCompass", "bqm" };

	@Override
	public byte[] transform(String name, String transformedName, byte[] tranformedClass)
	{
		int index = Arrays.asList(CLASSTOTRANSFORM).indexOf(transformedName);
		return index != -1 ? transform(index, tranformedClass, !name.equals(transformedName)) : tranformedClass;
	}

	private static byte[] transform(int index, byte[] tranformedClass, boolean isObfuscated)
	{
		System.out.println("[Survival Tweaks Core] Transforming:" + CLASSTOTRANSFORM);
		try
		{
			ClassNode node = new ClassNode();
			ClassReader reader = new ClassReader(tranformedClass);

			reader.accept(node, 0);

			if (index == 0 || index == 1)
				tranformCompassTextureClass(node, isObfuscated);

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);
			return writer.toByteArray();
		} catch (Exception exception)
		{
			exception.printStackTrace();
		}

		return tranformedClass;
	}

	private static void tranformCompassTextureClass(ClassNode compassTextureClass, boolean isObfuscated)
	{
		final String UPDATE_COMPASS = isObfuscated ? "a" : "updateCompass";
		final String UPDATE_COMPASS_DESC = isObfuscated ? "(Lahb;DDDZZ)V" : "(Lnet/minecraft/world/World;DDDZZ)V";
		
		for (MethodNode methods : compassTextureClass.methods)
		{
			if (methods.name.equals(UPDATE_COMPASS) && methods.desc.equals(UPDATE_COMPASS_DESC)) 
			{
				
			}
		}

	}

}
