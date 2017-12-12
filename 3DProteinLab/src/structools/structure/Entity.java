package structools.structure;

/*
 * THIS WILL BE REMOVED AND REPLACED WITH CLASSES FOR SPECIFIC ENTITY TYPES
 * E.G. PROTEIN, DNA, RNA
 */

public class Entity {
	private String pdbId;
	private Integer entityId;
	private String type;
	private String description;
	
	public Entity() {
	}
	
	public Entity(String pdbId, Integer entityId, String type, String description) {
		this.pdbId = pdbId;
		this.entityId = entityId;
		this.type = type;
		this.description = description;
	}

	public String getPdbId() {
		return pdbId;
	}
	public void setPdbId(String pdbId) {
		this.pdbId = pdbId;
	}
	public Integer getEntityId() {
		return entityId;
	}
	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
