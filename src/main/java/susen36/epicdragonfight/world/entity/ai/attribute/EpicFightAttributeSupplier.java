package susen36.epicdragonfight.world.entity.ai.attribute;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;

public class EpicFightAttributeSupplier extends AttributeSupplier {
	private static Map<Attribute, AttributeInstance> putEpicFightAttributes(Map<Attribute, AttributeInstance> originalMap) {
		Map<Attribute, AttributeInstance> newMap = Maps.newHashMap();
		
		AttributeSupplier supplier = AttributeSupplier.builder()
				.add(Attributes.ATTACK_DAMAGE)
			    .build();
		
		newMap.putAll(supplier.instances);
		newMap.putAll(originalMap);
		
		return ImmutableMap.copyOf(newMap);
	}
	
	public EpicFightAttributeSupplier(AttributeSupplier copy) {
		super(putEpicFightAttributes(copy.instances));
	}
}