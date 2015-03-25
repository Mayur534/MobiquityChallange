package challange.dropbox.mobiquity;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;

public class listFile
{
	 private DropboxAPI<?> mApi;
		ArrayList<String> folderName=new ArrayList<String>();
		
	 public listFile(DropboxAPI<?> mApi)
	 {
		 this.mApi = mApi;
	 }
	 

public  ArrayList<String> list()
{
	try{ 
	Log.d("1", "In lists");
		    Entry dropboxDir1 = mApi.metadata("/Mobiquity/", 1, null, true, null);    
		  
		       
		       
		    for (Entry entry : dropboxDir1.contents) {
		    	
                folderName.add(entry.fileName());
                Log.d("1", entry.fileName());
            	
            }
		          
	
		     }catch (Exception ex) {
		                
		                    
		           }
	return folderName;
}
}
