package challange.dropbox.mobiquity;

import java.util.ArrayList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

//This activity is used to return the list of files from the desired directory
//I have used AsyncTask that will allow the application to run this task in background, to improve performance

public class FileList extends AsyncTask<Void, Void, ArrayList<String>> 
	{
		private DropboxAPI<?> mApi;
		private String path;
		private Handler handler;
	   
	    public FileList(DropboxAPI<?> mApi, String path, Handler handler) 
	    {
	    	this.mApi = mApi;
	    	this.path = path;
	    	this.handler = handler;
	     }
	    
	 @Override
	 	protected ArrayList<String> doInBackground(Void... params) 
	 	{
		 	ArrayList<String> files = new ArrayList<String>();
		 	try 
		 	{
		 		Entry directory = mApi.metadata(path, 1000, null, true, null);

	            for (Entry entry : directory.contents) 
	            {
	            	files.add(entry.fileName());
	            }
	        } 
		 	catch (DropboxException e) 
		 	{
		 		e.printStackTrace();
		 	}
	
	     return files;
	 	}
	
	 	protected void onPostExecute(ArrayList<String> result) 
	 	{
	 		Message message = handler.obtainMessage();
	 		Bundle bundle = new Bundle();
	 		bundle.putStringArrayList("foldernames", result);
	 		message.setData(bundle);
	 		handler.sendMessage(message);
	    }
	}

	

	

