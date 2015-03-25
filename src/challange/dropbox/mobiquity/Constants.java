package challange.dropbox.mobiquity;


import com.dropbox.client2.session.Session.AccessType;

//This activity defines all the important parameters that will allow the user to connect the android device
//to his/her Dropbox account

class Constants 
	{
	public static final String OVERRIDEMSG = "File name with this name already exists.Do you want to replace this file?";
	final static public String DROPBOX_APP_KEY = "jzo059atbh90psf";
	final static public String DROPBOX_APP_SECRET = "9gmutp6rq1qclkd";

	final static public AccessType ACCESS_TYPE = AccessType.DROPBOX;

	final static public String ACCOUNT_PREFS_NAME = "preferences";
	final static public String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static public String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	}
