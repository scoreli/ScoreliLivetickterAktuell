package tk.scoreli.liveticker.data;

/**
 * Hier werden die Eigenschaften für das Objekt Mitglied vorgegeben. Dieses
 * Objekt ist für den eingeloggten Zustand wichtig, da es dort die uuid
 * speichert und mit dieser dann die Datenbank zugriffe betätigt.
 * 
 * @author philipp
 *
 */
public class Mitglied {

	long _id;
	String uuid;

	public Mitglied() {
	}

	public Mitglied(String uuid) {
		super();
		this.uuid = uuid;
	}

	public Mitglied(long _id, String uuid) {
		super();
		this._id = _id;
		this.uuid = uuid;
	}

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}