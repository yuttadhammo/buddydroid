BuddyDroid
=================
This is a Buddypress client for Android. If configured properly, it will allow you to post status updates to your Buddypress install.  It also has a simple RSS reader for your sitewide activity feed where you can comment on, share and delete feed items.

This app is still in beta - please visit the support thread here:

http://buddypress.org/support/topic/buddydroid-buddypress-for-android/

to discuss.


Instructions
=============

Step 1: (Webmaster) Install the BuddyPress XML-RPC receiver plugin for Wordpress here: http://wordpress.org/extend/plugins/buddypress-xml-rpc-receiver/
Step 2: (Webmaster) Activate and configure above plugin via wp-admin under Settings->BuddyPress XML-RPC
Step 3: (App User) Add your website (root path, e.g. http://www.site.com/), username, and password to the Preferences screen in this app.

Once you have done these steps correctly, you should be able to interact with your BuddyPress activity stream.


Features
========
- posts updates to stream 
- view feed of latest updates with nested comments
- filter feed in various ways
- delete, comment on, and share posts via long-click on feed
- view user profiles by clicking on avatars
- view, delete, mark read/unread, and reply to private messages
- allows importing plain text / links from other apps
- option to check for and show Buddypress notifications in notification drawer

Technical Details
=================
Author: Yuttadhammo Bhikkhu <yuttadhammo@gmail.com>

This software is free and open source, under the GPL v3 license, for more info please visit: http://www.gnu.org/copyleft/gpl.html

Source code for this app is available on GitHub:

https://github.com/yuttadhammo/buddydroid

Changelog
=================

2.8
- notification bugfixes

2.7
- added clicks to friend avatar
- group creation
- better notification handling
- bug fixes

2.6
- groups and friends
- proper expanded list for filters

2.5
- grouped filters
- messaging
- bugfixes

2.4
- fixed home icon for compat
- moved filters to sidemenu
- added friending to user profiles

2.3
- moved user prefs to dedicated login/register screen

2.1
- added user profiles

2.0
- switched to password instead of api key - requires server update!
- bug fixes

1.8
- added messages to stream choices

1.7.1
- added content max pref
- refined filter bar hiding
- date fix and pref choice

1.7
- stream items fill space better
- crash fix

1.6 
- added EditText text transfer on rotate
- fixed text from share
- fixed sound in notification
- switched avatars to URLImageViewHelper class
- disappearing filter pane
- better older/smaller device compatibility

Credits
=================
- Wordpress, Buddypress, Android, et al
- Koushik Dutta for URLImageViewHelper
- Jake Wharton for ActionBarSherlock
- Jeremy Feinstein for SlidingMenu
