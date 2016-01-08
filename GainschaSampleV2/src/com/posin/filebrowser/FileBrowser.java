package com.posin.filebrowser;

import android.app.Activity;
import android.app.ListActivity;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import com.gainscha.sample.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class FileBrowser extends ListActivity {
	
	private final static String TAG = "Posin.FileBrowser";

	private List<String> mItems = null;
	private List<String> mPaths = null;
	private String mRootPath = "/mnt";
	private String mCurrentSelection = null;
	private TextView mPath;
	private FileFilter mFilter = null;
	private boolean mChooseFile = true;
	private Button mBtnConfirm;

	//private boolean mShowHidden = false;
	private boolean mShowHidden = true;
	
	static final String KEY_FILE_EXT = "file_ext";
	static final String KEY_FILTER_CLASS = "filter_class";
	static final String KEY_CHOOSE_FILE = "choose_file";
	static final String KEY_SHOW_HIDDEN = "show_hidden";
	
	public static final int ACTIVITY_CODE = R.layout.fileselect;
	
	public static void chooseFile(Activity context, String fileExt, boolean chooseFile, boolean showHidden) {
		Intent i = new Intent(context, FileBrowser.class);
		if(fileExt != null)
			i.putExtra(KEY_FILE_EXT, fileExt);
		i.putExtra(KEY_CHOOSE_FILE, chooseFile);
		i.putExtra(KEY_SHOW_HIDDEN, showHidden);
		context.startActivityForResult(i, ACTIVITY_CODE);
	}
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fileselect);

		Intent i = getIntent();
		if(i.hasExtra(KEY_FILTER_CLASS)) {
			String cls = i.getStringExtra(KEY_FILTER_CLASS);
			try {
				mFilter = (FileFilter) Class.forName(cls).newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(i.hasExtra(KEY_FILE_EXT))
			mFilter = new FileExtFilter( i.getStringExtra(KEY_FILE_EXT));
		
		if(i.hasExtra(KEY_SHOW_HIDDEN))
			mShowHidden = i.getBooleanExtra(KEY_SHOW_HIDDEN, mShowHidden);
		
		if(i.hasExtra(KEY_CHOOSE_FILE))
			mChooseFile = i.getBooleanExtra(KEY_CHOOSE_FILE, true);
		
		mPath = (TextView) findViewById(R.id.mPath);

		mBtnConfirm = (Button) findViewById(R.id.buttonConfirm);
		mBtnConfirm.setEnabled(!mChooseFile);
		mBtnConfirm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//returnSelection(curPath);
				returnSelection(mCurrentSelection);
			}
		});

		Button buttonCancle = (Button) findViewById(R.id.buttonCancle);
		buttonCancle.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		getFileDir(mRootPath);
	}

	class FileExtFilter implements FileFilter {
		String mExt;
		
		FileExtFilter(String ext) {
			mExt = ext;
		}
		
		@Override
		public boolean accept(File pathname) {
			if(!mShowHidden && pathname.isHidden())
				return false;

			if(pathname.isDirectory()) {
				return true;
			}
			//Log.d(TAG, "file : " + pathname.getName());

			if(pathname.getName().endsWith(mExt)) {
				return true;
			}
			return false;
		}
	}

	void sortInsert(List<File> lst, int start, File f) {
		for (int i = start; i < lst.size(); i++) {
			if (lst.get(i).getName().compareTo(f.getName())>=0) {
				lst.add(i, f);
				return;
			}
		}
		lst.add(f);
	}

	File[] sortFiles(File[] fs) {
		if(fs == null)
			return null;
		
		ArrayList<File> lst = new ArrayList<File>();
		for (int i = 0; i < fs.length; i++)
			if (fs[i].isDirectory())
				sortInsert(lst, 0, fs[i]);
		int start = lst.size();
		for (int i = 0; i < fs.length; i++)
			if (!fs[i].isDirectory())
				sortInsert(lst, start, fs[i]);
		
		if(lst.size() != 0)
			return lst.toArray(fs);
		return null;
	}

	private void getFileDir(String filePath) {

		final String currPath = getResources().getString(R.string.strCurrentPath);

		mPath.setText(currPath + filePath);
		mItems = new ArrayList<String>();
		mPaths = new ArrayList<String>();
		File f = new File(filePath);
		File[] files;
		
		if(mFilter != null) {
			files = sortFiles(f.listFiles(mFilter));
		}
		else
			files = sortFiles(f.listFiles());

		if (!filePath.equals(mRootPath)) {
			mItems.add("b1");
			mPaths.add(mRootPath);
			mItems.add("b2");
			mPaths.add(f.getParent());
		}
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				mItems.add(file.getName());
			//	curPath=file.getName();
				mPaths.add(file.getPath());
			}
		}
		setListAdapter(new FileBrowserAdapter(this, mItems, mPaths));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		File file = new File(mPaths.get(position));
		mCurrentSelection = file.getAbsolutePath();

		if (file.isDirectory()) {

			getFileDir(mPaths.get(position));
			
			mBtnConfirm.setEnabled(!mChooseFile);

		} else {
			mBtnConfirm.setEnabled(true);
		}
	}
	
	void returnSelection(String path){
		Intent data = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("file", path);
		data.putExtras(bundle);
		setResult(Activity.RESULT_OK, data);
		finish();
	}
}
