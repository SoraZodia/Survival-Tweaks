package sorazodia.survival.asm;

import org.objectweb.asm.Type;

public enum Descriptor
{

	INTS(Type.getDescriptor(int.class)),
	FLOATS(Type.getDescriptor(float.class)),
	DOUBLES(Type.getDescriptor(double.class)),
	BOOLEANS(Type.getDescriptor(boolean.class)),
	
	WORLD("Lnet/minecraft/world/World", "Lahb"),
	ENTITY_PLAYER("Lnet/minecraft/entity/player/EntityPlayer", "Lyz"),
	ITEMSTACK("Lnet/minecraft/item/ItemStack", "Ladd");
	
	private final String DESC;
	private final String OBFDESC; //For Minecraft classes
	
	Descriptor(String desc)
	{
		this(desc, "");
	}
	
	Descriptor(String deobfDesc, String obfDesc)
	{
		DESC = deobfDesc;
		OBFDESC = obfDesc;
	}
	
	public String getDesc()
	{
		return DESC;
	}
	
	public String getDesc(boolean isObfuscated)
	{
		return isObfuscated ? OBFDESC : DESC;
	}

}
