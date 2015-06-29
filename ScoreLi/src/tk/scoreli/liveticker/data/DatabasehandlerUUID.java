package tk.scoreli.liveticker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * Hier wird die SqlLite Datenbank konfiguriert. Diese wird dann erstellt.
 * Hier sind auch alle Methoden vermerkt die für die Datenbankzugriffe
 * notwendig sind. Hierbei werden jeweils Sql zugriffe vollzogen. Hier
 * werden die Daten von dem angemeldeten User gespeichert. Damit es dann für
 * weitere Zwecke verwendet kann.
 * @author philipp
 */

public class DatabasehandlerUUID extends SQLiteOpenHelper {
		// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "UUID.db";

	// Contacts table name
	private static final String TABLE_MITGLIEDER = "UUID";

	// Contacts Table Columns names
	private static final String MITGLIEDER_ID = "id";
	private static final String MITGLIEDER_EMAIL = "UUID";

	public DatabasehandlerUUID(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Mit dieser Methode wird die Sql-lite Datenbank gebaut bzw. die Tabelle.
	 * Hierbei wird ein Sql-Befehl erzeugt und dieser dann ausgeführt.(execSQL)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_MITGLIEDER + "("
				+ MITGLIEDER_ID + " INTEGER PRIMARY KEY," + MITGLIEDER_EMAIL
				+ " TEXT" + ")";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MITGLIEDER);

		// Create tables again
		onCreate(db);
	}

	/**
	 * Hier wird das Mitglied in der Datenbank eingspeichert.
	 * 
	 * @param mitglied
	 */
	public void addMitglied(Mitglied mitglied) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(MITGLIEDER_EMAIL, mitglied.getUuid()); // UUID

		// Inserting Row
		db.insert(TABLE_MITGLIEDER, null, values);
		db.close(); // Closing database connection
	}

	/**
	 * Hier wird das Mitglied mit hilfe einer SQL-Abfrage aus der Datenbank geholt und das objekt wird
	 * zurückggeben.
	 * 
	 * @return mitglied
	 */
	public Mitglied getMitglied() {
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_MITGLIEDER;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		cursor.moveToFirst();
		Mitglied mitglied = new Mitglied();
		mitglied.set_id(Integer.parseInt(cursor.getString(0)));
		mitglied.setUuid(cursor.getString(1));
		db.close(); // Closing database connection
		return mitglied;

	}

	/**
	 * Hier wird ein Mitglied mit hilfe einer SQL-Abfrage gelöscht. Der
	 * Eintrag/Mitglied wird mit der ID-Referenziert.
	 * 
	 * @param mitglied
	 */
	public void deleteMitglied(Mitglied mitglied) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_MITGLIEDER, MITGLIEDER_ID + " = ?",
				new String[] { String.valueOf(mitglied.get_id()) });
		db.close();
	}
/**
 * Mit dieser Methode werden alle Einträger der Datenbank gelöscht.
 */
	public void deleteUsers() {
		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete(TABLE_MITGLIEDER, null, null);
		db.close();

	}
}
