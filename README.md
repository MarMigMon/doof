# FoodApp

___________ [Insert app name here] is an app that offers the unique feature of a hands-free cooking experience through voice controlled, step by step audio feedback to complete the recipe. It allows users to upload their own recipes and interact with recipes created by other users, which enhance the cooking experience by incorporating this social aspect.

Time spent: [__] hours spent in total

## User Stories

The following **required** functionality is completed:

* [x] The recipe will be read aloud
* [x] The user will be able to scroll through a list of recipes
* [x] The recipes will produce a detail screen when clicked
* [x] The app will be able to register when the user says commands such as "Next Step", "Previous Step", "Repeat Step", and respond accordingly


The following **optional** features are implemented:

* [x] Users can log in and log out of their accounts
* [x] CurrentUser is persisted across sessions (closing and resuming the app)
* [x] New users can be created through sign up
* [x] There will be a search function and a filter to narrow the list of recipes.
  * [x] Options for prep time, price, ingredients, and food type.
  * [x] Filter is persisted through opening and closing the filter window
* [x] Users have a profile page.
  * [x] Within the profile page there are nested fragments for their own recipes, and one for their favorited recipes.
  * [x] Profile page keeps track of recipes contributed (by that user), recipes reviewed (rated), and recipes completed.
  * [x] Users are able to edit their profile page, such as adding a description and changing their profile picture.
* [x] Users receive notifications when other users interact with their recipes.
  * [x] Other users' profiles can be accessed through the notification.
  * [x] The recipe details can be accessed through the notification.
* [x] Ability for the user to add/import their own saved recipes into the app database.
  * [x] Users are prohibited from leaving fields blank
  * [x] Drop down options for category constraints
  * [x] Users can upload a photo for each recipe
  * [ ] Users can record each step themselves to upload audio.
  * [x] Users can add as many steps as they require for the recipe (addStep button)
  * [x] Add and Remove steps buttons autoscroll the screen as they are clicked
* [x] While recipe is being played, there is a user progress bar.
* [x] Main feed features infinite scrolling.
* [x] Search bar automatically filters recipes as user types, and displays options for autocompletion.
* [x] Each recipe displays a view count
* [x] Transition animations between fragments
* [x] Recipe details page displays full recipe information
  * [x] Recipe photo features pull to zoom
  * [x] Users can rate each recipe, and the overall rating for that recipe is an average of all users' input.
  * [x] Users can "favorite" recipes, and that recipe will appear on their favorites tab (on the profile)
  * [x] Speech activity is launched from this page
  * [x] Profile page (including their recipes) of user who contributed that recipe can be accessed by clicking their handle
  


The following **stretch goals** are implemented:

* [ ] Push Notifications!
* [ ] An automatic timer that starts when a timed instruction is read (e.g. baking time, thawing time, etc.)


The following **additional** features are implemented:

* [ ] 


## Wireframe
![Home](Home.png) ![Details](Details.png)

https://www.figma.com/file/XeCmflnw8JHiKel1N4hADTbL/AMarMigMon


## Video Walkthrough

Here's a walkthrough of implemented user stories:



GIF created with [LiceCap](http://www.cockos.com/licecap/).

## Notes

Describe any challenges encountered while building the app:


## Open-source libraries used

- [Android Async HTTP](https://github.com/loopj/android-async-http) - Simple asynchronous HTTP requests with JSON parsing
- API endpoints: 


## Considerations
1. What is your product pitch?
   - Start with a problem statement and follow up with a solution.
   - Focus on engaging your audience with a relatable need.
   The assumption is that people dislike interacting with their devices while cooking, as cooking tends to be hands-on and messy. However, nowadays, people commonly access recipes online, through such devices.
   Cooking is messy, and people like keeping their devices clean (and their food free of germs).
   WE PROVIDE A HANDS FREE COOKING EXPERIENCE THROUGH THE AUDIO FEATURE OF THIS APP.
   
2. Who are the key stakeholders for this app?
   - Who will be using this?
     Anyone who cooks through recipes
   - What will they be using this for?
     Following the steps of the recipes without the need to touch electronic devices while doing so
   
3. What are the core flows?
   - What are the key functions?
     
   - What screens will each user see?
   
4. What will your final demo look like?
   - Describe the flow of your final demo
   
5. What mobile features do you leverage?
   - Leverage at least two mobile-oriented features (i.e. maps and camera)
   
6. What are your technical concerns?
   - What technical features do you need help or resources for?

## License

