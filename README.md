# Froyo

## Functionalities

### 1.	Create Account
User can create the account by their own email old sign up by their google account. it will check the user is already created or not in Firebase Authentication. If the user is new, it will move to Detail form to set up the information. It will give an alert if user makes the blank in the text field.

### 2.	Fill Detail From
For the new User, user must make username(id), the short description, and the own profile image. If the user does not select the image, it will upload the default image. Once the user clicks create button, the User information will be uploaded to Firestore Database.

### 3.	Log in
After creating the account, user can log in with their own email account or google account. it will authenticate with the firebase authentication and move to the Post View which is the Homepage.

### 4.	Log out 
User can click log out by clicking the button in the profile view. it will return to Welcome Page.

### 5.	Profile View
By clicking the person icon in the navigation bar, it will shows the user info with the user profile and number of following and followers. Below the user info, it has 2 buttons that can display user’s own posts or edit the user information. The default is displaying the user’s own posts.

### 6.	Profile Edit view
By clicking the edit button in the profile view, it will remove the user’s posts and display the text field that user change. User can change the description and the profile image. Once clicking the save button, it will update the information into firestore Database and read the information.

### 7.	Post Edit
In the Profile View, each post has edit button in the right top. It only shows when the post is made by the user. When edit button is clicked, it will move to Edit Profile View so that user can change the post image, contents, major tag, tags and update to Firestore Database when user clicks save button. User can delete the post when user clicks the delete button on the right top.

### 8.	Admin
Admin email is ‘admin@gmail.com’ and the password is ‘123456’. Admin can edit all the posts even though the admin does not make the post. Admin can also delete the post. When admin clicks the person icon in the navigation bar. It will display the all-user list. It can search the user and each row has delete button that user can remove the user right away in the Firestore Database.

### 9.	Chatting List view
Show all chat rooms that users are participating in. Also, the most current chat room will be 	on top. Users can go into the chat room by clicking on the row of list.

### 10.	Chatting room view
Users can see all messages that they have sent before, messages are shown in the order of time.

### 11.	Adding chat room
Users can create the chat room. It could be 1: 1 chat, also being group chat. If user create a group chat, user can set the image of chat room and title

### 12.	Search chat room by username and chat room title
In the chat room list and add chat room activity, user can find users or chat rooms by the name of user or the title of char room, if a user is participating into the chat room. The chat 	room is 	being printed as a result.

### 13.	Search message by keyword and travel by controlling controller
Users can search the message by keyword. If the user enters keyword on the search bar, the matched words are highlighted. Also, the scroll moves to the first result, and a remote 		controller will appear on the left side of the screen. Users do not need to find the highlighted 	words by scrolling by themselves.

### 14.	Send message, picture and emoji
In the chat room, users can send the message, pictures are stored in their devices and various emojis

### 15.	Leave chat room
User can leave the chat room, however, if there are some people inside the chat room, the 	chat room will never be deleted. But the user already leaved, cannot access to the chat room

### 16.	Post View
When logged in, users is presented with a List of Posts on the Post. User can Scroll Up / Down to navigate between Posts.  

### 17.	Like/ Write Comments 
For each Post, users can like or comment on the post by clicking on the Like button or the Comment button. Like is a counter that increases each time a user likes a post and doesn’t check for unique like. User can post new comment to a Post through the Comment Diaglog, and the comment section will be updated with the user’s new comment. Likes and Comments are stored in Firestore accordingly. 

### 18.	Sort Posts by keywords/ major tags/ hashtags
Users can click on the search icon button on the top right of the Post tab to filter the Post. Users can search for posts that contain the keyword, or search for posts with a specific major tag (and keyword) or search for posts that contain specific hashtags (and keyword). If there is no post found, a Toast message informs there is no post matches the search criteria will show.  

### 19.	Update and Delete Posts in Post
Users can Edit or Delete posts if they are the author of the posts. There will be an icon button “X” to delete the posts on user’s post page on Post Tab. In the same tab, user can also click “Edit” button to update the content of the posts including the post image/ post content/ post major tag/ post hashtags. 

### 20.	Create New Post 
Users can click the New Post Tab to create a new post. Each post requires post content, a major tag, which is one of the 4 School of RMIT, hashtags which can be added dynamically, and a post image. After submitting the new post, it is stored in Firestore with the created date and all corresponding information, and the new post will be added to the Post Tab immediately. 		

### 21.	Purchase Emoji and Payment
Users can Purchase emojis from the Emoji Store by clicking the “Purchase” button in the Emoji Window. The Emoji Window can be found from the input field of any Chat Rooms. Currently, the Emoji store sells 4 new Emojis and users can purchase each emoji for the price of $1. The payment is processed using Stripe. If the user hasn’t purchased any emoji before, the application will create a new customer id for that user and store it in Firestore. The purchased emoji is then added into the emoji list of the user.  

### 22. Message notification
When the user did not check the message from the other user, it will show the number of messages that user did not check in each chatting list.


## Technology Use
### 1.	Firebase Authentication
### 2.	Firestore Database
### 3.	Stripe
### 4.	Google developer console
### 5.	Volley

## Known bugs
### 1.	Payment only works for first purchase
When the payment for an Emoji purchase is successful, it is required to restart the application to purchase another Emoji. The application will crash if the user clicks on the purchase button of another emoji right after successful payment.

### 2.	Number of posts does not directly decrease when user delete post
After the user successfully delete the post, it should directly decrease number of posts. However, it does not change directly also getting the data again. It only updated after return from the other page.

### 3.	Admin user delete alert dialog does not work
When admin delete the user, it should show the alert to check again. However, for some reason, by making an alert dialog, the app just shut down.
