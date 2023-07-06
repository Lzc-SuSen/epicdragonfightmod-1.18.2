package susen36.epicdragonfight.gameasset;

import susen36.epicdragonfight.api.collider.Collider;
import susen36.epicdragonfight.api.collider.MultiOBBCollider;
import susen36.epicdragonfight.api.collider.OBBCollider;

public class ColliderPreset {
	public static final Collider DRAGON_BODY = new OBBCollider(2.0D, 1.5D, 4.0D, 0.0D, 1.5D, -0.5D);
	public static final Collider DRAGON_LEG = new MultiOBBCollider(3, 0.8D, 1.6D, 0.8D, 0.0D, -0.6D, 0.7D);
	public static final Collider DRAGON_FIST = new MultiOBBCollider(3, 0.4D, 0.4D, 0.4D, 0D, 0D, 0D);
}