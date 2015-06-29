package tk.scoreli.liveticker.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * Hier wird die SqlLite Datenbank konfiguriert. Diese wird dann erstellt.
 * Hier sind auch alle Methoden vermerkt die für die Datenbankzugriffe
 * notwendig sind. Hierbei werden jeweils Sql zugriffe vollzogen. In diese
 * Datenbank werden die Spiele gespeichert.
 * @author philipp
 */
public class DatabasehandlerSpiele extends SQLiteOpenHelper {
	
	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "Spiele.db";

	// Contacts table name
	private static final String TABLE_Veranstaltungen = "veranstaltungen";

	// Contacts Table Columns names
	private static final String Veranstaltung_ID = "id";
	private static final String Veranstaltung_Sportart = "Sportart";
	private static final String Veranstaltung_Heimmanschaft = "Heimmanschaft";
	private static final String Veranstaltung_Gastmanschaft = "Gastmannschaft";
	private static final String Veranstaltung_SpielstandHeim = "SpielstandHeim";
	private static final String Veranstaltung_SpielstandGast = "SpielstandGast";
	private static final String Veranstaltung_Spielbeginn = "Spielbeginn";
	private static final String Veranstaltung_Status = "Status";

	public DatabasehandlerSpiele(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Mit dieser Methode wird die Sql-lite Datenbank gebaut bzw. die Tabelle. Hierbei wird ein
	 * Sql-Befehl erzeugt und dieser dann ausgeführt.(execSQL)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_Veranstaltung_TABLE = "CREATE TABLE "
				+ TABLE_Veranstaltungen + "(" + Veranstaltung_ID
				+ " INTEGER PRIMARY KEY," + Veranstaltung_Sportart + " TEXT,"
				+ Veranstaltung_Heimmanschaft + " TEXT, "
				+ Veranstaltung_Gastmanschaft + " TEXT, "
				+ Veranstaltung_SpielstandHeim + " INTEGER, "
				+ Veranstaltung_SpielstandGast + " INTEGER, "
				+ Veranstaltung_Spielbeginn + " TEXT, " + Veranstaltung_Status
				+ " TEXT" + ")";
		db.execSQL(CREATE_Veranstaltung_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_Veranstaltungen);

		// Create tables again
		onCreate(db);
	}

	/**
	 * Hier wird eine neuer Eintrag erstellt. Hierbei muss ein
	 * Veranstaltungsobjekt erzeugt werden.
	 *
	 * @param veranstaltung
	 */
	public void addVeranstaltung(Veranstaltung veranstaltung) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Veranstaltung_ID, veranstaltung.getId());
		values.put(Veranstaltung_Sportart, veranstaltung.getSportart()); // Sportart
		values.put(Veranstaltung_Heimmanschaft,
				veranstaltung.getHeimmanschaft()); // Heimmannschaft
		values.put(Veranstaltung_Gastmanschaft,
				veranstaltung.getGastmannschaft()); // Gastmannschaft
		values.put(Veranstaltung_SpielstandHeim,
				veranstaltung.getSpielstandHeim()); // SpielstandHeim
		values.put(Veranstaltung_SpielstandGast,
				veranstaltung.getSpielstandGast()); // SpielstandGast
		values.put(Veranstaltung_Spielbeginn, veranstaltung.getSpielbeginn());// Spielbeginn
		values.put(Veranstaltung_Status, veranstaltung.getStatus());// Status
		// Inserting Row
		db.insert(TABLE_Veranstaltungen, null, values);
		db.close(); // Closing database connection
	}

	/**
	 * Hierbei wird ein ein Veranstaltungseintrag über die id der Veranstaltung
	 * geholt. Danach wird dieses als Veranstaltungsobjekt zurückgegeben.
	 * 
	 * @param id
	 * @return
	 */
	// Getting single Veranstaltung
	public Veranstaltung getVeranstaltung(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_Veranstaltungen, new String[] {
				Veranstaltung_ID, Veranstaltung_Sportart,
				Veranstaltung_Heimmanschaft, Veranstaltung_Gastmanschaft,
				Veranstaltung_SpielstandHeim, Veranstaltung_SpielstandGast,
				Veranstaltung_Spielbeginn, Veranstaltung_Status },
				Veranstaltung_ID + "=?", new String[] { String.valueOf(id) },
				null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		Veranstaltung veranstaltung = new Veranstaltung(Integer.parseInt(cursor
				.getString(0)), cursor.getString(1), cursor.getString(2),
				cursor.getString(3), Integer.parseInt(cursor.getString(4)),
				Integer.parseInt(cursor.getString(5)), cursor.getString(6),
				cursor.getString(7));
		db.close(); // Closing database connection
		// return contact

		return veranstaltung;
	}

	/**
	 * Bei dieser Methode wird mit einer SQL-Abfrage alle Veranstaltungseinträge
	 * geholt. Diese werden in einer List<> zurückgegeben.
	 * 
	 * @return
	 */

	public List<Veranstaltung> getAllVeranstaltungen() {
		List<Veranstaltung> veranstaltungliste = new ArrayList<Veranstaltung>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_Veranstaltungen;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Veranstaltung veranstaltung = new Veranstaltung(
						Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), cursor.getString(2),
						cursor.getString(3), Integer.parseInt(cursor
								.getString(4)), Integer.parseInt(cursor
								.getString(5)), cursor.getString(6),
						cursor.getString(7));

				// Adding contact to list
				veranstaltungliste.add(veranstaltung);
			} while (cursor.moveToNext());
		}
		db.close(); // Closing database connection
		// return contact list
		return veranstaltungliste;
	}

	/**
	 * Bei dieser Methode wird mit einer SQL-Abfrage alle Veranstaltungseinträge
	 * geholt die der Sportart Tischtennis angehören. Diese werden in einer
	 * List<> zurückgegeben.
	 * 
	 * @return
	 */
	public List<Veranstaltung> getTischtennisVeranstaltungen() {
		List<Veranstaltung> veranstaltungliste = new ArrayList<Veranstaltung>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_Veranstaltungen
				+ " WHERE " + Veranstaltung_Sportart + " LIKE 'Tischtennis'";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Veranstaltung veranstaltung = new Veranstaltung(
						Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), cursor.getString(2),
						cursor.getString(3), Integer.parseInt(cursor
								.getString(4)), Integer.parseInt(cursor
								.getString(5)), cursor.getString(6),
						cursor.getString(7));

				// Adding contact to list
				veranstaltungliste.add(veranstaltung);
			} while (cursor.moveToNext());
		}
		db.close(); // Closing database connection
		// return contact list
		return veranstaltungliste;
	}

	/**
	 * Bei dieser Methode wird mit einer SQL-Abfrage alle Veranstaltungseinträge
	 * geholt die der Sportart Fußball angehören. Diese werden in einer List<>
	 * zurückgegeben.
	 * 
	 * @return
	 */

	public List<Veranstaltung> getFussballVeranstaltungen() {
		List<Veranstaltung> veranstaltungliste = new ArrayList<Veranstaltung>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_Veranstaltungen
				+ " WHERE " + Veranstaltung_Sportart + " LIKE 'Fussball'";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Veranstaltung veranstaltung = new Veranstaltung(
						Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), cursor.getString(2),
						cursor.getString(3), Integer.parseInt(cursor
								.getString(4)), Integer.parseInt(cursor
								.getString(5)), cursor.getString(6),
						cursor.getString(7));

				// Adding contact to list
				veranstaltungliste.add(veranstaltung);
			} while (cursor.moveToNext());
		}
		db.close(); // Closing database connection
		// return contact list
		return veranstaltungliste;
	}

	/**
	 * Bei dieser Methode wird mit einer SQL-Abfrage alle Veranstaltungseinträge
	 * geholt die der Sportart Handball angehören. Diese werden in einer List<>
	 * zurückgegeben.
	 * 
	 * @return
	 */
	public List<Veranstaltung> getHandballVeranstaltungen() {
		List<Veranstaltung> veranstaltungliste = new ArrayList<Veranstaltung>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_Veranstaltungen
				+ " WHERE " + Veranstaltung_Sportart + " LIKE 'Handball'";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Veranstaltung veranstaltung = new Veranstaltung(
						Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), cursor.getString(2),
						cursor.getString(3), Integer.parseInt(cursor
								.getString(4)), Integer.parseInt(cursor
								.getString(5)), cursor.getString(6),
						cursor.getString(7));

				// Adding contact to list
				veranstaltungliste.add(veranstaltung);
			} while (cursor.moveToNext());
		}
		db.close(); // Closing database connection
		// return contact list
		return veranstaltungliste;
	}

	/**
	 * Bei dieser Methode wird mit einer SQL-Abfrage alle Veranstaltungseinträge
	 * geholt die der Sportart Volleyball angehören. Diese werden in einer
	 * List<> zurückgegeben.
	 * 
	 * @return
	 */
	public List<Veranstaltung> getVolleyballVeranstaltungen() {
		List<Veranstaltung> veranstaltungliste = new ArrayList<Veranstaltung>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_Veranstaltungen
				+ " WHERE " + Veranstaltung_Sportart + " LIKE 'Volleyball'";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Veranstaltung veranstaltung = new Veranstaltung(
						Integer.parseInt(cursor.getString(0)),
						cursor.getString(1), cursor.getString(2),
						cursor.getString(3), Integer.parseInt(cursor
								.getString(4)), Integer.parseInt(cursor
								.getString(5)), cursor.getString(6),
						cursor.getString(7));

				// Adding contact to list
				veranstaltungliste.add(veranstaltung);
			} while (cursor.moveToNext());
		}
		db.close(); // Closing database connection
		// return contact list
		return veranstaltungliste;
	}

	/**
	 * Bei dieser Methode wird mit einer SQL-Abfrage die Veranstaltung
	 * geupdatet. Diese wird mit der ID- Referenziert.
	 * 
	 * @param veranstaltung
	 * @return
	 */
	public int updateVeranstaltung(Veranstaltung veranstaltung) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Veranstaltung_ID, veranstaltung.getId());
		values.put(Veranstaltung_Sportart, veranstaltung.getSportart()); // Sportart
		values.put(Veranstaltung_Heimmanschaft,
				veranstaltung.getHeimmanschaft()); // Heimmannschaft
		values.put(Veranstaltung_Gastmanschaft,
				veranstaltung.getGastmannschaft()); // Gastmannschaft
		values.put(Veranstaltung_SpielstandHeim,
				veranstaltung.getSpielstandHeim()); // SpielstandHeim
		values.put(Veranstaltung_SpielstandGast,
				veranstaltung.getSpielstandGast()); // SpielstandGast
		values.put(Veranstaltung_Spielbeginn, veranstaltung.getSpielbeginn());// Spielbeginn
		values.put(Veranstaltung_Status, veranstaltung.getStatus());// Status
		db.update(TABLE_Veranstaltungen, values, Veranstaltung_ID + " = ?",
				new String[] { String.valueOf(veranstaltung.getId()) });
		// updating row
		db.close(); // Closing database connection
		return 1;

	}

	/**
	 * Bei dieser Methode wird eine Veranstaltung mit einer SQL-Abfrage
	 * gelöscht. Hierbei wird sie auch über die ID-Referenziert.
	 * 
	 * @param veranstaltung
	 */
	public void deleteVeranstaltung(Veranstaltung veranstaltung) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_Veranstaltungen, Veranstaltung_ID + " = ?",
				new String[] { String.valueOf(veranstaltung.getId()) });
		db.close();
	}

	/**
	 * Hier werden alle Veranstaltungseinträge gezählt.
	 * @return
	 */
	public int getVeranstaltungCount() {
		String countQuery = "SELECT  * FROM " + TABLE_Veranstaltungen;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		// return count
		return cursor.getCount();
	}

	/**
	 * Hier wird die ganze Datenbank gelöscht.
	 */
	public void deleteVeranstaltungen() {
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_Veranstaltungen, null, null);
		db.close();
	}
}