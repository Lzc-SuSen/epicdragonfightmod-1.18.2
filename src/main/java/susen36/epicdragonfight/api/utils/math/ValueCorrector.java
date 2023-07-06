package susen36.epicdragonfight.api.utils.math;

public class ValueCorrector {
	private float adders;
	private float multipliers;
	private float setters;
	
	public ValueCorrector(float adder, float multiplier, float setter) {
		this.adders = adder;
		this.multipliers = multiplier;
		this.setters = setter;
	}

	
	public float getTotalValue(float value) {
		return this.setters == 0 ? (value * this.multipliers) + this.adders : this.setters;
	}
	
	@Override
	public String toString() {
		return this.setters == 0
				? String.format("%.0f%%", this.multipliers * 100.0F) + (this.adders == 0 ? "" : String.format(" + %.1f", this.adders))
				: String.format("%.0f", this.setters);
	}
}