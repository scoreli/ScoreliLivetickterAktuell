package tk.scoreli.liveticker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tk.scoreli.liveticker.bluetooth.BluetoothService;
import tk.scoreli.liveticker.data.Veranstaltung;
import tk.scoreli.liveticker.util.SystemUiHider;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Diese Activity ist für die Anzeige zuständig. In der App ist es das
 * Scoreboard. Sie empfaengt Daten über Bluetooth und gibt sie auf dem Screen
 * aus. Außerdem ist sie eine Fullscreen Activity. Das heißt es werden die
 * Onscreentasten und die Statusleiste ausgeblendet.
 * 
 * @author philipp
 *
 */
public class AnzeigeActivity extends Activity {
	TextView AnzeigeHeima, AnzeigekleinHeima, AnzeigeGasta, AnzeigekleinGasta,
			AnzeigeStatusa;
	private final static int REQUEST_ENABLE_BT = 1;
	private static final String TAG = "Bluetooth";
	private static final boolean D = true;

	// Der Handler empfängt nur Zahlen und diese werden hier zugeordnet
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	Veranstaltung veranstaltung;

	// Layout Views
	// private TextView mTitle;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothService mChatService = null;
	// Hier das sie überall zu verfügung steht.
	Veranstaltung zeigeVeranstaltung = new Veranstaltung();
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anzeige);
		init();
		try {
			/**
			 * Hier wird das Scoreboard aktualisiert. Beim ersten Starten wird
			 * das schon vollfuehrt,weil beim Beenden/Abreißen der Verbindung
			 * die Activity neugeladen wird. Dann wird der zuletz gespeicherte
			 * wert übergeben.
			 */
			aktualisiereScoreboard(deserialize(getIntent().getExtras()
					.getByteArray(MainActivity.KEY)));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/**
		 * Hat das Geraet Bluetooth ?
		 */
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		/**
		 * Wenn kein BluetoothAdapter gefunden wurde wird die Activity beendet
		 * beendet und eine Meldung ausgegeben.
		 * 
		 */
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		/**
		 * Das wird benoetigt um bei Berührung die Anzeigenelemente auszublenden
		 * (Fullscreenactivity) Das bewirkt auch das die Anzeigenelemente
		 * ausgeblendet werden. Außerdem ist jetzt die Nervige
		 * ActivityBeschriftung weg. http://www
		 * .android-hilfe.de/android-app-entwicklung/123750-titelleiste
		 * -ausblenden-oder-faerben.html
		 */
		final View contentView = findViewById(R.id.LayoutGesamt);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});
	}

	/**
	 * Wird geprueft ob Bluetooth auch aktiviert wurde. Wenn Bluetooth nicht
	 * aktiviert wurde wird die Activity beendet und man gelangt auf den
	 * Hauptbildschirm zurück.
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (resultCode == Activity.RESULT_OK) {
			// Bluetooth ist aktiv.
		} else {
			finish();
		}
	}

	/**
	 * Bei dieser Methode werden die Elemente für das Layout initialisiert.
	 * 
	 */
	private void init() {
		AnzeigeHeima = (TextView) findViewById(R.id.AnzeigeHeim);
		AnzeigekleinHeima = (TextView) findViewById(R.id.AnzeigekleinHeim);
		AnzeigeGasta = (TextView) findViewById(R.id.AnzeigeGast);
		AnzeigekleinGasta = (TextView) findViewById(R.id.AnzeigekleinGast);
		AnzeigeStatusa = (TextView) findViewById(R.id.AnzeigeStatus);

	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");
		/**
		 * Performing this check in onResume() covers the case in which BT was
		 * not enabled during onStart(), so we were paused to enable it...
		 * onResume() will be called when ACTION_REQUEST_ENABLE activity
		 * returns.
		 */

		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");
		init();

		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothService(this, mHandler);

		// Initialize the buffer for outgoing messages
		// mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	/**
	 * Der Handler empfaengt Nachrichten der BluetoothService klasse. Denn nur
	 * durch einen Handler können Daten von einem Thread abgerufen werden.
	 * 
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
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
			/**
			 * Hier wird die Empfangene serialliesierte Veranstaltung
			 * deserialisiert. Nach erfolgreicher Umwandlung erfolgt die Anzeige
			 * auf dem Scoreboard.
			 */
			case MESSAGE_READ:

				byte[] readBuf = (byte[]) msg.obj;
				System.out.println(readBuf);
				// construct a string from the valid bytes in the buffer

				try {
					zeigeVeranstaltung = deserialize(readBuf);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				aktualisiereScoreboard(zeigeVeranstaltung);
				break;
			case MESSAGE_DEVICE_NAME:
				/**
				 * Hier wird der Name des Verbundenen Gerätes angezeigt
				 */
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Verbunden mit " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			/**
			 * Die BluetoothService Klasse meldet ein Ereignis. Wenn die
			 * Verbindung verloren geht, wird "Device connection was lost"
			 * gesendet. Nach trennen der Verbindung ist es nicht möglich die
			 * Verbindung erneut aufzubauen. Deswegen wird sobald diese
			 * Nachricht empfangen wird die Activity nochmal aufgerufen. Dabei
			 * übergibt sie den aktuellen Spielstand wieder sich selber. Somit
			 * bleibt der Spielstand erhalten.
			 */
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				if (msg.getData().getString(TOAST)
						.equals("Device connection was lost")) {

					Intent i = new Intent(AnzeigeActivity.this,
							AnzeigeActivity.class);

					byte[] uebergabe;
					try {// Daten empfangen
						/**
						 * Objekt muss serialisiert werden um über Bluetooth
						 * gesendet werden zu können.
						 */
						uebergabe = serialize(zeigeVeranstaltung);
						i.putExtra(MainActivity.KEY, uebergabe);
						startActivity(i);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finish();

				}
				break;
			}
		}
	};

	/**
	 * Hier wird das Scoreboard aktualisiert. Dabei wird ein
	 * Veranstaltungsobjekt übergeben und davon die einzelnen Eigenschaften
	 * geholt und angezeigt.
	 * 
	 * @param veranstaltung
	 */
	public void aktualisiereScoreboard(Veranstaltung veranstaltung) {
		AnzeigeHeima.setText("" + veranstaltung.getSpielstandHeim());
		AnzeigekleinHeima.setText(veranstaltung.getHeimmanschaft());
		AnzeigeGasta.setText("" + veranstaltung.getSpielstandGast());
		AnzeigekleinGasta.setText(veranstaltung.getGastmannschaft());
		AnzeigeStatusa.setText(veranstaltung.getStatus());
	}

	/*
	 * @Override public void onWindowFocusChanged(boolean hasFocus) { // TODO
	 * Auto-generated method stub super.onWindowFocusChanged(hasFocus); // Here
	 * you can get the size! int breite = AnzeigeStatusa.getWidth();
	 * Toast.makeText(getApplicationContext(), "" + breite, Toast.LENGTH_SHORT)
	 * .show(); }
	 */
	/**
	 * Diese Methode wandelt das serialisierte Objekt wieder zu einem Objekt um.
	 * In unserem Fall ein Veranstaltungsobjekt.
	 * 
	 * @param bytes
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Veranstaltung deserialize(byte[] bytes) throws IOException,
			ClassNotFoundException {
		ByteArrayInputStream b = new ByteArrayInputStream(bytes);
		ObjectInputStream o = new ObjectInputStream(b);
		return (Veranstaltung) o.readObject();
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

	// Im Beispiel von Android noch intigriert doch geht auch ohne.
	/*
	 * @Override protected void onPostCreate(Bundle savedInstanceState) {
	 * super.onPostCreate(savedInstanceState);
	 * 
	 * // Trigger the initial hide() shortly after the activity has been // //
	 * created, to briefly hint to the user that UI controls // are //
	 * available. delayedHide(100); }
	 * 
	 * /** Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	/*
	 * View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener()
	 * {
	 * 
	 * @Override public boolean onTouch(View view, MotionEvent motionEvent) { if
	 * (AUTO_HIDE) { delayedHide(AUTO_HIDE_DELAY_MILLIS); } return false; } };
	 * 
	 * Handler mHideHandler = new Handler(); Runnable mHideRunnable = new
	 * Runnable() {
	 * 
	 * @Override public void run() { mSystemUiHider.hide(); } };
	 * 
	 * /** Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	/*
	 * private void delayedHide(int delayMillis) {
	 * mHideHandler.removeCallbacks(mHideRunnable);
	 * mHideHandler.postDelayed(mHideRunnable, delayMillis); }
	 */
}
