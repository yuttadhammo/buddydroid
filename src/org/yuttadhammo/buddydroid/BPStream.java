package org.yuttadhammo.buddydroid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.yuttadhammo.buddydroid.BPStatus.uploadStatusTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class BPStream {

	private long id;
	private int blogID;
	private String categories;
	private String custom_fields;
	private long dateCreated;
	private long date_created_gmt;
	private String description;
	private String link;
	private boolean mt_allow_comments;
	private boolean mt_allow_pings;
	private String mt_excerpt;
	private String mt_keywords;
	private String mt_text_more;
	private String permaLink;
	private String post_status;
	private String postid;
	private String title;
	private String userid;
	private String wp_author_display_name;
	private String wp_author_id;
	private String wp_password;
	private String wp_post_format;
	private String wp_slug;
	private boolean localDraft;
	private boolean uploaded;
	private double latitude;
	private double longitude;
	private boolean isPage;
	private boolean isLocalChange;

	private String mediaPaths;
	private static Context context;
	private static Buddypress activity;

	public Vector<String> imageUrl = new Vector<String>();
	Vector<String> selectedCategories = new Vector<String>();
	private static String scope;
	private static int max;


	public BPStream(Buddypress atv, String ascope, int amax) {
		
		activity = atv;
		scope = ascope;
		max = amax;
	}

	private static ProgressDialog downloadProgressDialog;

	public static class getStreamTask extends
			AsyncTask<BPStream, Boolean, Boolean> {

		String error = "";
		boolean mediaError = false;
		private Object toast;
		private boolean success = false;
		private Object rss;

		@Override
		protected void onPostExecute(Boolean result) {
			if(downloadProgressDialog.isShowing()) {
				downloadProgressDialog.dismiss();
			}
			Toast.makeText(activity, (CharSequence) toast,
					Toast.LENGTH_SHORT).show();
			if (success) {
				activity.onRefreshStream((HashMap<?, ?>) rss);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
	        downloadProgressDialog = new ProgressDialog(activity);
	        downloadProgressDialog.setCancelable(true);
	        downloadProgressDialog.setMessage(activity.getString(R.string.updating));
	        downloadProgressDialog.setIndeterminate(true);
	        downloadProgressDialog.show();
		}

		@Override
		protected Boolean doInBackground(BPStream... streams) {

			Object[] params;

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("scope", scope);
			data.put("max", max);

			params = new Object[] { Buddypress.getUsername(),
					Buddypress.getServiceName(),
					Buddypress.getApiKey(), data };
			Log.e("BP", "api key" + params[2]);
			XMLRPCClient client = new XMLRPCClient(Buddypress.getUrl(),
					Buddypress.getHttpuser(), Buddypress.getHttppassword());
			try {
				rss = client.call("bp.getActivity", params);
				toast = activity.getString(R.string.posted);
				success = true;
				return true;
			} catch (final XMLRPCException e) {
				toast = e.toString();
				e.printStackTrace();
			}
			return false;
		}

	}

	public long getId() {
		return id;
	}

	public long getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(long dateCreated) {
		this.dateCreated = dateCreated;
	}

	public long getDate_created_gmt() {
		return date_created_gmt;
	}

	public void setDate_created_gmt(long dateCreatedGmt) {
		date_created_gmt = dateCreatedGmt;
	}

	public int getBlogID() {
		return blogID;
	}

	public void setBlogID(int blogID) {
		this.blogID = blogID;
	}

	public boolean isLocalDraft() {
		return localDraft;
	}

	public void setLocalDraft(boolean localDraft) {
		this.localDraft = localDraft;
	}

	public JSONArray getCategories() {
		JSONArray jArray = null;
		if (categories == null)
			categories = "";
		try {
			jArray = new JSONArray(categories);
		} catch (JSONException e) {
		}
		return jArray;
	}

	public void setCategories(JSONArray categories) {
		this.categories = categories.toString();
	}

	public JSONArray getCustom_fields() {
		JSONArray jArray = null;
		try {
			jArray = new JSONArray(custom_fields);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jArray;
	}

	public void setCustom_fields(JSONArray customFields) {
		custom_fields = customFields.toString();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public boolean isMt_allow_comments() {
		return mt_allow_comments;
	}

	public void setMt_allow_comments(boolean mtAllowComments) {
		mt_allow_comments = mtAllowComments;
	}

	public boolean isMt_allow_pings() {
		return mt_allow_pings;
	}

	public void setMt_allow_pings(boolean mtAllowPings) {
		mt_allow_pings = mtAllowPings;
	}

	public String getMt_excerpt() {
		return mt_excerpt;
	}

	public void setMt_excerpt(String mtExcerpt) {
		mt_excerpt = mtExcerpt;
	}

	public String getMt_keywords() {
		if (mt_keywords == null)
			return "";
		else
			return mt_keywords;
	}

	public void setMt_keywords(String mtKeywords) {
		mt_keywords = mtKeywords;
	}

	public String getMt_text_more() {
		if (mt_text_more == null)
			return "";
		else
			return mt_text_more;
	}

	public void setMt_text_more(String mtTextMore) {
		mt_text_more = mtTextMore;
	}

	public String getPermaLink() {
		return permaLink;
	}

	public void setPermaLink(String permaLink) {
		this.permaLink = permaLink;
	}

	public String getPost_status() {
		return post_status;
	}

	public void setPost_status(String postStatus) {
		post_status = postStatus;
	}

	public String getPostid() {
		return postid;
	}

	public void setPostid(String postid) {
		this.postid = postid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getWP_author_display_name() {
		return wp_author_display_name;
	}

	public void setWP_author_display_name(String wpAuthorDisplayName) {
		wp_author_display_name = wpAuthorDisplayName;
	}

	public String getWP_author_id() {
		return wp_author_id;
	}

	public void setWP_author_id(String wpAuthorId) {
		wp_author_id = wpAuthorId;
	}

	public String getWP_password() {
		return wp_password;
	}

	public void setWP_password(String wpPassword) {
		wp_password = wpPassword;
	}

	public String getWP_post_format() {
		return wp_post_format;
	}

	public void setWP_post_form(String wpPostForm) {
		wp_post_format = wpPostForm;
	}

	public String getWP_slug() {
		return wp_slug;
	}

	public void setWP_slug(String wpSlug) {
		wp_slug = wpSlug;
	}

	public String getMediaPaths() {
		return mediaPaths;
	}

	public void setMediaPaths(String mediaPaths) {
		this.mediaPaths = mediaPaths;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public boolean isPage() {
		return isPage;
	}

	public void setPage(boolean isPage) {
		this.isPage = isPage;
	}

	public boolean isUploaded() {
		return uploaded;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}

	public void get() {

		new getStreamTask().execute(this);

	}

	public boolean isLocalChange() {
		return isLocalChange;
	}

	public void setLocalChange(boolean isLocalChange) {
		this.isLocalChange = isLocalChange;
	}

}
