package tk.scoreli.liveticker.internet;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tk.scoreli.liveticker.ImSpielActivity;
import tk.scoreli.liveticker.MainActivity;
import tk.scoreli.liveticker.NavigationDrawerFragment;
import tk.scoreli.liveticker.NeuesSpielActivity;
import tk.scoreli.liveticker.SpieleDesUsersActivity;
import tk.scoreli.liveticker.data.DatabasehandlerSpiele;
import tk.scoreli.liveticker.data.Veranstaltung;
import tk.scoreli.liveticker.loginregister.AppConfig;
import tk.scoreli.liveticker.loginregister.AppController;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Diese Activity regelt die Kommunikation mit dem Internet und schickt und holt
 * die Veranstaltungen usw.
 * 
 * @author philipp
 *
 */
public class InternetService extends Activity {
	private static final String TAGregister = NeuesSpielActivity.class
			.getSimpleName();
	public SpieleDesUsersActivity desUsers;
	DatabasehandlerSpiele db;
	private static final String TAG_Veranstaltungen = "veranstaltung";
	// Für die Veranstaltungen holen
	private static final String TAGholen = NavigationDrawerFragment.class
			.getSimpleName();
	private static final String TAGimSpiel = ImSpielActivity.class
			.getSimpleName();
	private static final String TAG_VeranstaltungenUserLogcat = SpieleDesUsersActivity.class
			.getSimpleName();
	private static final String TAG_VeranstaltungenDesUsers = "veranstaltungdesusers";
	private ProgressDialog pDialog;
	Activity hans;
	private MainActivity mainactivity;

	/**
	 * Hierbei wird die Activity übergeben und dann dem Attribut der Klasse
	 * Internet Service übergeben. Dies ist nötig, da die Methoden auf die
	 * Activity zugreigen. Und so können sie das.
	 * 
	 * @param servus
	 */
	public InternetService(Activity servus) {
		db = new DatabasehandlerSpiele(servus.getApplicationContext());
		hans = servus;

		// Progress dialog
		// Geht nicht
		// pDialog = new ProgressDialog(servus.getApplicationContext());
		// pDialog.setCancelable(false);
	}

	/**
	 * Hierbei wird die Activity übergeben und dann dem Attribut der Klasse
	 * Internet Service übergeben. Dies ist nötig, da die Methoden auf die
	 * Activity zugreigen. Und so können sie das. Außerdem wird noch die Spiele
	 * des User Activity übergeben. Damit Veranstaltung anzeigen funktioniert.
	 * 
	 * @param servus
	 * @param spieledesUsers
	 */
	public InternetService(Activity servus,
			SpieleDesUsersActivity spieledesUsers) {
		db = new DatabasehandlerSpiele(servus.getApplicationContext());
		hans = servus;
		desUsers = spieledesUsers;
		// Progress dialog
		// Geht nicht
		// pDialog = new ProgressDialog(servus.getApplicationContext());
		// pDialog.setCancelable(false);
	}

	/**
	 * Diese Methode dient zur Speicherung einer Veranstaltungen online. Dazu
	 * werden die einzelnen Attribute übergeben. Dieses wird mit der Android
	 * Volley Bibliotehk realisiert. Dabei wird mit JSON gearbeitet. Dazu wird
	 * erst ein Anfangstag geschickt. Laut diesem entscheidet der Server welche
	 * Methode/Zweig ausgeführt werden sollen. Dabei sind dann noch weitere Tags
	 * versehen die dann mit den Eigenschaften der neuen Veranstaltungen
	 * versehen sind. Diese werden dann auch geholt. Nach diesem Vorgang wartet
	 * die Methode auf das Rückarray und speichert dieses dann auch in der
	 * internen Datenbank ab. Das heißt es wird nur eins gespeichert wenn die
	 * Speicherung online auch erfolgreich war. Nach diesem Prinzip funktioniert
	 * jede Methode so die mit dem Internet etwas zu tun hat.
	 * */
	public void registerVeranstaltung(final String sportart,
			final String user_id, final String heimmannschaft,
			final String gastmannschaft, final String punkteHeim,
			final String punkteGast, final String spielbeginn,
			final String status) {
		// Tag used to cancel the request
		String tag_string_req = "req_registerveranstaltung";

		// pDialog.setMessage("Erstellen ...");
		// showDialog();

		StringRequest strReq = new StringRequest(Method.POST,
				AppConfig.URL_VERANSTALTUNG, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAGregister,
								"Register Response: " + response.toString());
						// hideDialog();
						try {
							JSONObject jObj = new JSONObject(response);
							boolean error = jObj.getBoolean("error");
							if (!error) {
								// User successfully stored in MySQL
								// Now store the user in sqlite
								// String uid = jObj.getString("uid");

								JSONObject user = jObj
										.getJSONObject("veranstaltung");
								String idj = user.getString("veranstaltung_id");
								String sportartj = user.getString("sportart");
								String heimmannschaftj = user
										.getString("heimmannschaft");
								String gastmannschaftj = user
										.getString("gastmannschaft");
								String punkteHeimj = user
										.getString("punkteHeim");
								String punkteGastj = user
										.getString("punkteGast");
								String spielbeginnj = user
										.getString("spielbeginn");
								String statusj = user.getString("status");

								db.addVeranstaltung(new Veranstaltung(Long
										.parseLong(idj), sportartj,
										heimmannschaftj, gastmannschaftj,
										Integer.parseInt(punkteHeimj), Integer
												.parseInt(punkteGastj),
										spielbeginnj, statusj));

								/*
								 * // Launch login activity Intent intent = new
								 * Intent( RegisterVeranstaltung.this,
								 * LoginActivity.class); startActivity(intent);
								 * finish();
								 */
								hans.finish();// Hat beendet da man was
												// aufgerufen
												// hat obwohl es beendet worden
												// ist.
							} else {

								// Error occurred in registration. Get the error
								// message
								String errorMsg = jObj.getString("error_msg");

								Toast.makeText(hans.getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();

							}
						} catch (JSONException e) {
							e.printStackTrace();
							Toast.makeText(hans.getApplicationContext(),
									e.toString(), Toast.LENGTH_LONG).show();
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAGregister,
								"Registration Error: " + error.getMessage());

						Toast.makeText(hans.getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();
						// hideDialog();

					}
				}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("tag", "veranstaltung");// Zuerst Tag dann
													// Daten
				params.put("sportart", sportart);
				params.put("user", user_id);
				params.put("heimmannschaft", heimmannschaft);
				params.put("gastmannschaft", gastmannschaft);
				params.put("punkteHeim", punkteHeim);
				params.put("punkteGast", punkteGast);
				params.put("spielbeginn", spielbeginn);
				params.put("status", status);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	/**
	 * Die Funktion holt alle Veranstaltungen und speichert diese in die
	 * My_Sqllite Datenbank ab. Dies wird nach der gleichen vorhergehensweise
	 * realisiert wie in registerVeranstaltung(). Da die gleiche Bibliothek
	 * benutzt wird.
	 * */
	public void Veranstaltungholen() {
		// Tag used to cancel the request
		String tag_string_req = "req_holen";

		// pDialog.setMessage("Holen ...");
		// showDialog();

		StringRequest strReq = new StringRequest(Method.POST,
				AppConfig.URL_VERANSTALTUNG, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAGholen,
								"Veranstaltung Holen: " + response.toString());
						// hideDialog();
						try {
							/*
							 * Toast.makeText(getApplicationContext(),
							 * response.toString(), Toast.LENGTH_SHORT) .show();
							 */
							JSONObject jObj = new JSONObject(response);
							// boolean error = jObj.getBoolean("error");
							boolean error = false;
							if (!error) {
								// User successfully stored in MySQL
								// Now store the user in sqlite
								db.deleteVeranstaltungen();

								JSONArray uebergabe = jObj
										.getJSONArray(TAG_Veranstaltungen);
								for (int i = 0; i < uebergabe.length(); i++) {
									JSONObject veranstaltung = uebergabe
											.getJSONObject(i);
									String idj = veranstaltung
											.getString("veranstaltung_id");
									String heimmannschaftj = veranstaltung
											.getString("heimmannschaft");
									String gastmannschaftj = veranstaltung
											.getString("gastmannschaft");
									String punkteHeimj = veranstaltung
											.getString("punkteHeim");
									String punkteGastj = veranstaltung
											.getString("punkteGast");
									String statusj = veranstaltung
											.getString("status");
									String sportartj = veranstaltung
											.getString("sportart");
									String spielbeginnj = veranstaltung
											.getString("spielbeginn");
									db.addVeranstaltung(new Veranstaltung(Long
											.parseLong(idj), sportartj,
											heimmannschaftj, gastmannschaftj,
											Integer.parseInt(punkteHeimj),
											Integer.parseInt(punkteGastj),
											spielbeginnj, statusj));

								}
								Toast.makeText(hans.getApplicationContext(),
										"Aktualisiert", Toast.LENGTH_SHORT)
										.show();
							} else {

								// Error occurred in registration. Get the error
								// message
								// String errorMsg =
								// jObj.getString("error_msg");

								// Toast.makeText(getApplicationContext(),
								// errorMsg, Toast.LENGTH_LONG).show();

							}
						} catch (JSONException e) {
							// JSON error
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAGholen, "Holenfehler: " + error.getMessage());

						Toast.makeText(hans.getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();
						// hideDialog();

					}
				}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("tag", "holeveranstaltungen");
				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	/*
	 * public void Veranstaltungbeenden(final String veranstaltungs_id, final
	 * String user_id) { // Tag used to cancel the request String tag_string_req
	 * = "req_beendeVeranstaltung"; /* //
	 * pDialog.setMessage("Beenden der Veranstaltung ..."); // showDialog();
	 * 
	 * StringRequest strReq = new StringRequest(Method.POST,
	 * AppConfig.URL_VERANSTALTUNG, new Response.Listener<String>() {
	 * 
	 * @Override public void onResponse(String response) { Log.d(TAGimSpiel,
	 * "Veranstaltung Response: " + response.toString()); // hideDialog();
	 * 
	 * try {
	 * 
	 * /* Toast.makeText(getApplicationContext(), response.toString(),
	 * Toast.LENGTH_SHORT) .show();
	 */
	/*
	 * JSONObject jObj = new JSONObject(response); boolean error =
	 * jObj.getBoolean("error"); if (!error) {
	 * 
	 * Toast.makeText(hans.getApplicationContext(), "Beendet",
	 * Toast.LENGTH_SHORT).show(); } else {
	 * Toast.makeText(hans.getApplicationContext(), "Beenden fehlgeschlagen",
	 * Toast.LENGTH_SHORT).show(); } } catch (JSONException e) { // JSON error
	 * e.printStackTrace(); }
	 * 
	 * }
	 * 
	 * }, new Response.ErrorListener() {
	 * 
	 * @Override public void onErrorResponse(VolleyError error) {
	 * Log.e(TAGimSpiel, "Beenden Error: " + error.getMessage());
	 * 
	 * Toast.makeText(hans.getApplicationContext(), error.getMessage(),
	 * Toast.LENGTH_LONG).show(); // hideDialog();
	 * 
	 * } }) {
	 * 
	 * @Override protected Map<String, String> getParams() { // Posting params
	 * to register url Map<String, String> params = new HashMap<String,
	 * String>(); params.put("tag", "beendeveranstaltung");
	 * params.put("veranstaltungs_id", veranstaltungs_id); params.put("user",
	 * user_id); return params; }
	 * 
	 * };
	 * 
	 * // Adding request to request queue
	 * AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	 * 
	 * }
	 */
	/**
	 * Bei dieser Methode wird eine Veranstaltung gelöscht. Dabei wird die
	 * veranstaltungs_id und die user_id (uuid) benötigt. Es wird dabei wieder
	 * die Volleybibliothek benutzt.
	 * 
	 * @param veranstaltungs_id
	 * @param user_id
	 */
	public void Veranstaltungloeschen(final String veranstaltungs_id,
			final String user_id) {
		// Tag used to cancel the request
		String tag_string_req = "req_loescheVeranstaltung";

		// pDialog.setMessage("Löschen ...");
		// showDialog();

		StringRequest strReq = new StringRequest(Method.POST,
				AppConfig.URL_VERANSTALTUNG, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAGimSpiel,
								"Veranstaltung Response: "
										+ response.toString());
						// hideDialog();

						try {

							/*
							 * Toast.makeText(getApplicationContext(),
							 * response.toString(), Toast.LENGTH_SHORT) .show();
							 */

							JSONObject jObj = new JSONObject(response);
							boolean error = jObj.getBoolean("error");
							if (!error) {

								Toast.makeText(hans.getApplicationContext(),
										"Gelöscht", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(hans.getApplicationContext(),
										"Löschen fehlgeschlagen",
										Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) { // JSON error
							e.printStackTrace();
						}

					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAGimSpiel,
								"Registration Error: " + error.getMessage());

						Toast.makeText(hans.getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();
						// hideDialog();

					}
				}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("tag", "loescheveranstaltung");
				params.put("veranstaltungs_id", veranstaltungs_id);
				params.put("user", user_id);
				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	/**
	 * Bei dieser Mehtode wird die Veranstaltung geupdatet. Dabei wird wieder
	 * die Volleybibiliothek benutzt. Aber die geänderte Veranstaltung wird
	 * nicht wieder empfangen .
	 * 
	 * @param punkteHeim
	 * @param punkteGast
	 * @param status
	 * @param veranstaltungs_id
	 */
	public void updateVeranstaltung(final String punkteHeim,
			final String punkteGast, final String status,
			final String veranstaltungs_id, final String user_id) {
		// Tag used to cancel the request
		String tag_string_req = "req_updateveranstaltung";

		// pDialog.setMessage("Aktualisieren ...");
		// showDialog();

		StringRequest strReq = new StringRequest(Method.POST,
				AppConfig.URL_VERANSTALTUNG, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAGimSpiel,
								"Register Response: " + response.toString());
						// hideDialog();

						try {
							JSONObject jObj = new JSONObject(response);
							boolean error = jObj.getBoolean("error");
							if (!error) {
								// User successfully stored in MySQL
								// Now store the user in sqlite
								// String uid = jObj.getString("uid");
								/*
								 * Toast.makeText(getApplicationContext(),
								 * response, Toast.LENGTH_LONG).show();
								 * JSONObject user = jObj
								 * .getJSONObject("veranstaltung"); String idj =
								 * user.getString("veranstaltung_id"); String
								 * sportartj = user.getString("sportart");
								 * String heimmannschaftj = user
								 * .getString("heimmannschaft"); String
								 * gastmannschaftj = user
								 * .getString("gastmannschaft"); String
								 * punkteHeimj = user .getString("punkteHeim");
								 * String punkteGastj = user
								 * .getString("punkteGast"); String spielbeginnj
								 * = user .getString("spielbeginn"); String
								 * statusj = user.getString("status");
								 * 
								 * 
								 * 
								 * int ka = db.updateVeranstaltung(new
								 * Veranstaltung(Long .parseLong(idj),
								 * sportartj, heimmannschaftj, gastmannschaftj,
								 * Integer.parseInt(punkteHeimj), Integer
								 * .parseInt(punkteGastj),
								 * 
								 * spielbeginnj, statusj));
								 */
								// finish();// Hat beendet da man was aufgerufen
								// hat obwohl es beendet worden ist.
								Toast.makeText(hans.getApplicationContext(),
										"Aktualisiert", Toast.LENGTH_SHORT)
										.show();
							} else {

								// Error occurred in registration. Get the error
								// message
								String errorMsg = jObj.getString("error_msg");

								Toast.makeText(hans.getApplicationContext(),
										errorMsg, Toast.LENGTH_LONG).show();

							}
						} catch (JSONException e) {
							e.printStackTrace();
							Toast.makeText(hans.getApplicationContext(),
									e.toString(), Toast.LENGTH_LONG).show();
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAGimSpiel,
								"Registration Error: " + error.getMessage());

						Toast.makeText(hans.getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();
						// hideDialog();

					}
				}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("tag", "updateveranstaltung");// Zuerst Tag dann
				// Daten
				params.put("user", user_id);
				params.put("punkteHeim", punkteHeim);
				params.put("punkteGast", punkteGast);
				params.put("veranstaltungs_id", veranstaltungs_id);
				params.put("status", status);

				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	/**
	 * Hier werden nur die Veranstaltungen des Users geholt. Diese Methode ist
	 * wieder mit der Volleybibliothek realisiert. Sie ist nötig das man diese
	 * löschen oder ändern kann.
	 * 
	 * @param user_id
	 */
	public void VeranstaltungholenDesUsers(final String user_id) {
		// Tag used to cancel the request
		String tag_string_req = "req_holendesUsers";

		// pDialog.setMessage("Holen ...");
		// showDialog();

		StringRequest strReq = new StringRequest(Method.POST,
				AppConfig.URL_VERANSTALTUNG, new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG_VeranstaltungenUserLogcat,
								"Veranstaltung Response: "
										+ response.toString());
						// hideDialog();
						try {

							// Toast.makeText(hans.getApplicationContext(),
							// response.toString(), Toast.LENGTH_SHORT) .show();

							JSONObject jObj = new JSONObject(response);
							// boolean error = jObj.getBoolean("error");
							boolean error = false;
							if (!error) {
								// User successfully stored in MySQL
								// Now store the user in sqlite
								db.deleteVeranstaltungen();

								JSONArray uebergabe = jObj
										.getJSONArray(TAG_VeranstaltungenDesUsers);
								for (int i = 0; i < uebergabe.length(); i++) {
									JSONObject veranstaltung = uebergabe
											.getJSONObject(i);
									String idj = veranstaltung
											.getString("veranstaltung_id");
									String heimmannschaftj = veranstaltung
											.getString("heimmannschaft");
									String gastmannschaftj = veranstaltung
											.getString("gastmannschaft");
									String punkteHeimj = veranstaltung
											.getString("punkteHeim");
									String punkteGastj = veranstaltung
											.getString("punkteGast");
									String statusj = veranstaltung
											.getString("status");
									String sportartj = veranstaltung
											.getString("sportart");
									String spielbeginnj = veranstaltung
											.getString("spielbeginn");
									db.addVeranstaltung(new Veranstaltung(Long
											.parseLong(idj), sportartj,
											heimmannschaftj, gastmannschaftj,
											Integer.parseInt(punkteHeimj),
											Integer.parseInt(punkteGastj),
											spielbeginnj, statusj));

									desUsers.Veranstaltungzeigen();
								}

								Toast.makeText(hans.getApplicationContext(),
										"Aktualisiert", Toast.LENGTH_SHORT)
										.show();
							} else {

								// Error occurred in registration. Get the error
								// message
								// String errorMsg =
								// jObj.getString("error_msg");

								// Toast.makeText(hans.getApplicationContext(),
								// errorMsg, Toast.LENGTH_LONG).show();

							}
						} catch (JSONException e) {
							// JSON error
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG_VeranstaltungenUserLogcat,
								"Registration Error: " + error.getMessage());

						Toast.makeText(hans.getApplicationContext(),
								error.getMessage(), Toast.LENGTH_LONG).show();
						// hideDialog();

					}
				}) {

			@Override
			protected Map<String, String> getParams() {
				// Posting params to register url
				Map<String, String> params = new HashMap<String, String>();
				params.put("tag", "holeveranstaltungendesusers");
				params.put("user", user_id);
				return params;
			}

		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
	}

	private void showDialog() {
		if (!pDialog.isShowing())
			pDialog.show();
	}

	private void hideDialog() {
		if (pDialog.isShowing())
			pDialog.dismiss();
	}
}
