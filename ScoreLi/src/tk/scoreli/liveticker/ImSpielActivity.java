package tk.scoreli.liveticker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import tk.scoreli.liveticker.bluetooth.BluetoothService;
import tk.scoreli.liveticker.bluetooth.GeräteSuchen;
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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Diese Activity regelt die eingaben des Benutzers während eines laufenden
 * Livespieles. Von dort aus kann man die Veranstaltung zur AnzeigenActivity
 * schicken und die Spielstände und den Status anpassen.
 * 
 * @author philipp
 *
 */
public class ImSpielActivity extends Activity implements
		OnCheckedChangeListener {
	/**
	 * Werte für die initalisierung von Im Spiel
	 * 
	 */
	private EditText txfSpielstandHeim, txfSpielstandGast, txfStatus;
	private TextView statusScoreboard;
	private Button btnaktualisieren, btnloeschen, btnheimplusein,
			btngastpluseins, btnheimminuseins, btngastminuseins, btnzurueck;
	// private CheckBox checkboxbeenden;
	private Switch switch_scoreboard;
	/**
	 * Für die Aktualisierung der Spielstände ist eine Datenbankanbindung
	 * notwendig
	 * 
	 */
	DatabasehandlerSpiele db = new DatabasehandlerSpiele(this);
	DatabasehandlerUUID dbuuid = new DatabasehandlerUUID(this);
	/**
	 * Wird auf true gesetzt sobald eine erfolgreiche Verbindung zwischen zwei
	 * Geräten erfolgt ist.
	 */
	private boolean freigabe = false;
	// private ProgressDialog pDialog;

	private static final long serialVersionUID = 1L;
	/**
	 * Der Handler aus der Bluetoothservice Klasse sendet nur Zahlen
	 */
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	/**
	 * Diese Schlüsselnamen sind von dem Bluetooth Service Handler
	 */
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	public static final String KEY = "ID_Veranstaltung";
	private final static int REQUEST_ENABLE_BT = 2;
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothService mChatService = null;

	/**
	 * Für die Internet Klasse
	 * 
	 */
	private InternetService internetservice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_updatespiel);
		init();
		internetservice = new InternetService(this);
		/**
		 * Für den Switch von nöten.
		 */
		switch_scoreboard.setOnCheckedChangeListener(this);
		statusScoreboard.setText("Nicht Verbunden");
		// Progress dialog
		// pDialog = new ProgressDialog(this);
		/**
		 * Hier werden die Daten geholt die beim Aufrufen der Activity übergeben
		 * wurden. So bekommt man die aktuelle Veranstaltungsid heraus.
		 */
		long i = getIntent().getExtras().getLong(SpieleDesUsersActivity.KEY);
		Veranstaltung uebergabeveranstaltung = db.getVeranstaltung((int) i);
		//
		plusminusHeim(0, uebergabeveranstaltung);

		btnaktualisieren.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				aktualisieren();

			}
		});
		btnloeschen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				loeschen();
			}
		});
		btnheimplusein.setOnClickListener(new OnClickListener() {
			/**
			 * Wird der Button Heim plus eins gedrückt, wird nochmal die
			 * Veranstaltung geholt und dann mit der Methode plusminusEins
			 * aufgerufen und dann auf der internen Datenbank auch gleichzeitig
			 * aktualisiert.
			 */
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				long i = getIntent().getExtras().getLong(
						SpieleDesUsersActivity.KEY);
				Veranstaltung uebergabeveranstaltung = db
						.getVeranstaltung((int) i);
				plusminusHeim(1, uebergabeveranstaltung);
			}
		});
		btngastpluseins.setOnClickListener(new OnClickListener() {
			/**
			 * Wird der Button Gast plus eins gedrückt, wird nochmal die
			 * Veranstaltung geholt und dann mit der Methode plusminusEins
			 * aufgerufen und dann auf der internen Datenbank auch gleichzeitig
			 * aktualisiert.
			 */
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				long i = getIntent().getExtras().getLong(
						SpieleDesUsersActivity.KEY);
				Veranstaltung uebergabeveranstaltung = db
						.getVeranstaltung((int) i);
				plusminusGast(1, uebergabeveranstaltung);
			}
		});
		btnheimminuseins.setOnClickListener(new OnClickListener() {
			/**
			 * Wird der Button Heim minus eins gedrückt, wird nochmal die
			 * Veranstaltung geholt und dann mit der Methode plusminusEins
			 * aufgerufen und dann auf der internen Datenbank auch gleichzeitig
			 * aktualisiert.
			 */
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				long i = getIntent().getExtras().getLong(
						SpieleDesUsersActivity.KEY);
				Veranstaltung uebergabeveranstaltung = db
						.getVeranstaltung((int) i);
				plusminusHeim(-1, uebergabeveranstaltung);
			}
		});
		btngastminuseins.setOnClickListener(new OnClickListener() {
			/**
			 * Wird der Button Gast minus eins gedrückt, wird nochmal die
			 * Veranstaltung geholt und dann mit der Methode plusminusEins
			 * aufgerufen und dann auf der internen Datenbank auch gleichzeitig
			 * aktualisiert.
			 */
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				long i = getIntent().getExtras().getLong(
						SpieleDesUsersActivity.KEY);
				Veranstaltung uebergabeveranstaltung = db
						.getVeranstaltung((int) i);
				plusminusGast(-1, uebergabeveranstaltung);
			}
		});
		btnzurueck.setOnClickListener(new OnClickListener() {
			/**
			 * Wird der Button zurück gedrückt wird die Acitivty beendet und man
			 * gelangt auf die Veranstaltungen des Users.
			 */
			@Override
			public void onClick(View v) {
				finish();

			}
		});

	}

	/**
	 * Bei dieser Methode wird der Wert des Parameters i dem Spielstand der
	 * Heimmannschaft der zu bearbeitenden Mannschaft addiert. Außerdem
	 * aktualisiert die Methode noch den Spielstand den sie als Hint ausgibt.
	 * 
	 * @param i
	 * @param veranstaltung
	 */
	public void plusminusHeim(int i, Veranstaltung veranstaltung) {
		int uebergabe = veranstaltung.getSpielstandHeim();
		boolean freigabe = i > 0 || uebergabe > 0 && i < 1;
		if (freigabe) {
			uebergabe = uebergabe + i;
			veranstaltung.setSpielstandHeim(uebergabe);
		}
		txfSpielstandHeim.setHint("Heim: " + veranstaltung.getSpielstandHeim());
		txfSpielstandGast.setHint("Gast: " + veranstaltung.getSpielstandGast());
		try {
			db.updateVeranstaltung(veranstaltung);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Bei dieser Methode wird der Wert des Parameters i dem Spielstand der
	 * Gastmannschaft der zu bearbeitenden Mannschaft addiert. Außerdem
	 * aktualisiert die Methode noch den Spielstand den sie als Hint ausgibt.
	 * 
	 * @param i
	 * @param veranstaltung
	 */
	public void plusminusGast(int i, Veranstaltung veranstaltung) {
		int uebergabe = veranstaltung.getSpielstandGast();
		boolean freigabe = i > 0 || uebergabe > 0 && i < 1;
		if (freigabe) {
			uebergabe = uebergabe + i;
			veranstaltung.setSpielstandGast(uebergabe);
		}
		txfSpielstandHeim.setHint("Heim: " + veranstaltung.getSpielstandHeim());
		txfSpielstandGast.setHint("Gast: " + veranstaltung.getSpielstandGast());
		try {
			db.updateVeranstaltung(veranstaltung);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Diese Methode wird von dem System aufgerufen sobald der Zustand des
	 * Schalter/Switch für die Verbindung des Scoreboards geändert wurde.
	 * 
	 * @param buttonView
	 * @param isChecked
	 */
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			// Get local Bluetooth adapter
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			/**
			 * Ist kein Bluetoothadapter gefunden worden oder nicht unterstützt
			 * wird die Methode wieder beendet
			 */
			if (mBluetoothAdapter == null) {
				Toast.makeText(this, "Bluetooth ist nicht verfügbar",
						Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			/**
			 * Jetzt wird getestet ob Bluetooth schon aktiviert ist oder noch
			 * aktiviert werden muss. Ist Bluetooth schon aktiviert wird die
			 * setupUbertragung() Methode aufgerufen
			 */
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
				// Otherwise, setup the chat session
			} else {
				if (mChatService == null)
					setupUebertragung();
				/**
				 * Falls beim anmachen des Schalters Bluetooth schon an ist wird
				 * die Gerätesuche geöffnet.
				 */
				Intent serverIntent = new Intent(getApplicationContext(),
						GeräteSuchen.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}

		} else {
			// Stop the Bluetooth chat services
			if (mChatService != null) {
				mChatService.stop();
				// if(D) Log.e(TAG, "--- ON DESTROY ---");
			}
		}
	}

	/**
	 * Initialisieren der grafischen Elementen
	 */
	private void init() {
		txfSpielstandHeim = (EditText) findViewById(R.id.txfHeimmannschaftAktualisiere);
		txfSpielstandGast = (EditText) findViewById(R.id.txfGastmannschaftAktualisiere);
		txfStatus = (EditText) findViewById(R.id.txfAktualisiereStatus);
		btnaktualisieren = (Button) findViewById(R.id.btnaktualisieren);
		btnloeschen = (Button) findViewById(R.id.btnloeschen);
		btnheimplusein = (Button) findViewById(R.id.btnplus1heim);
		btngastpluseins = (Button) findViewById(R.id.btnplus1gast);
		btnheimminuseins = (Button) findViewById(R.id.btnminus1heim);
		btngastminuseins = (Button) findViewById(R.id.btnminus1gast);
		switch_scoreboard = (Switch) findViewById(R.id.switch_scoreboard);
		statusScoreboard = (TextView) findViewById(R.id.statusscoreboard);
		btnzurueck = (Button) findViewById(R.id.btn_zurueck);

	}

	/**
	 * Diese Methode löscht die Veranstaltung auf der internen Datenbank.
	 * Zusätzlich wird dieses Spiel auch noch im Internet gelöscht. Dies erfolgt
	 * durch die Veranstaltungloeschen Methode von der InternetService Activty
	 * Außerdem wird noch danach Gelöscht in form eines Toasts ausgegeben.
	 */
	private void loeschen() {
		/**
		 * Hier wird die Zahl(id) der Veranstaltung geholt
		 */
		long i = getIntent().getExtras().getLong(SpieleDesUsersActivity.KEY);
		Veranstaltung updateveranstaltung = db.getVeranstaltung((int) i);
		db.deleteVeranstaltung(updateveranstaltung);
		Mitglied uebertrag = dbuuid.getMitglied();
		internetservice.Veranstaltungloeschen("" + updateveranstaltung.getId(),
				uebertrag.getUuid());
		Toast.makeText(getApplicationContext(), "Gelöscht", Toast.LENGTH_SHORT)
				.show();

		finish();
	}

	/**
	 * Diese Methode aktualisiert die Veranstaltung auf der internen Datenbank.
	 * Dazu werden die Daten aus den TextViews geholt um dann den Text daraus
	 * die einzelnen Spielstände zu aktualisieren. Zusätzlich wird dieses Spiel
	 * auch noch im Internet aktualisiert. Dies erfolgt durch die
	 * updateveranstaltung Methode von der InternetService Activty. Außerdem
	 * wird noch danach Gelöscht in form eines Toasts ausgegeben. Zustätzlich
	 * wird am ende geprüft ob eine Verbindung zum Scoreboard vorhanden ist.
	 * Trifft dieses zu wird auch die Veranstaltung auf das Scoreboard
	 * übertragen.
	 */
	private void aktualisieren() {
		/*
		 * Hier wird die Zahl(id) der Veranstaltung geholt
		 */
		long i = getIntent().getExtras().getLong(SpieleDesUsersActivity.KEY);
		Veranstaltung updateveranstaltung = db.getVeranstaltung((int) i);
		String spielstandheim = txfSpielstandHeim.getText().toString();
		String spielstandgast = txfSpielstandGast.getText().toString();
		String spielstatus = txfStatus.getText().toString();

		if (TextUtils.isEmpty(spielstandheim) == false) {
			updateveranstaltung.setSpielstandHeim(Integer
					.parseInt(spielstandheim));
		}
		if (TextUtils.isEmpty(spielstandgast) == false) {
			updateveranstaltung.setSpielstandGast(Integer
					.parseInt(spielstandgast));
		}
		if (TextUtils.isEmpty(spielstatus) == false) {
			updateveranstaltung.setStatus(spielstatus);
		}
		Mitglied uebertrag = dbuuid.getMitglied();

		try {
			internetservice.updateVeranstaltung(
					"" + updateveranstaltung.getSpielstandHeim(), ""
							+ updateveranstaltung.getSpielstandGast(),
					updateveranstaltung.getStatus(),
					"" + updateveranstaltung.getId(), uebertrag.getUuid());
			int j = db.updateVeranstaltung(updateveranstaltung);
			// Geht nicht irgendwie
			// ist schon geschlossen keine ahnung
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.toString(),
					Toast.LENGTH_LONG).show();
		}
		if (switch_scoreboard.isChecked() && freigabe) {
			sendVeranstaltung(updateveranstaltung);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.update_spiel, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Diese Methode serialisiert ein beliebiges Objekt,damit es über Bluetooth
	 * oder zwischen Activitys übergeben werden kann. In unserem Fall ist es ein
	 * Veranstaltungsobjekt.
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);
		o.writeObject(obj);
		return b.toByteArray();
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		// if(D) Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		// if(D) Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		// if(D) Log.e(TAG, "--- ON DESTROY ---");
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	/**
	 * Grundsätzlich wird diese Methode aufgerufen sobald man eine andere
	 * Activity gestartet hat oder ein Broadcast. Wie zum Beispiel die Bluetooth
	 * Aktivierung. Diese wird nach dem der Auswahl des Gerätes aufgerufen das
	 * gepaart werden soll. Dabei wird die Adresse geholt und dann diese dann
	 * dem BluetoothService übergeben um so eine Verbindung aufbauen zu können.
	 * 
	 * 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		try {
			switch (requestCode) {
			case REQUEST_CONNECT_DEVICE:
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK) {
					// Get the device MAC address
					String address = data.getExtras().getString(
							GeräteSuchen.EXTRA_DEVICE_ADDRESS);

					// Get the BLuetoothDevice object
					BluetoothDevice device = mBluetoothAdapter
							.getRemoteDevice(address);
					// Attempt to connect to the device

					mChatService.connect(device);
				}
				break;
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a chat session
					setupUebertragung();
					Intent serverIntent = new Intent(getApplicationContext(),
							GeräteSuchen.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
				} else {
					// User did not enable Bluetooth or an error occured
					// Toast.makeText(this, R.string.bt_not_enabled_leaving,
					// Toast.LENGTH_SHORT).show();
					Toast.makeText(getApplicationContext(),
							"Bluetooth muss aktiviert sein", Toast.LENGTH_SHORT)
							.show();
					switch_scoreboard.setChecked(false);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setupUebertragung() {
		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothService(this, mHandlerHier);
		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
	}

	/**
	 * Veranstaltung wird geschickt. Über die BluetoothServiceKlasse geschickt.
	 * Es wird ein ByteArray über Bluetooth geschickt deswegen muss das Objekt
	 * serialisiert werden.
	 * 
	 * @param veranstaltung
	 */
	private void sendVeranstaltung(Veranstaltung veranstaltung) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
			// Toast.makeText(this, R.string.not_connected,
			// Toast.LENGTH_SHORT).show();
			return;
		}

		if (veranstaltung != null) {

			byte[] send = null;
			try {
				send = serialize(veranstaltung);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// send=veranstaltung.getBytes();

			mChatService.write(send);

		}
	}

	/**
	 * Der Handler empfängt Nachrichten der BluetoothService klasse. Denn nur
	 * durch einen Handler können Daten von einem Thread abgerufen werden.
	 * 
	 */
	private final Handler mHandlerHier = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:

				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					// mTitle.setText(R.string.title_connected_to);
					// mTitle.append(mConnectedDeviceName);
					// mConversationArrayAdapter.clear();
					break;
				case BluetoothService.STATE_CONNECTING:
					// mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					// mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;

			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				freigabe = true;
				statusScoreboard.setText("Verbunden");
				break;
			/**
			 * Wird nicht Verbunden geschickt, geht der Scoreboardswitcher
			 * wieder auf die Off Position.
			 */
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST),

						Toast.LENGTH_SHORT).show();
				if (msg.getData().getString(TOAST)
						.equals("Unable to connect device")
						|| msg.getData().getString(TOAST)
								.equals("Device connection was lost")) {
					switch_scoreboard.setChecked(false);
					statusScoreboard.setText("Nicht Verbunden");
					freigabe = false;

				}

				break;
			}
		}
	};
}
