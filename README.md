
How to use this sample app :

1) Set your env ID at the Flagship init
2) Reproduce the campaigns configurations to your flagship account. (screens in the assets/ folder)
3) Build and run the app

-------------------------------------------------------------------------------------------------

There are 4 campaigns set in this app :

1) Bottom Logo (Perso) : Displays the flagship logo at the bottom left of the app.
2) VIP Feature (Toggle Feature) : Enable the tracking feature for VIP users.
3) Title Wording (ab test) : Change the title wording and the user color according to the selected variations : (Hi, Hello, Hey, Ahoy)
4) Title Wording (ab test) : Change the title to 'Welcome back' or 'Glad to see you back' if the value 'daysSinceLastLaunch' is greater than 5.


The following variables : 'visitorId', 'isVIPUser' and 'daysSinceLastLaunch' are visitor context values.
Flagship decision api will allocation campaigns/variations according to these context values.


You can modify them by clicking on the floating action button at the bottom right of the app.
Then click save to update the campaigns.

-------------------------------------------------------------------------------------------------

Tracking

You can send Flagship hit tracking by clicking the 4 corresponding buttons in the Tracking section of the app.
 
-------------------------------------------------------------------------------------------------

Technical documentation is available at : https://developers.flagship.io/android/


