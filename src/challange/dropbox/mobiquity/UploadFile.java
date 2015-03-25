package challange.dropbox.mobiquity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

//This activity performs the functionality of uploading the image file into the desired Dropbox folder
//I have used AsyncTask that will allow the application to run this task in background, to improve performance
//I have also used Progressbar to show the progress of the image that is being uploaded
//This activity also shows number of exceptions that are handled

public class UploadFile extends AsyncTask<Void, Long, Boolean> 
{
	private DropboxAPI<?> dropboxApi;
	private String dropboxPath;
	private File file;
	private long fileLength;
	private UploadRequest uploadRequest;
	private Context context;
	private ProgressDialog processDialog;
	private String errorMsg;

	public UploadFile(Context context, DropboxAPI<?> dropboxApi, String dropboxPath, File file) 
	{
      this.context = context;
        fileLength = file.length();
        this.dropboxApi = dropboxApi;
        this.dropboxPath = dropboxPath;
        this.file = file;

        processDialog = new ProgressDialog(context);
        processDialog.setMax(100);
        processDialog.setMessage("Uploading " + file.getName());
        processDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        processDialog.setProgress(0);
      
       processDialog.setButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // This will cancel the putFile operation
            	uploadRequest.abort();
            }
        });
        processDialog.show();
    }

	@Override
	protected Boolean doInBackground(Void... params) 
	{
		try 
		{
			FileInputStream fis = new FileInputStream(file);
			String path = dropboxPath + file.getName();
			uploadRequest = dropboxApi.putFileOverwriteRequest(path, fis, file.length(),
					new ProgressListener() 
			{
						@Override
						public long progressInterval() 
						{
							// Update the progress bar every half-second or so
							return 500;
						}

						@Override
						public void onProgress(long bytes, long total) 
						{
							publishProgress(bytes);
						}
			});

			if (uploadRequest != null) 
			{
				uploadRequest.upload();
				return true;
			}

		} 
		catch (DropboxUnlinkedException e) 
		{
			// This session wasn't authenticated properly or user unlinked
			errorMsg = "The app is not authenticated properly.";
		} 
		catch (DropboxFileSizeException e) 
		{
			// File size too big to upload via the API
			errorMsg = "This file is too big to upload";
		} 
		catch (DropboxPartialFileException e) 
		{
			// User canceled the operation
			errorMsg = "Upload canceled";
		} 
		catch (DropboxServerException e)
		{
			// Server-side exception. These are examples of what could happen,
			// but I din't do anything special with them here.
			if (e.error == DropboxServerException._401_UNAUTHORIZED) 
			{
			// Unauthorized, so should be unlinked. We may want to automatically log the user out in this case.
			} 
			else if (e.error == DropboxServerException._403_FORBIDDEN) 
			{
				// Not allowed to access this
			} 
			else if (e.error == DropboxServerException._404_NOT_FOUND) 
			{
				// path not found (or if it was the thumbnail, can't be thumbnailed)
			} 
			else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) 
			{
				// user is over quota
			} 
			else 
			{
				// Something else
			}
			// This gets the Dropbox error, translated into the user's language
			
			errorMsg = e.body.userError;
			if (errorMsg == null) 
			{
				errorMsg = e.body.error;
			}
		} 
		catch (DropboxIOException e) 
		{
			e.printStackTrace();
			errorMsg = "Network error.  Try again.";
		} 
		catch (DropboxParseException e) 
		{
			// Probably due to Dropbox server restarting, should retry
			errorMsg = "Dropbox error.  Try again.";
		} 
		catch (DropboxException e) 
		{
			// Unknown error
			errorMsg = "Unknown error.  Try again.";
		} 
		catch (FileNotFoundException e) 
		{
		}
		return false;
	}

	@Override
	protected void onProgressUpdate(Long... progress) 
	{
		int percentProgress = (int) (100.0 * (double) progress[0] / fileLength + 0.5);
		processDialog.setProgress(percentProgress);
	}

	@Override
	protected void onPostExecute(Boolean result) 
	{
		processDialog.dismiss();
		if (result) 
		{
			showToast("Successfully uploaded");
		} 
		else 
		{
			showToast(errorMsg);
		}
	}

	private void showToast(String msg) 
	{
		Toast error = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		error.show();
	}
}
