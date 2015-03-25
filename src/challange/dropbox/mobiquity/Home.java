package challange.dropbox.mobiquity;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;

//This is the home page of the application
//This activity displays the options provided to the user
//Also responsible for creating the session and authentication
//Displays all the uploaded files on this page only

public class Home extends Activity implements OnClickListener 
	{
	private static final int TAKE_PHOTO = 1;
	private Button buttonUpload, buttonListFiles, buttonLogin;
	private final String DIR = "/Mobiquity/";
	private File fileToUpload;
	private boolean check, onResume;
	private DropboxAPI<AndroidAuthSession> dropboxApi;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		AndroidAuthSession session = createSession();
		dropboxApi = new DropboxAPI<AndroidAuthSession>(session);
		
		isLoggedIn(false);
		
		buttonListFiles = (Button) findViewById(R.id.btnListFiles);
		buttonLogin = (Button) findViewById(R.id.loginBtn);
		buttonUpload = (Button) findViewById(R.id.btnUploadPhoto);
		
		buttonUpload.setOnClickListener(this);
		buttonListFiles.setOnClickListener(this);
		buttonLogin.setOnClickListener(this);

	}

	private AndroidAuthSession createSession() 
	{
		AppKeyPair appKeyPair = new AppKeyPair(Constants.DROPBOX_APP_KEY, Constants.DROPBOX_APP_SECRET);
		AndroidAuthSession session;

		String[] storedKeys = getKeys();
		if (storedKeys != null) 
		{
			AccessTokenPair accessToken = new AccessTokenPair(storedKeys[0], storedKeys[1]);
			session = new AndroidAuthSession(appKeyPair, Constants.ACCESS_TYPE, accessToken);
		} 
		else 
		{
			session = new AndroidAuthSession(appKeyPair, Constants.ACCESS_TYPE);
		}

		return session;
	}

	private String[] getKeys() 
	{
		SharedPreferences preferences = getSharedPreferences(Constants.ACCOUNT_PREFS_NAME, 0);
		String key = preferences.getString(Constants.ACCESS_KEY_NAME, null);
		String secret = preferences.getString(Constants.ACCESS_SECRET_NAME, null);
		
		if (key != null && secret != null) 
		{
			String[] cred = new String[2];
			cred[0] = key;
			cred[1] = secret;
			return cred;
		} 
		else 
		{
			return null;
		}
			
	}

    private final Handler handler = new Handler() 
    {
    	public void handleMessage(Message message) 
    	{
    		ArrayList<String> result = message.getData().getStringArrayList("foldernames");
            TextView textView = (TextView) findViewById(R.id.listFiles1);
            
            for (String fileName : result) 
            {
            	textView.append(fileName+"\n");
            }
        }
    };
    
	@Override
	public void onClick(View v) 
	{
		switch(v.getId())
		{
		//Login button functionality
		case R.id.loginBtn:
			if (check) 
				{
					logOut();
					check=false;	
				}
			else
				{
					dropboxApi.getSession().startAuthentication(Home.this);
					check=true;
					buttonUpload.setEnabled(true);
					buttonListFiles.setEnabled(true);
					buttonLogin.setText("Logout");
				}
			break;
			
		//Upload button functionality
		case R.id.btnUploadPhoto:
				createDir();
				
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				fileToUpload = new File(Utils.getPath(),new Date().getTime()+".jpg");
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileToUpload));
				startActivityForResult(intent, TAKE_PHOTO);
			break;
		
		//List files button functionality
		case R.id.btnListFiles:
				 FileList fileList = new FileList(dropboxApi, DIR, handler);
		         fileList.execute(); 
		     
		         break;
		
		         //Does nothing	  
		default:
			break;
		}
	}	 
	
	private void logOut() 
	{
		dropboxApi.getSession().unlink();
		clearKeys();
		buttonUpload.setEnabled(false);
		buttonListFiles.setEnabled(false);
		buttonLogin.setText("Login");
	}

	private void clearKeys() 
	{
		SharedPreferences preferences = getSharedPreferences(Constants.ACCOUNT_PREFS_NAME, 0);
		Editor edit = preferences.edit();
		edit.clear();
		edit.commit();
	}

	private void createDir() 
	{
		File directory = new File(Utils.getPath());
		if (!directory.exists()) 
		{
			directory.mkdir();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) 
		{
			if (requestCode == TAKE_PHOTO) 
			{
				if (Utils.isOnline(Home.this)) 
				{
					onResume = true;
				} 
				else
				{
					Utils.showNetworkAlert(Home.this);
				}
			}
		}
	}

	public void isLoggedIn(boolean loggedIn) 
	{
		check = loggedIn;
		 
		if (check) 
		{
			UploadFile upload = new UploadFile(Home.this, dropboxApi, DIR, fileToUpload);
			upload.execute();
			onResume = false;
		}
	}

	private void storeKeys(String key, String secret) 
	{
		SharedPreferences preferences = getSharedPreferences(Constants.ACCOUNT_PREFS_NAME, 0);
		Editor edit = preferences.edit();
		edit.putString(Constants.ACCESS_KEY_NAME, key);
		edit.putString(Constants.ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	private void showToast(String msg) 
	{
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}

	@Override
	protected void onResume() 
	{
		AndroidAuthSession session = dropboxApi.getSession();

		if (session.authenticationSuccessful()) 
		{
			try 
			{
				session.finishAuthentication();
				TokenPair tokens = session.getAccessTokenPair();
				storeKeys(tokens.key, tokens.secret);
				isLoggedIn(onResume);
			} 
			catch (IllegalStateException e) 
			{
				showToast("Authentication with Dropbox failed: " + e.getLocalizedMessage());
			}
		}
		super.onResume();
	}
}
