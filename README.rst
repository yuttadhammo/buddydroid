BuddyDroid
=================
This is a very simple Buddypress client. If configured properly, it will allow you to post status updates to your Buddypress install.  It also has a simple RSS reader for your sitewide activity feed.

Instructions
=============
Step 1: Install the BuddyPress XML-RPC receiver plugin for Wordpress here: https://github.com/yuttadhammo/buddypress-xmlrpc-receiver 
Step 2: Activate and configure above plugin via wp-admin under Settings->BuddyPress XML-RPC
Step 3: Add your website (root path, e.g. http://www.site.com/) and username to the Preferences screen in this app, then click register.  Wait for the API key to show up under the API key heading.
Step 4: Go to your BuddyPress profile settings under Remote Access (should open automatically after last step) and approve the BuddyDroid app.

Now, if you have done these steps correctly, you should be able to post status updates to your BuddyPress activity stream.

Features
========
- posts updates to stream via xml-rpc
- shows a feed of latest updates and threaded comments
- ability to comment on and delete updates
- retrieves Buddypress notifications
- allows importing plain text / links from other apps

Technical Details
=================
Author: Yuttadhammo Bhikkhu <yuttadhammo@gmail.com>

This software is free and open source, under the GPL v3 license, for more info please visit: http://www.gnu.org/copyleft/gpl.html

Source code for this app is available on GitHub:

https://github.com/yuttadhammo/buddydroid

Changelog
=================
1.6 
- added EditText text transfer on rotate
- fixed text from share
- fixed sound in notification

Credits
=================
- Wordpress, Buddypress, Android, et al
