package tk.scoreli.liveticker.data;

import java.io.Serializable;

/**
 * Bei dieser Klasse muss das Interface Serializable implementiert werden, damit
 * es für die Bluetoothverbindung serilaisiert werden kann. Hierbei muss noch
 * eine serialVersionUID angegeben werden. Dieses Objekt wird für die Erzeugung
 * und Anzeige der Spielstände benutzt.
 * 
 * @author philipp
 *
 */
public class Veranstaltung implements Serializable {

	// private static final long serialVersionUID = 1L;
	private String Heimmanschaft, Gastmannschaft, Spielbeginn, Sportart,
			Status;
	private long _id;
	private int SpielstandHeim, SpielstandGast;

	public Veranstaltung() {
	}

	/*
	 * Status fehlt noch
	 */
	public Veranstaltung(String heimmanschaft, String gastmannschaft,
			String spielbeginn, String sportart, int spielstandHeim,
			int spielstandGast, String status) {
		super();
		Heimmanschaft = heimmanschaft;
		Gastmannschaft = gastmannschaft;
		Spielbeginn = spielbeginn;
		Sportart = sportart;
		SpielstandHeim = spielstandHeim;
		SpielstandGast = spielstandGast;
		Status = status;
	}

	public Veranstaltung(long id, String sportart, String heimmanschaft,
			String gastmannschaft, int spielstandHeim, int spielstandGast,
			String spielbeginn, String status) {
		super();
		Heimmanschaft = heimmanschaft;
		Gastmannschaft = gastmannschaft;
		Spielbeginn = spielbeginn;
		Sportart = sportart;
		this._id = id;
		SpielstandHeim = spielstandHeim;
		SpielstandGast = spielstandGast;
		Status = status;
	}

	public String getHeimmanschaft() {
		return Heimmanschaft;
	}

	public void setHeimmanschaft(String heimmanschaft) {
		Heimmanschaft = heimmanschaft;
	}

	public String getGastmannschaft() {
		return Gastmannschaft;
	}

	public void setGastmannschaft(String gastmannschaft) {
		Gastmannschaft = gastmannschaft;
	}

	public String getSpielbeginn() {
		return Spielbeginn;
	}

	public void setSpielbeginn(String spielbeginn) {
		Spielbeginn = spielbeginn;
	}

	public String getSportart() {
		return Sportart;
	}

	public void setSportart(String sportart) {
		Sportart = sportart;
	}

	public long getId() {
		return _id;
	}

	public void setId(long id) {
		this._id = id;
	}

	public int getSpielstandHeim() {
		return SpielstandHeim;
	}

	public void setSpielstandHeim(int spielstandHeim) {
		SpielstandHeim = spielstandHeim;
	}

	public int getSpielstandGast() {
		return SpielstandGast;
	}

	public void setSpielstandGast(int spielstandGast) {
		SpielstandGast = spielstandGast;
	}

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	@Override
	public String toString() {
		return "" + Heimmanschaft + " gegen " + Gastmannschaft + " "
				+ SpielstandHeim + " : " + SpielstandGast + " Spielbeginn: "
				+ Spielbeginn + " Uhr " + " Sportart: " + Sportart
				+ " Status: " + Status;
	}

}