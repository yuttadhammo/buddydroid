package org.yuttadhammo.buddydroid.interfaces;

import org.yuttadhammo.buddydroid.R;

public class BPStrings {
	
	public static final int SITEWIDE = 1;
	public static final int ACTIVITY_UPDATE = 2;
	public static final int FAVORITES = 3;
	public static final int NEW_MEMBER = 4;
	public static final int JUST_ME = 5;
	public static final int MENTIONS = 6;
	public static final int GROUPS = 7;
	public static final int MY_GROUPS = 8;
	public static final int NEW_GROUP = 9;
	public static final int FRIENDS = 10;
	public static final int FRIEND_REQUESTS = 11;
	public static final int INBOX = 12;
	public static final int SENTBOX = 13;
	public static final int COMPOSE = 14;

	public static final Integer[] ACTIVITIES = {SITEWIDE,ACTIVITY_UPDATE,FAVORITES,NEW_MEMBER,JUST_ME,MENTIONS};
	public static final Integer[] GROUPS_ARRAY = {GROUPS,MY_GROUPS,NEW_GROUP};
	public static final Integer[] FRIENDS_ARRAY = {FRIENDS,FRIEND_REQUESTS};
	public static final Integer[] MESSAGES_ARRAY = {INBOX,SENTBOX,COMPOSE};
	
	public static final int NOTIFICATIONS = 15;
	
	public static int getFilterDisplayString(int filter) {
		int string = 0;
		
		switch(filter) {
			
			case SITEWIDE:
				return R.string.sitewide;
			case ACTIVITY_UPDATE:
				return R.string.activity_update;
			case FAVORITES:
				return R.string.favorites;
			case NEW_MEMBER:
				return R.string.new_member;
			case JUST_ME:
				return R.string.just_me;
			case MENTIONS:
				return R.string.mentions;
			case GROUPS:
				return R.string.groups;
			case MY_GROUPS:
				return R.string.my_groups;
			case NEW_GROUP:
				return R.string.new_group;
			case FRIENDS:
				return R.string.friends;
			case FRIEND_REQUESTS:
				return R.string.friend_requests;
			case INBOX:
				return R.string.inbox;
			case SENTBOX:
				return R.string.sentbox;
			case COMPOSE:
				return R.string.compose;
			case NOTIFICATIONS:
				return R.string.notifications;
		}
		
		return string;
	}
	
	public static String getFilterRequestString(int filter) {
		String string = "";
		
		switch(filter) {
			case SITEWIDE:
				return "sitewide";
			case ACTIVITY_UPDATE:
				return "activity_update";
			case FAVORITES:
				return "favorites";
			case NEW_MEMBER:
				return "new_member";
			case JUST_ME:
				return "just_me";
			case MENTIONS:
				return "mentions";
			case GROUPS:
				return "groups";
			case MY_GROUPS:
				return "my_groups";
			case NEW_GROUP:
				return "new_group";
			case FRIENDS:
				return "friends";
			case FRIEND_REQUESTS:
				return "friend_requests";
			case INBOX:
				return "inbox";
			case SENTBOX:
				return "sentbox";
			case COMPOSE:
				return "compose";
		}
		
		return string;
	}
}
