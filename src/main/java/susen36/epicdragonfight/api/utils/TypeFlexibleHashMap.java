package susen36.epicdragonfight.api.utils;

import susen36.epicdragonfight.api.utils.TypeFlexibleHashMap.TypeKey;

import java.util.HashMap;

public class TypeFlexibleHashMap<A extends TypeKey> extends HashMap<A, Object> {
	
	@SuppressWarnings("unchecked")
	public <T> T put(TypeKey typeKey, T val) {
		return (T)super.put((A) typeKey, val);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(TypeKey typeKey) {
		return (T)super.get(typeKey);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getOrDefault(TypeKey typeKey, T defaultVal) {
		return (T)super.getOrDefault(typeKey, defaultVal);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public interface TypeKey {
	}
}