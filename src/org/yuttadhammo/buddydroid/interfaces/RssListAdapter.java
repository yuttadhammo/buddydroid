package org.yuttadhammo.buddydroid.interfaces;

import org.yuttadhammo.buddydroid.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RssListAdapter extends ArrayAdapter<Object> {

	protected String TAG = "RssListAdapter";
	public SparseIntArray expanded = new SparseIntArray();
	private RssListAdapter tclass;
	
	public RssListAdapter(Activity activity, Object[] rss) {
		super(activity, 0, rss);
		tclass = this;
	}


	@SuppressLint("NewApi")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final Activity activity = (Activity) getContext();
		LayoutInflater inflater = activity.getLayoutInflater();

		// Inflate the views from XML
		View rowView = inflater.inflate(R.layout.stream_item, null);
		final HashMap<?,?> entryMap = (HashMap<?, ?>) getItem(position);
		
		TextView titleView = (TextView) rowView.findViewById(R.id.title);
		TextView textView = (TextView) rowView.findViewById(R.id.text);
		TextView dateView = (TextView) rowView.findViewById(R.id.date);
        try {
        	String text = sanitizeText((String)entryMap.get("content"));
        	text = text.replace("\n", "<br/>");
        	String title = (String)entryMap.get("action");
        	
        	String dates = (String)entryMap.get("date_recorded");
        	
        	int comments = 0;
        	
        	if(entryMap.containsKey("children") && entryMap.get("children") instanceof HashMap) {
        			
        		HashMap<?,?> chm = (HashMap<?,?>)entryMap.get("children");
        		comments = chm.entrySet().size();
				Log.i(TAG,comments+" comments");

        		final LinearLayout commentPane = (LinearLayout) rowView.findViewById(R.id.comment_pane);
        		commentPane.setVisibility(View.VISIBLE);
        		Map<String,LinearLayout> tva = makeCommentLayout(chm);
        				
        		
        		for (Iterator<?> it = tva.keySet().iterator(); it.hasNext();) {
    				String key = (String) it.next();
    				LinearLayout comment = tva.get(key);
        			commentPane.addView(comment);
        		}
        	}

        	// load image
        	
        	String imgurl = (String)entryMap.get("user_avatar");
        	imgurl = imgurl.replaceAll(".*src=\"([^\"]*)\".*","$1");
        	
        	ImageView iv = (ImageView) rowView.findViewById(R.id.feed_image);
        	UrlImageViewHelper.setUrlDrawable(iv, imgurl);

        	// add text content if exists
        	
        	if(text.replaceAll("<[^>]*>", "").length() > 0) {
            	Spanned out = Html.fromHtml(text);
        		textView.setText(out);
    			textView.setMovementMethod(LinkMovementMethod.getInstance());
    			title = title + ":";
        	}
    		else
        		textView.setVisibility(View.GONE);

        	// add title
        	
        	Spanned titleSpan = Html.fromHtml(title);
        	titleView.setText(titleSpan);
        	titleView.setMovementMethod(LinkMovementMethod.getInstance());

        	// add date
        	//2013-03-11 20:32:01
        	
        	Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH).parse(dates);
        	//DateFormat df = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH);
        	
        	CharSequence dateString = DateUtils.getRelativeTimeSpanString(date.getTime());      	
        	dateView.setText(dateString);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
		return rowView;

	}


	private Map<String, LinearLayout> makeCommentLayout(HashMap<?, ?> chm) {
		
		Map<String,LinearLayout> tva = new TreeMap<String,LinearLayout>();
		
		final Activity activity = (Activity) getContext();
		LayoutInflater inflater = activity.getLayoutInflater();

		
		for (Iterator<?> it = chm.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			final HashMap<?,?> comment = (HashMap<?,?>) chm.get(key);
			LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.comment_shell, null);
			TextView tv = (TextView) inflater.inflate(R.layout.comment, null);
			if(comment.containsKey("content")) {
				String commentString = sanitizeText((String) comment.get("content"));
				
				int cl = commentString.length();
				String commentAuthor = "- <a href=\""+comment.get("primary_link")+"\">" + (String) comment.get("display_name")+"</a>";
				Spanned commentAuthorSpan = Html.fromHtml(commentAuthor);
				SpannedString styledComment = new SpannedString(commentString+"\n");
				styledComment = (SpannedString) TextUtils.concat(styledComment,commentAuthorSpan);
				SpannableString outcomment = new SpannableString(styledComment);
				outcomment.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_OPPOSITE),
						cl + 1, cl + 1 + commentAuthorSpan.length(), 
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				outcomment.setSpan(new StyleSpan(Typeface.ITALIC),
						cl + 1, cl + 1 + commentAuthorSpan.length(), 
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				tv.setText(outcomment );
			}
			else {
				continue;
			}
			ll.addView(tv);
			// iterate
			
			if(comment.containsKey("children") && comment.get("children") instanceof HashMap) {
				HashMap<?,?> chmc = (HashMap<?,?>)comment.get("children");
				Map<String,LinearLayout> tvac = makeCommentLayout(chmc);
        		for (Iterator<?> it2 = tvac.keySet().iterator(); it2.hasNext();) {
    				String keyc = (String) it2.next();
    				LinearLayout commentc = tvac.get(keyc);
        			ll.addView(commentc);
        		}				
			}
			tva.put((String) comment.get("id"), ll);
		}

		return tva;
	}


	private String sanitizeText(String string) {
		string = string.replace("\\\"", "\"").replace("\\'", "'");
		return string;
	} 

}