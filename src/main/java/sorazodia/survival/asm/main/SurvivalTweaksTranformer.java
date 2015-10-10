package sorazodia.survival.asm.main;

import java.util.Arrays;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import sorazodia.survival.asm.CompassTextureTranformer;

public class SurvivalTweaksTranformer implements IClassTransformer
{
	private static final String[] CLASS_TO_TRANSFORM = {
			"net.minecraft.client.renderer.texture.TextureCompass", "bqm"};

	@Override
	public byte[] transform(String name, String transformedName, byte[] tranformedClass)
	{
		int index = Arrays.asList(CLASS_TO_TRANSFORM).indexOf(transformedName);
		return index != -1 ? transform(index, tranformedClass, transformedName,
				!name.equals(transformedName)) : tranformedClass;
	}

	private byte[] transform(int index, byte[] tranformedClass, String transformedName, boolean isObfuscated)
	{
		SurvivalTweaksCore.getLogger().info("Transforming:" + CLASS_TO_TRANSFORM[index]);
		try
		{
			ClassNode node = new ClassNode();
			ClassReader reader = new ClassReader(tranformedClass);

			reader.accept(node, 0);

			if (index == 0 || index == 1)
				CompassTextureTranformer.tranformCompassTextureClass(node, isObfuscated);

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);

			return writer.toByteArray();

		} catch (Exception exception)
		{
			SurvivalTweaksCore.getLogger().error("Unable to save tranformed bytecode");
			exception.printStackTrace();
		}

		return tranformedClass;
	}

}
