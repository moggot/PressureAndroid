package com.mobsoftmaster.bloodpressurediary2;

import com.mobsoftmaster.bloodpressurediary2.R;

import java.util.Calendar;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class Settings extends FragmentActivity implements
		LoaderCallbacks<Cursor> {

	CheckBox checkBoxGraph, checkBoxNotif;

	TimePicker timePicker, timeEditPicker;

	int idCurrentNotif;

	EditText editNotif;

	private static final int CM_EDIT_NOTIF = 0, CM_DELETE_NOTIF = 1,
			CM_DELETE_ALL = 2;

	final String LOG_TAG = "Pressure";

	EditText editCurrentNotif;

	String[] currentNotif = new String[] { "", "", "" };

	MyDB db;

	ListView listNotif;

	ImageView btnAddNotif;

	int count_element_notif;

	Configuration c;

	boolean rotation;
	boolean notification;

	static class time {
		public static int hour = 0;
		public static int minute = 0;
	}

	SimpleCursorAdapter scAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		db = new MyDB(this);
		db.open();

		int language = SharedPreference.LoadLanguage(this);

		Log.d(LOG_TAG, "language_Start = " + language);
		setTitle(R.string.settings);

		Typeface font = Typeface.createFromAsset(getAssets(), "Dashley.ttf");

		TextView tvSettingsTitle = (TextView) findViewById(R.id.tvSettingsTitle);
		checkBoxGraph = (CheckBox) findViewById(R.id.checkBoxRotation);
		checkBoxNotif = (CheckBox) findViewById(R.id.checkBoxTimePicker);
		checkBoxGraph.setTypeface(font);
		checkBoxNotif.setTypeface(font);
		tvSettingsTitle.setTypeface(font);

		btnAddNotif = (ImageView) findViewById(R.id.btnAddNotif);
		ImageView infoButton = (ImageView) findViewById(R.id.imageButtonInfo);
		ImageView emailUs = (ImageView) findViewById(R.id.imageButtonEmail);
		ImageView btnLanguage = (ImageView) findViewById(R.id.imageButtonLanguage);

		editNotif = (EditText) findViewById(R.id.editNotif);

		listNotif = (ListView) findViewById(R.id.listNotif);

		Calendar cal_alarm = Calendar.getInstance();

		timePicker = (TimePicker) findViewById(R.id.timePicker);
		timePicker.setIs24HourView(true);
		timePicker.setCurrentHour(cal_alarm.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(cal_alarm.get(Calendar.MINUTE));

		rotation = SharedPreference.LoadRotation(this);
		notification = SharedPreference.LoadNotification(this);

		if (rotation) {
			checkBoxGraph.setChecked(true);
		} else {
			checkBoxGraph.setChecked(false);
		}

		if (notification) {
			checkBoxNotif.setChecked(true);
			editNotif.setEnabled(true);
			btnAddNotif.setVisibility(View.VISIBLE);
			timePicker.setVisibility(View.VISIBLE);
			editNotif.requestFocus();
			if (editNotif.requestFocus()) {
				getWindow().setSoftInputMode(
						LayoutParams.SOFT_INPUT_ADJUST_NOTHING
								| LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			}
			listNotif.setEnabled(true);
		} else {
			SharedPreference.SavePreferences(this,
					SharedPreference.s_notification, notification);
			btnAddNotif.setVisibility(View.GONE);
			timePicker.setVisibility(View.GONE);
			checkBoxNotif.setChecked(false);
			editNotif.setEnabled(false);
			listNotif.setEnabled(false);
		}

		checkBoxNotif
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton checkView,
							boolean isChecked) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

						if (checkView.isChecked()) {
							btnAddNotif.setVisibility(View.VISIBLE);
							timePicker.setVisibility(View.VISIBLE);
							editNotif.setEnabled(true);
							editNotif.requestFocus();
							if (editNotif.requestFocus()) {
								imm.showSoftInput(editNotif,
										InputMethodManager.SHOW_IMPLICIT);
							}
							timePicker.setEnabled(true);
							btnAddNotif.setEnabled(true);
							listNotif.setEnabled(true);
							Cursor cursor = db.getAllDataNotif();
							if (cursor != null) {
								cursor.moveToFirst();
								for (int i = 0; i < cursor.getCount(); ++i) {
									SetAlarm(
											Integer.valueOf(cursor.getString(0)),
											cursor.getString(1),
											Integer.valueOf(cursor.getString(2)),
											Integer.valueOf(cursor.getString(3)));
									cursor.moveToNext();
								}

							}
						} else {
							btnAddNotif.setVisibility(View.GONE);
							timePicker.setVisibility(View.GONE);
							imm.hideSoftInputFromWindow(
									editNotif.getWindowToken(), 0);
							timePicker.setEnabled(false);
							btnAddNotif.setEnabled(false);
							editNotif.setEnabled(false);
							listNotif.setEnabled(false);
							Cursor cursor = db.getAllDataNotif();
							if (cursor != null) {
								cursor.moveToFirst();
								for (int i = 0; i < cursor.getCount(); ++i) {
									CancelAlarm(Integer.valueOf(cursor
											.getString(0)));
									cursor.moveToNext();
								}
							}
						}
						SharedPreference.SavePreferences(getBaseContext(),
								SharedPreference.s_notification,
								checkView.isChecked());
					}
				});

		// формируем столбцы сопоставления
		String[] from = new String[] { MyDB.COLUMN_NOTIF_HOUR,
				MyDB.COLUMN_NOTIF_MINUTE, MyDB.COLUMN_NOTIF_MESSAGE };
		int[] to = new int[] { R.id.hourSettings, R.id.minuteSettings,
				R.id.notifSettings };

		// создааем адаптер и настраиваем список
		scAdapter = new SimpleCursorAdapter(this, R.layout.list_settings, null,
				from, to, 0);

		registerForContextMenu(listNotif);

		getSupportLoaderManager().initLoader(0, null, this);

		listNotif.setAdapter(scAdapter);

		count_element_notif = db.getCountElementsSettings();

		btnAddNotif.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				timePicker.clearFocus();
				time.hour = timePicker.getCurrentHour();
				time.minute = timePicker.getCurrentMinute();
				String hour;
				String minute;
				if (time.hour < 10)
					hour = "0" + String.valueOf(time.hour);
				else
					hour = String.valueOf(time.hour);
				if (time.minute < 10)
					minute = "0" + String.valueOf(time.minute);
				else
					minute = String.valueOf(time.minute);

				if (0 != editNotif.getText().toString().length()) {

					// сохраняем в базу добавляемое уведомление, заодним на
					// сгенериться для него id
					db.addNotif(editNotif.getText().toString(), hour, minute);
					// берём из базы только что созданное уведомление
					Cursor cursor = db.getAllDataNotif();
					cursor.moveToLast();
					SetAlarm(Integer.valueOf(cursor.getString(0)),
							cursor.getString(1),
							Integer.valueOf(cursor.getString(2)),
							Integer.valueOf(cursor.getString(3)));
					editNotif.setText("");
					getSupportLoaderManager().getLoader(0).forceLoad();
					scrollMyListViewToBottom();
					addNotif();
				} else {
					inCorrectData();
				}
			}
		});

		infoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Settings.this, ProgrammInfo.class);
				startActivity(intent);
				overridePendingTransition(R.anim.open_window_start,
						R.anim.open_window_end);
			}
		});

		emailUs.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);

				emailIntent.setType("plain/text");
				// Кому
				Resources res = getResources();
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						new String[] { res.getString(R.string.app_email) });
				// Зачем
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"Blood pressure diary errors");
				// О чём
				Settings.this.startActivity(Intent.createChooser(emailIntent,
						res.getString(R.string.mail_sanding)));
			}
		});
		btnLanguage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showLanguage();
			}
		});
		scrollMyListViewToBottom();

	}

	@Override
	public void onBackPressed() {
		rotation = checkBoxGraph.isChecked();

		if ((db.getCountElementsSettings() == 0)
				&& (checkCheckBox(checkBoxNotif)))
			checkBoxNotif.setChecked(false);
		notification = checkBoxNotif.isChecked();
		SharedPreference.SavePreferences(this, SharedPreference.s_rotation,
				rotation);
		SharedPreference.SavePreferences(this, SharedPreference.s_notification,
				notification);
		super.onBackPressed();
		overridePendingTransition(R.anim.close_window_start,
				R.anim.close_window_end);
	}

	public void showLanguage() {
		final Dialog dialog = new Dialog(Settings.this,
				R.style.Dialogeasydealtheme);
		dialog.setContentView(R.layout.dialog_language);
		Resources res = getResources();
		dialog.setTitle(res.getString(R.string.language));

		Button btnEnglish = (Button) dialog.findViewById(R.id.btnEnglish);
		Button btnRussian = (Button) dialog.findViewById(R.id.btnRussian);
		Button btnChinese = (Button) dialog.findViewById(R.id.btnChinese);

		c = new Configuration(getResources().getConfiguration());

		btnEnglish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int language = 1;
				Intent intent = getIntent();
				finish();
				startActivity(intent);
				c.locale = Locale.ENGLISH;
				getResources().updateConfiguration(c,
						getResources().getDisplayMetrics());
				SharedPreference.saveLanguage(getBaseContext(),
						SharedPreference.s_language, language);
				dialog.dismiss();
			}
		});

		btnRussian.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int language = 2;
				Intent intent = getIntent();
				finish();
				startActivity(intent);
				Locale myLocale = new Locale("ru", "RU");
				c.locale = myLocale;
				getResources().updateConfiguration(c,
						getResources().getDisplayMetrics());
				SharedPreference.saveLanguage(getBaseContext(),
						SharedPreference.s_language, language);
				dialog.dismiss();
			}
		});

		btnChinese.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int language = 3;
				Intent intent = getIntent();
				finish();
				startActivity(intent);
				c.locale = Locale.CHINESE;
				getResources().updateConfiguration(c,
						getResources().getDisplayMetrics());
				SharedPreference.saveLanguage(getBaseContext(),
						SharedPreference.s_language, language);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public void SetAlarm(int id, String message, int hour, int minute) {
		AlarmManager am = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, AlarmManagerBroadcastReceiver.class);
		Resources res = this.getResources();
		intent.putExtra("message", message);
		intent.putExtra("appName", res.getString(R.string.app_name));
		Calendar calendar = Calendar.getInstance();
		Calendar now = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		long alarm = 0;
		if (calendar.getTimeInMillis() <= now.getTimeInMillis())
			alarm = calendar.getTimeInMillis()
					+ (AlarmManager.INTERVAL_DAY + 1);
		else
			alarm = calendar.getTimeInMillis();
		PendingIntent pi = PendingIntent.getBroadcast(this, id, intent, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, alarm,
				AlarmManager.INTERVAL_DAY, pi);
	}

	public void CancelAlarm(int id) {
		Intent intent = new Intent(this, AlarmManagerBroadcastReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(this, id, intent, 0);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

	public void dialogNotif() {
		final Dialog dialog = new Dialog(Settings.this,
				R.style.Dialogeasydealtheme);
		dialog.setContentView(R.layout.dialog_settings);
		Button btnSaveNotif = (Button) dialog.getWindow().findViewById(
				R.id.btnSaveNotif);
		editCurrentNotif = (EditText) dialog.getWindow().findViewById(
				R.id.editCurrentNotif);
		editCurrentNotif.requestFocus();
		dialog.getWindow().setSoftInputMode(
				LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		editCurrentNotif.setText(currentNotif[0]);

		timeEditPicker = (TimePicker) dialog.getWindow().findViewById(
				R.id.timeEditPicker);
		timeEditPicker
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		timeEditPicker.setIs24HourView(true);

		timeEditPicker.setCurrentHour(Integer.valueOf(currentNotif[1]));
		timeEditPicker.setCurrentMinute(Integer.valueOf(currentNotif[2]));

		btnSaveNotif.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String hour;
				String minute;
				if (timeEditPicker.getCurrentHour() < 10)
					hour = "0"
							+ String.valueOf(timeEditPicker.getCurrentHour());
				else
					hour = String.valueOf(timeEditPicker.getCurrentHour());
				if (timeEditPicker.getCurrentMinute() < 10)
					minute = "0"
							+ String.valueOf(timeEditPicker.getCurrentMinute());
				else
					minute = String.valueOf(timeEditPicker.getCurrentMinute());

				if (0 != editCurrentNotif.getText().toString().length()) {
					db.editNotif(String.valueOf(editCurrentNotif.getText()),
							hour, minute, String.valueOf(idCurrentNotif));
					String[] s = db.getCurrentNotif(idCurrentNotif);
					SetAlarm(idCurrentNotif, s[0], Integer.valueOf(s[1]),
							Integer.valueOf(s[2]));
					Log.d(LOG_TAG, "hour = " + Integer.valueOf(s[1]));
					Log.d(LOG_TAG, "minute = " + Integer.valueOf(s[2]));
					getSupportLoaderManager().getLoader(0).forceLoad();
					scrollMyListViewToBottom();
					changeNotif();
				}
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void scrollMyListViewToBottom() {
		listNotif.post(new Runnable() {
			@Override
			public void run() {
				listNotif.setAdapter(scAdapter);
				Cursor cursor = db.getAllDataNotif();
				// Select the last row so it will scroll into view...
				listNotif.setSelection(cursor.getCount() + 1);
			}
		});

	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CM_EDIT_NOTIF, 0, R.string.edit_notif);
		menu.add(0, CM_DELETE_NOTIF, 0, R.string.delete_notif);
		menu.add(0, CM_DELETE_ALL, 0, R.string.delete_notif_all);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getItemId() == CM_DELETE_NOTIF) {
			// извлекаем id записи и удаляем соответствующую запись в БД
			db.delRecNotif(acmi.id);
			CancelAlarm((int) acmi.id);
			// получаем новый курсор с данными
			getSupportLoaderManager().getLoader(0).forceLoad();
			deleteNotif();
			scrollMyListViewToBottom();
			return true;
		} else if (item.getItemId() == CM_EDIT_NOTIF) {
			idCurrentNotif = (int) acmi.id;
			currentNotif = db.getCurrentNotif(acmi.id);
			dialogNotif();
			scrollMyListViewToBottom();
			return true;
		} else if (item.getItemId() == CM_DELETE_ALL) {
			showChoice();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public void showChoice() {
		final Dialog dialog = new Dialog(Settings.this,
				R.style.Dialogeasydealtheme);
		dialog.setContentView(R.layout.dialog_choice);
		dialog.setTitle(R.string.are_you_sure);

		Button btnYes = (Button) dialog.findViewById(R.id.btnYes);
		Button btnNo = (Button) dialog.findViewById(R.id.btnNo);

		btnYes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Cursor cursor = db.getAllDataNotif();
				if (cursor != null) {
					cursor.moveToFirst();
					for (int i = 0; i < cursor.getCount(); ++i) {
						CancelAlarm(Integer.valueOf(cursor.getString(0)));
						cursor.moveToNext();
					}
				}
				db.delRecAllNotif();

				getSupportLoaderManager().getLoader(0).forceLoad();
				deleteAllNotif();
				dialog.dismiss();
			}
		});

		btnNo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean checkCheckBox(View v) {
		CheckBox checkBoxGraph = (CheckBox) v;
		if (!checkBoxGraph.isChecked())
			return false;
		return true;
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
		return new MyCursorLoader(this, db);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		scAdapter.swapCursor(cursor);
	}

	static class MyCursorLoader extends CursorLoader {

		MyDB db;

		public MyCursorLoader(Context context, MyDB db) {
			super(context);
			this.db = db;
		}

		@Override
		public Cursor loadInBackground() {
			Cursor cursor = db.getAllDataNotif();
			return cursor;
		}
	}

	void inCorrectData() {
		Toast.makeText(this, R.string.correct_notif, Toast.LENGTH_SHORT).show();
	}

	void deleteData() {
		Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
	}

	void addNotif() {
		Toast.makeText(this, R.string.notif_added, Toast.LENGTH_SHORT).show();
	}

	void changeNotif() {
		Toast.makeText(this, R.string.notif_changed, Toast.LENGTH_SHORT).show();
	}

	void deleteNotif() {
		Toast.makeText(this, R.string.notif_deleted, Toast.LENGTH_SHORT).show();
	}

	void deleteAllNotif() {
		Toast.makeText(this, R.string.notif_all_deleted, Toast.LENGTH_SHORT)
				.show();
	}
}
