package tk.scoreli.liveticker;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tk.scoreli.liveticker.data.DatabasehandlerSpiele;
import tk.scoreli.liveticker.data.DatabasehandlerUUID;
import tk.scoreli.liveticker.data.Mitglied;
import tk.scoreli.liveticker.data.Veranstaltung;
import tk.scoreli.liveticker.internet.InternetService;
import tk.scoreli.liveticker.loginregister.AppConfig;
import tk.scoreli.liveticker.loginregister.AppController;
import tk.scoreli.liveticker.loginregister.SessionManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Hier werden die Spiele des Users angezeigt. Durch Tippen auf eine
 * Veranstaltung gelangt man auf die ImSpielActivity.
 * 
 * @author philipp
 *
 */
public class SpieleDesUsersActivity extends Activity implements
		OnItemClickListener, Serializable {
	/**
	 * 
	 */

	private static final long serialVersionUID = 1L;

	private ListView Veranstaltungsliste;
	/**
	 * Datenbankanbindung die UUID ist wichtig damit nur die Heruntergeladen
	 * werden die dem Nutzer gehoeren.
	 */
	DatabasehandlerSpiele db = new DatabasehandlerSpiele(this);
	DatabasehandlerUUID dbuuid = new DatabasehandlerUUID(this);
	private SessionManager session;
	private InternetService internetService;
	public static final String KEY = "ID_Veranstaltung";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spiele);
		// Session manager
		session = new SessionManager(getApplicationContext());
		/**
		 * 
		 */
		internetService = new InternetService(this, this);
		/*
		 * Komischerweise führt Android automatisch die toString methode aus und
		 * gibt die Veranstaltung als String aus.
		 * http://app-makers.blogspot.de/2010
		 * /05/eine-listview-mit-inhalt-fullen.html
		 * http://www.appartig.net/?e=18
		 */
		db.deleteVeranstaltungen();
		if (session.isLoggedIn()) {
			Mitglied abfrage = dbuuid.getMitglied();
			internetService.VeranstaltungholenDesUsers(abfrage.getUuid());
		} else {
			Toast.makeText(getApplicationContext(),
					"Bitte einloggen um Veranstaltungen anzusehen",
					Toast.LENGTH_SHORT).show();
		}
		Veranstaltungzeigen();
		/*
		 * try { Veranstaltungsliste = (ListView)
		 * findViewById(R.id.listVeranstaltung); ListAdapter listenAdapter = new
		 * ArrayAdapter<Veranstaltung>(this,
		 * android.R.layout.simple_list_item_1, db.getAllVeranstaltungen());
		 * Veranstaltungsliste.setAdapter(listenAdapter);
		 * Veranstaltungsliste.setOnItemClickListener(this); //
		 * Veranstaltungsliste.setOnItemLongClickListener(this); } catch
		 * (Exception e) { Toast.makeText(getApplicationContext(), e.toString(),
		 * Toast.LENGTH_LONG).show();
		 * 
		 * }
		 */
	}

	
	@Override
	public synchronized void onResume() {
		super.onResume();
		// if(D) Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		Veranstaltungzeigen();
		/*
		 * try { Veranstaltungsliste = (ListView)
		 * findViewById(R.id.listVeranstaltung); ListAdapter listenAdapter = new
		 * ArrayAdapter<Veranstaltung>(this,
		 * android.R.layout.simple_list_item_1, db.getAllVeranstaltungen());
		 * Veranstaltungsliste.setAdapter(listenAdapter);
		 * Veranstaltungsliste.setOnItemClickListener(this); //
		 * Veranstaltungsliste.setOnItemLongClickListener(this); } catch
		 * (Exception e) { Toast.makeText(getApplicationContext(), e.toString(),
		 * Toast.LENGTH_LONG).show();
		 * 
		 * }
		 */
	}

	/**
	 * Hier werden alle Veranstaltungen geholt und dann auf der Liste
	 * ausgegeben. Aber hier werden nur die Veranstaltungen des Users geholt, da
	 * die Datenbank gelöscht wird und dann nur mit den Veranstaltungen des
	 * Users beschrieben wird.
	 */
	public void Veranstaltungzeigen() {
		try {
			Veranstaltungsliste = (ListView) findViewById(R.id.listVeranstaltung);
			ListAdapter listenAdapter = new ArrayAdapter<Veranstaltung>(this,
					android.R.layout.simple_list_item_1,
					db.getAllVeranstaltungen());
			Veranstaltungsliste.setAdapter(listenAdapter);
			Veranstaltungsliste.setOnItemClickListener(this);
			// Veranstaltungsliste.setOnItemLongClickListener(this);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.toString(),
					Toast.LENGTH_LONG).show();

		}

	}

	/*
	 * Hier fehlt noch das die Id von der Veranstaltung angezeigt wird.
	 */
	@Override
	public void onItemClick(AdapterView<?> lV, View view, int pos, long id) {
		// Hier wird das Objekt geholt der Liste und unten die Id geholt
		Veranstaltung veranstaltung = (Veranstaltung) Veranstaltungsliste
				.getItemAtPosition(pos);
		Intent i = new Intent(SpieleDesUsersActivity.this,
				ImSpielActivity.class);
		i.putExtra(KEY, veranstaltung.getId());
		startActivity(i);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.spiele, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		/**
		 * Hier gelangt man zu dr Activity wo eine Neue Veranstaltung erstellt
		 * werden kann.
		 */
		if (id == R.id.spiel_neueVeranstaltung) {
			startActivity(new Intent(SpieleDesUsersActivity.this,
					NeuesSpielActivity.class));

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}