package com.example.pressure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.example.pressure.R.string;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View.OnClickListener;

public class MyStatistic extends FragmentActivity implements OnClickListener,
		LoaderCallbacks<Cursor> {

	private static final int CM_DELETE_ID = 0, CM_EDIT_ID = 1;
	long idCurrentName = 0;
	String formattedDate, currentPulse, currentSys, currentDias;
	
	MyDB db;
	Button btnAdd, btnSave, btnLoad, btnEmail;
	EditText etPulse, etSysPressure, etDiasPressure;
	static String profile_id;
	SimpleCursorAdapter scAdapter;
	String[] profile_name;

	ListView listStat;

	final String DIR_SD = "Pressure";
	final String FILENAME_SD = "pressure_stat";

	final String FILENAME = "Pressure_stat";

	Cursor cursor;
	String[] currentStat = new String[] { "", "", "" };
	final int DIALOG_STAT = 1;
	final String LOG_TAG = "myLogs";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistic);

		btnAdd = (Button) findViewById(R.id.btnAddStat);
		btnSave = (Button) findViewById(R.id.btnSave);
		btnEmail = (Button) findViewById(R.id.btnEmail);
		TextView name, e_mail;
		btnAdd.setOnClickListener(this);
		btnSave.setOnClickListener(this);

		db = new MyDB(this);
		db.open();

		// формируем столбцы сопоставления
		String[] from = new String[] { MyDB.COLUMN_PULSE,
				MyDB.COLUMN_SYS_PRESSURE, MyDB.COLUMN_DIAS_PRESSURE,
				MyDB.COLUMN_DATE };
		int[] to = new int[] { R.id.tvTextPulse, R.id.tvTextSys,
				R.id.tvTextDias, R.id.tvTextDate };

		name = (TextView) findViewById(R.id.profile_name);
		e_mail = (TextView) findViewById(R.id.profile_e_mail);
		profile_id = getIntent().getStringExtra("id_profile_key");

		profile_name = db.getCurrentName(Long.parseLong(profile_id));
		name.setText(profile_name[0]);
		e_mail.setText(profile_name[1]);

		// создааем адаптер и настраиваем список
		scAdapter = new SimpleCursorAdapter(this, R.layout.list, null, from,
				to, 0);
		listStat = (ListView) findViewById(R.id.listStat);
		registerForContextMenu(listStat);

		listStat.setAdapter(scAdapter);

		// // создаем лоадер для чтения данных
		getSupportLoaderManager().initLoader(0, null, this);
		// //Получаем текущее время
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy\nHH:mm");
		formattedDate = df.format(c.getTime());

		btnEmail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				final Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);

				emailIntent.setType("plain/text");
				// Кому
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						new String[] { profile_name[1].toString() });
				// Зачем
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"Pressure diary");
				// О чём
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hello, " + profile_name[0] + ", this is your statistic:");
				emailIntent.putExtra(
						android.content.Intent.EXTRA_STREAM,
						Uri.parse("file://"
								+ Environment.getExternalStorageDirectory()
								+ "/" + DIR_SD + "/" + FILENAME_SD));
				Log.d(LOG_TAG,
						"genm: " + Environment.getExternalStorageDirectory()
								+ "/" + DIR_SD + "/" + FILENAME_SD);

				// emailIntent.setType("text/video");
				// Поехали!
				MyStatistic.this.startActivity(Intent.createChooser(
						emailIntent, "Отправка письма..."));
			}
		});
	}

	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		if (id == DIALOG_STAT) {
			etPulse = (EditText) dialog.getWindow().findViewById(R.id.etPulse);
			etSysPressure = (EditText) dialog.getWindow().findViewById(
					R.id.etSysPressure);
			etDiasPressure = (EditText) dialog.getWindow().findViewById(
					R.id.etDiasPressure);
			etPulse.setText(currentStat[0]);
			etSysPressure.setText(currentStat[1]);
			etDiasPressure.setText(currentStat[2]);
		}
	}

	DialogInterface.OnClickListener myClickListenerStat = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			// положительная кнопка
			case Dialog.BUTTON_POSITIVE:
				if (idCurrentName != 0) {
					if (!(TextUtils.isDigitsOnly(etPulse.getText().toString()))
							|| !(TextUtils.isDigitsOnly(etSysPressure.getText()
									.toString()))
							|| !(TextUtils.isDigitsOnly(etDiasPressure
									.getText().toString()))
							|| (TextUtils.isEmpty(etPulse.getText().toString()))
							|| (TextUtils.isEmpty(etSysPressure.getText()
									.toString()))
							|| (TextUtils.isEmpty(etDiasPressure.getText()
									.toString()))) {
						correctData();
					} else {
						Log.d(LOG_TAG, "row inserted, id= " + idCurrentName);
						currentStat[0] = etPulse.getText().toString();
						currentStat[1] = etSysPressure.getText().toString();
						currentStat[2] = etDiasPressure.getText().toString();
						db.editStat(currentStat, String.valueOf(idCurrentName));
						getSupportLoaderManager().getLoader(0).forceLoad();
						idCurrentName = 0;
						saveData();
					}
				} else if (idCurrentName == 0) {
					Log.d(LOG_TAG, "row inserted, id= " + idCurrentName);
					if (!(TextUtils.isDigitsOnly(etPulse.getText().toString()))
							|| !(TextUtils.isDigitsOnly(etSysPressure.getText()
									.toString()))
							|| !(TextUtils.isDigitsOnly(etDiasPressure
									.getText().toString()))
							|| (TextUtils.isEmpty(etPulse.getText().toString()))
							|| (TextUtils.isEmpty(etSysPressure.getText()
									.toString()))
							|| (TextUtils.isEmpty(etDiasPressure.getText()
									.toString()))) {
						correctData();
					} else {
						db.addStat(etPulse.getText().toString(), etSysPressure
								.getText().toString(), etDiasPressure.getText()
								.toString(), profile_id, formattedDate);
						idCurrentName = 0;
						getSupportLoaderManager().getLoader(0).forceLoad();
						addData();
					}
				}
				break;
			// нейтральная кнопка
			case Dialog.BUTTON_NEUTRAL:
				break;
			}
		}
	};

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CM_EDIT_ID, 0, R.string.edit_record);
		menu.add(0, CM_DELETE_ID, 0, R.string.delete_record);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getItemId() == CM_DELETE_ID) {
			db.delRecStat(acmi.id);
			// получаем новый курсор с данными
			getSupportLoaderManager().getLoader(0).forceLoad();
			deleteData();
			return true;
		} else if (item.getItemId() == CM_EDIT_ID) {
			currentStat = db.getCurrentStat(acmi.id);
			idCurrentName = acmi.id;
			Log.d(LOG_TAG, "row inserted, id= " + idCurrentName);
			Log.d(LOG_TAG, "row inserted, pulse = " + currentStat[0]);
			Log.d(LOG_TAG, "row inserted, sys= " + currentStat[1]);
			Log.d(LOG_TAG, "row inserted, dias= " + currentStat[2]);
			showDialog(DIALOG_STAT);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public void onClickBack(View v) {
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		finish();
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		if (id == DIALOG_STAT) {

			LinearLayout view = (LinearLayout) getLayoutInflater().inflate(
					R.layout.dialog_stat, null);
			// устанавливаем ее, как содержимое тела диалога
			adb.setView(view);

			// кнопка положительного ответа
			adb.setPositiveButton(R.string.yes, myClickListenerStat);
			// кнопка нейтрального ответа
			adb.setNeutralButton(R.string.cancel, myClickListenerStat);

			Dialog dialog = adb.create();
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onClick(View v) {
		Log.d(LOG_TAG, "row inserted, id= " + idCurrentName);
		switch (v.getId()) {
		case R.id.btnAddStat:
			showDialog(DIALOG_STAT);
			etSysPressure.setText("");
			etDiasPressure.setText("");
			etPulse.setText("");
			break;
		case R.id.btnSave:
			writeFileSD(profile_name[0]);
			break;
		}
	}

	void writeFileSD(String path) {
		// проверяем доступность SD
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Log.d(LOG_TAG,
					"SD-card is no available: "
							+ Environment.getExternalStorageState());
			return;
		}
		// получаем путь к SD
		File sdPath = Environment.getExternalStorageDirectory();
		// добавляем свой каталог к пути
		sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
		// создаем каталог
		sdPath.mkdirs();
		// формируем объект File, который содержит путь к файлу
		File sdFile = new File(sdPath, FILENAME_SD + "_for_" + path + ".txt");

		ObjectOutputStream out;
		Object[] objs = new Object[listStat.getCount()];
		String[] name = new String[] { "", "", "", "", "" };

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
			for (int i = 0; i < listStat.getCount(); i++) {
				Cursor cur = (Cursor) listStat.getAdapter().getItem(i);
				long temp = cur.getLong(cur.getColumnIndex("_id"));
				name = db.getCurrentStat(temp);
				bw.write("Date: ");
				bw.write(name[4] + "\n");
				bw.write("Pulse: ");
				bw.write(name[0] + "\n");
				bw.write("Sys. Pressure: ");
				bw.write(name[1] + "\n");
				bw.write("Dias. Pressure: ");
				bw.write(name[2] + "\n");
				bw.write("-------------\n");
			}
			bw.close();
			saveOnSD();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readFile() {
		try {
			// открываем поток для чтения
			BufferedReader br = new BufferedReader(new InputStreamReader(
					openFileInput(FILENAME)));
			String str = "";
			// читаем содержимое
			while ((str = br.readLine()) != null) {
				Log.d(LOG_TAG, str);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void onDestroy() {
		db.close();
		super.onDestroy();
		// закрываем подключение при выходе
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
		return new MyCursorLoader(this, db);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		scAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	static class MyCursorLoader extends CursorLoader {

		MyDB db;

		public MyCursorLoader(Context context, MyDB db) {
			super(context);
			this.db = db;
		}

		@Override
		public Cursor loadInBackground() {
			Cursor cursor = db.getAllDataStat(profile_id);
			return cursor;
		}
	}

	void saveData() {
		Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
	}

	void addData() {
		Toast.makeText(this, R.string.add, Toast.LENGTH_SHORT).show();
	}

	void deleteData() {
		Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
	}

	void correctData() {
		Toast.makeText(this, R.string.correct, Toast.LENGTH_SHORT).show();
	}
	
	void saveOnSD() {
		Toast.makeText(this, R.string.save_on_SD, Toast.LENGTH_SHORT).show();
	}
}