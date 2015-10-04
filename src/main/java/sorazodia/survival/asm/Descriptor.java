package sorazodia.survival.asm;


public enum Descriptor
{	
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
