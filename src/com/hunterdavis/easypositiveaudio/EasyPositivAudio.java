package com.hunterdavis.easypositiveaudio;

import java.io.IOException;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class EasyPositivAudio extends Activity {

	// setup our hidden sql text
	InventorySQLHelper messageData = new InventorySQLHelper(this);
	ArrayAdapter<String> m_adapterForSpinner = null;
	Uri audioUri = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		OnClickListener loadAudioListener = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				Intent intent = new Intent();
				intent.setType("audio/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Audio"), 8);
			}
		};

		TextView textView1 = (TextView) findViewById(R.id.positive);
		textView1.setOnClickListener(loadAudioListener);

		OnClickListener DeleteButtonListener = new OnClickListener() {
			public void onClick(View v) {
				yesnoDeleteHandler("Really Delete?",
						"Do you really want to delete the selected item?");
			}
		};

		OnClickListener RandomButtonListener = new OnClickListener() {
			public void onClick(View v) {
				// fill up our spinner item
				Cursor cursor = getMessageCursor();
				int cursorCount = cursor.getCount();
				Spinner spinner = (Spinner) findViewById(R.id.oldpositives);
				if (cursorCount == 1) {
					Toast.makeText(v.getContext(),
							spinner.getSelectedItem().toString(),
							Toast.LENGTH_SHORT).show();
				} else if (cursorCount > 1) {

					int randSelected = randomFromRange(0, cursorCount - 1);
					int currentSelected = spinner.getSelectedItemPosition();
					if (currentSelected != randSelected) {
						spinner.setSelection(randSelected);
					} else {
						Toast.makeText(v.getContext(),
								spinner.getSelectedItem().toString(),
								Toast.LENGTH_SHORT).show();
					}

				}
			}
		};

		OnClickListener SaveButtonListner = new OnClickListener() {
			public void onClick(View v) {

				TextView messageEdit = (TextView) findViewById(R.id.positive);
				String message = messageEdit.getText().toString();

				if(message.equalsIgnoreCase("Click to Select Audio File")) {
					return;
				}
				
				Cursor cursor = getMessageCursor();
				Boolean cursorFound = false;
				if (cursor.getCount() > 0) {
					while (cursor.moveToNext()) {
						String mac = cursor.getString(1);
						if (mac.equalsIgnoreCase(message)) {
							cursorFound = true;
							break;
						}
					}
				}
				if (cursorFound == false) {
					SQLiteDatabase db = messageData.getWritableDatabase();
					ContentValues values = new ContentValues();
					values.put(InventorySQLHelper.AUDIOURI, message);
					long latestRowId = db.insert(InventorySQLHelper.TABLE,
							null, values);
					db.close();
					m_adapterForSpinner.add(message);
					Button deleteButton = (Button) findViewById(R.id.deletebutton);
					deleteButton.setEnabled(true);
					Toast.makeText(v.getContext(), "Saved", Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(v.getContext(), "Duplicate Entry",
							Toast.LENGTH_SHORT).show();
				}

			}
		};

		OnClickListener playButtonListner = new OnClickListener() {
			public void onClick(View v) {
				playSound(v.getContext(), audioUri);
			}
		};

		Button saveButton = (Button) findViewById(R.id.savebutton);
		saveButton.setOnClickListener(SaveButtonListner);

		Button deleteButton = (Button) findViewById(R.id.deletebutton);
		deleteButton.setOnClickListener(DeleteButtonListener);

		Button randomButton = (Button) findViewById(R.id.randombutton);
		randomButton.setOnClickListener(RandomButtonListener);

		Button playButton = (Button) findViewById(R.id.playbutton);
		playButton.setOnClickListener(playButtonListner);

		// set an adapter for our spinner
		m_adapterForSpinner = new ArrayAdapter<String>(getBaseContext(),
				android.R.layout.simple_spinner_item);
		m_adapterForSpinner
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spinner = (Spinner) findViewById(R.id.oldpositives);
		spinner.setAdapter(m_adapterForSpinner);

		spinner.setOnItemSelectedListener(new MyUnitsOnItemSelectedListener());

		// fill up our spinner item
		Cursor cursor = getMessageCursor();
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				String mac = cursor.getString(1);
				m_adapterForSpinner.add(mac);
			}
			deleteButton.setEnabled(true);
		} else {
			spinner.setEnabled(false);
		}

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

	}// end of oncreate

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		final TextView textView1 = (TextView) findViewById(R.id.positive);

		if (resultCode == RESULT_OK) {

			if (requestCode == 8) {
				audioUri = data.getData();
				textView1.setText(audioUri.toString());
			}
		}
	}

	// set up the listener class for spinner
	class MyUnitsOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			// Resources res = getResources();
			// updateSqlValues(rowId, "units", unitsarray[pos]);
			TextView messsageText = (TextView) findViewById(R.id.positive);
			Spinner spinner = (Spinner) findViewById(R.id.oldpositives);

			String spinnerText = spinner.getSelectedItem().toString();
			spinner.setEnabled(true);
			messsageText.setText(spinnerText);
			Toast.makeText(view.getContext(), spinnerText, Toast.LENGTH_SHORT)
					.show();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	// get a message cursor
	private Cursor getMessageCursor() {
		SQLiteDatabase db = messageData.getReadableDatabase();
		Cursor cursor = db.query(InventorySQLHelper.TABLE, null, null, null,
				null, null, null);
		startManagingCursor(cursor);
		return cursor;
	}

	protected void yesnoDeleteHandler(String title, String mymessage) {
		new AlertDialog.Builder(this)
				.setMessage(mymessage)
				.setTitle(title)
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								TextView messageEdit = (TextView) findViewById(R.id.positive);
								String message = messageEdit.getText()
										.toString();

								DeleteMessageByName(message);

							}
						})
				.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	public void DeleteMessageByName(String card) {
		SQLiteDatabase db = messageData.getWritableDatabase();
		db.delete(InventorySQLHelper.TABLE, "audiouri = '" + card + "'", null);
		db.close();

		m_adapterForSpinner.remove(card); 

		Spinner spinner = (Spinner) findViewById(R.id.oldpositives);
		if (spinner.getCount() == 0) {
			spinner.setEnabled(false);
			Button deleteButton = (Button) findViewById(R.id.deletebutton);
			deleteButton.setEnabled(false);
		} else {
			TextView messsageText = (TextView) findViewById(R.id.positive);
			String spinnerText = spinner.getSelectedItem().toString();
			spinner.setEnabled(true);
			messsageText.setText(spinnerText);
			Toast.makeText(getBaseContext(), spinnerText, Toast.LENGTH_SHORT)
					.show();

		}

	}

	public void playSound(Context context, Uri PATH_TO_FILE) {
		if (PATH_TO_FILE == null) {
			Toast.makeText(context, "Select Audio FIle", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		MediaPlayer mp = new MediaPlayer();
		try {
			mp.setDataSource(context, PATH_TO_FILE);
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		}
		try {
			mp.prepare();
		} catch (IllegalStateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		}
		try {
			mp.start();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			Toast.makeText(context, "Error Playing Audio File!",
					Toast.LENGTH_LONG).show();
		}

	}

	int randomFromRange(int low, int high) {
		if (low == high) {
			return low;
		}
		final Random myRandom = new Random();
		Float myNewRandomPercent = myRandom.nextFloat();
		Integer actualResult = (int) Math.round(low
				+ (Math.abs((high - low)) * (myNewRandomPercent)));
		return actualResult;
	}

}// end of activity class